/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jersey;

import org.mule.config.MuleProperties;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.AbstractConnector;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.util.object.SingletonObjectFactory;

/**
 * 
 */
public class JerseyConnector extends AbstractConnector
{
    public JerseyConnector() {
        super();
        registerSupportedProtocol("http");
        registerSupportedProtocol("https");
        registerSupportedProtocol("jms");
        registerSupportedProtocol("vm");
        registerSupportedProtocol("servlet");
    }


    @SuppressWarnings("unchecked")
    protected void registerReceiverWithMuleService(UMOMessageReceiver receiver, UMOEndpointURI ep)
        throws UMOException
    {
        JerseyMessageReceiver jReceiver = (JerseyMessageReceiver) receiver;
        
        String name = "foo";
        MuleDescriptor descriptor = new MuleDescriptor("_jerseyConnector." + name);
        descriptor.setServiceFactory(new SingletonObjectFactory(jReceiver));
        
        // No determine if the endpointUri requires a new connector to be
        // registed in the case of http we only need to register the new
        // endpointUri if the port is different
        String endpoint = receiver.getEndpointURI().getAddress();
        String scheme = ep.getScheme().toLowerCase();


        boolean sync = receiver.getEndpoint().isSynchronous();

        // If we are using sockets then we need to set the endpoint name appropiately
        // and if using http/https
        // we need to default to POST and set the Content-Type
        if (scheme.equals("http") || scheme.equals("https") || scheme.equals("ssl")
            || scheme.equals("tcp") || scheme.equals("servlet"))
        {
//            receiver.getEndpoint().getProperties().put(HttpConnector.HTTP_METHOD_PROPERTY, "POST");
//            receiver.getEndpoint().getProperties().put(HttpConstants.HEADER_CONTENT_TYPE,
//                "text/xml");

            // Default to using synchronous for socket based protocols unless the
            // synchronous property has been set explicitly
            if (!receiver.getEndpoint().isSynchronousSet())
            {
                sync = true;
            }
        }
       
        UMOEndpoint serviceEndpoint = new MuleEndpoint(endpoint, true);
        serviceEndpoint.setSynchronous(sync);
        serviceEndpoint.setName(ep.getScheme() + ":" + name);

        // Set the transformers on the endpoint too
        serviceEndpoint.setTransformer(receiver.getEndpoint().getTransformer());
        receiver.getEndpoint().setTransformer(null);

        serviceEndpoint.setResponseTransformer(receiver.getEndpoint().getResponseTransformer());
        receiver.getEndpoint().setResponseTransformer(null);

        // set the filter on the axis endpoint on the real receiver endpoint
        serviceEndpoint.setFilter(receiver.getEndpoint().getFilter());
        // Remove the Axis filter now
        receiver.getEndpoint().setFilter(null);

        // set the Security filter on the axis endpoint on the real receiver
        // endpoint
        serviceEndpoint.setSecurityFilter(receiver.getEndpoint().getSecurityFilter());
        
        // Remove the Axis Receiver Security filter now
        receiver.getEndpoint().setSecurityFilter(null);
        descriptor.getInboundRouter().addEndpoint(serviceEndpoint);
        
        //cxfDescriptor.addInterceptor(new MethodFixInterceptor());
        
        descriptor.setModelName(MuleProperties.OBJECT_SYSTEM_MODEL);
        managementContext.getRegistry().registerService(descriptor, managementContext);
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
    }

    @Override
    protected void doStart() throws UMOException {
    }

    @Override
    protected void doStop() throws UMOException {
    }

    public String getProtocol() {
        return "jersey";
    }

}
