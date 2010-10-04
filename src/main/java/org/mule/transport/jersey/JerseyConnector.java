/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jersey;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleException;
import org.mule.api.context.notification.MuleContextNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.service.Service;
import org.mule.api.transport.MessageReceiver;
import org.mule.component.DefaultJavaComponent;
import org.mule.context.notification.MuleContextNotification;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.model.seda.SedaService;
import org.mule.object.SingletonObjectFactory;
import org.mule.routing.inbound.DefaultInboundRouterCollection;
import org.mule.transport.AbstractConnector;

/**
 * 
 */
public class JerseyConnector extends AbstractConnector implements MuleContextNotificationListener {
    public static final String PROTOCOL_CONNECTOR = "protocolConnector";
    public static final String JERSEY_RESPONSE = "jersey.response";
    private List<SedaService> services = new ArrayList<SedaService>();

    public JerseyConnector() {
        super();
    }
    
    public boolean supportsProtocol(String protocol)
    {
        // we can listen on any protocol provided that the necessary 
        // http headers are there.
        return protocol.startsWith("jersey:");
    }
    
    protected void registerReceiverWithMuleService(MessageReceiver receiver, EndpointURI ep)
        throws MuleException {
        JerseyMessageReceiver jReceiver = (JerseyMessageReceiver)receiver;
        // best I can come up with for now
        String name = new Integer(jReceiver.hashCode()).toString();
        
        // TODO MULE-2228 Simplify this API
        SedaService c = new SedaService();
        c.setName("_internal_jersey" + name + jReceiver.hashCode());
        c.setModel(muleContext.getRegistry().lookupSystemModel());

        c.setComponent(new DefaultJavaComponent(new SingletonObjectFactory(jReceiver)));

        // No determine if the endpointUri requires a new connector to be
        // registed in the case of http we only need to register the new
        // endpointUri if the port is different
        String endpoint = receiver.getEndpointURI().getAddress();

        InboundEndpoint originalEndpoint = receiver.getEndpoint();
        boolean sync = originalEndpoint.isSynchronous();

        EndpointBuilder protocolEndpointBuilder = new EndpointURIEndpointBuilder(endpoint, muleContext);
        protocolEndpointBuilder.setSynchronous(sync);
        protocolEndpointBuilder.setName(ep.getScheme() + ":" + name);
        
        EndpointBuilder receiverEndpointBuilder = new EndpointURIEndpointBuilder(originalEndpoint,
            muleContext);
        
        // Apply the transformers to the correct endpoint
        EndpointBuilder transformerEndpoint;
        if (jReceiver.isApplyTransformersToProtocol())
        {
            transformerEndpoint = protocolEndpointBuilder; 
            receiverEndpointBuilder.setTransformers(null);
            receiverEndpointBuilder.setResponseTransformers(null);
        }
        else
        {  
            transformerEndpoint = receiverEndpointBuilder;
        }

        // Ensure that the transformers aren't empty before setting them. Otherwise Mule will get confused
        // and won't add the default transformers.
        if (originalEndpoint.getTransformers() != null && !originalEndpoint.getTransformers().isEmpty())
        {
            transformerEndpoint.setTransformers(originalEndpoint.getTransformers());
        }

        if (originalEndpoint.getResponseTransformers() != null && !originalEndpoint.getResponseTransformers().isEmpty())
        {
            transformerEndpoint.setResponseTransformers(originalEndpoint.getResponseTransformers());
        }
        
        
        // apply the filters to the correct endpoint
        EndpointBuilder filterEndpoint;
        if (jReceiver.isApplyFiltersToProtocol())
        {
            filterEndpoint = protocolEndpointBuilder;   
            receiverEndpointBuilder.setFilter(null);                                                                                                
        }
        else
        {  
            filterEndpoint = receiverEndpointBuilder;
        }
        filterEndpoint.setFilter(originalEndpoint.getFilter());
        
        // apply the security filter to the correct endpoint
        EndpointBuilder secFilterEndpoint;
        if (jReceiver.isApplySecurityToProtocol())
        {
            secFilterEndpoint = protocolEndpointBuilder;   
            receiverEndpointBuilder.setSecurityFilter(null);                                                                                               
        }
        else
        {  
            secFilterEndpoint = receiverEndpointBuilder;
        }             
        secFilterEndpoint.setSecurityFilter(originalEndpoint.getSecurityFilter());

        String connectorName = (String) originalEndpoint.getProperty(PROTOCOL_CONNECTOR);
        if (connectorName != null) 
        {
            protocolEndpointBuilder.setConnector(muleContext.getRegistry().lookupConnector(connectorName));
        }
        
        InboundEndpoint protocolEndpoint = muleContext.getRegistry()
            .lookupEndpointFactory()
            .getInboundEndpoint(protocolEndpointBuilder);

        InboundEndpoint receiverEndpoint = muleContext.getRegistry()
            .lookupEndpointFactory()
            .getInboundEndpoint(receiverEndpointBuilder);

        jReceiver.setEndpoint(receiverEndpoint);
        
        c.setInboundRouter(new DefaultInboundRouterCollection());
        c.getInboundRouter().addEndpoint(protocolEndpoint);
        
        c.setOutboundRouter(jReceiver.getService().getOutboundRouter());
        
        services.add(c);
    }

    public void onNotification(ServerNotification event) {
        // We need to register the CXF service service once the model
        // starts because
        // when the model starts listeners on components are started, thus
        // all listener
        // need to be registered for this connector before the CXF service
        // service is registered. The implication of this is that to add a
        // new service and a
        // different http port the model needs to be restarted before the
        // listener is available
        if (event.getAction() == MuleContextNotification.CONTEXT_STARTED) {
            for (Service c : services) {
                try {
                    muleContext.getRegistry().registerService(c);
                } catch (MuleException e) {
                    handleException(e);
                }
            }
        }
    }

    @Override
    protected void doConnect() throws Exception {
    }

    @Override
    protected void doDisconnect() throws Exception {
    }

    @Override
    protected void doDispose() {
    }

    @Override
    protected void doInitialise() throws InitialisationException {
        // Registers the listener
        try {
            muleContext.registerListener(this);
        } catch (Exception e) {
            throw new InitialisationException(e, this);
        }
    }
//    
//    public boolean isSyncEnabled(String protocol)
//    {
//        protocol = protocol.toLowerCase();
//        if (protocol.equals("http") || protocol.equals("https") || protocol.equals("ssl") || protocol.equals("tcp") || protocol.equals("servlet"))
//        {
//            return true;
//        }
//        else
//        {
//            return super.isSyncEnabled(protocol);
//        }
//    }

    @Override
    protected void doStart() throws MuleException {
    }

    @Override
    protected void doStop() throws MuleException {
    }

    public String getProtocol() {
        return "jersey";
    }

}
