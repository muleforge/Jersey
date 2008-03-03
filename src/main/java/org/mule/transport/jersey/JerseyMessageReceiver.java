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

import com.sun.ws.rest.api.core.DefaultResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.spi.container.WebApplication;
import com.sun.ws.rest.spi.container.WebApplicationFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.Endpoint;
import org.mule.api.lifecycle.Callable;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.ConnectException;
import org.mule.transport.jersey.server.MuleRequestAdaptor;
import org.mule.transport.jersey.server.MuleResponseAdapter;

/**
 * <code>JerseyMessageReceiver</code> TODO document
 */
public class JerseyMessageReceiver extends AbstractMessageReceiver implements Callable {

    private WebApplication application;
    
    public JerseyMessageReceiver(Connector connector, 
                                 Service service, 
                                 Endpoint endpoint)
        throws CreateException {
        super(connector, service, endpoint);
    }

    public Object onCall(MuleEventContext event) throws Exception {
        MuleMessage message = event.getMessage();
        
        MuleRequestAdaptor req = new MuleRequestAdaptor(message, endpoint.getEndpointURI());
        
        MuleResponseAdapter res = new MuleResponseAdapter(req);
        
        application.handleRequest(req, res);
        
        res.commitStatusAndHeaders();
        
        return res.getMessage();
    }

    public void doConnect() throws Exception {
        final Set<Class> resources = new HashSet<Class>();
        
        try {
            Class c = service.getServiceFactory().getObjectClass();
            resources.add(c);
        } catch (Exception e) {
            throw new ConnectException(e, this);
        }
        
        DefaultResourceConfig resourceConfig = new DefaultResourceConfig(resources);
        
        application = WebApplicationFactory.createWebApplication();
        application.initiate(this, resourceConfig);
        
        ((JerseyConnector) connector).registerReceiverWithMuleService(this, getEndpointURI());
    }

    public void doDisconnect() throws ConnectException {
        
    }

    public void doStart() {
       
    }

    public void doStop() {
        
    }

    public void doDispose() {

    }

}
