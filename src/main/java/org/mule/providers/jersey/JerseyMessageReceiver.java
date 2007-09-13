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

import java.util.HashSet;
import java.util.Set;

import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.spi.container.WebApplication;
import com.sun.ws.rest.spi.container.WebApplicationFactory;
import org.mule.providers.ConnectException;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.jersey.server.MuleRequestAdaptor;
import org.mule.providers.jersey.server.MuleResponseAdapter;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Callable;
import org.mule.umo.lifecycle.CreateException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

/**
 * <code>JerseyMessageReceiver</code> TODO document
 */
public class JerseyMessageReceiver extends AbstractMessageReceiver implements Callable {

    private WebApplication application;
    
    public JerseyMessageReceiver(UMOConnector connector, UMOComponent component, 
                                 UMOEndpoint endpoint)
        throws CreateException {
        super(connector, component, endpoint);
    }

    public Object onCall(UMOEventContext event) throws Exception {
        UMOMessage message = event.getMessage();
        
        MuleRequestAdaptor req = new MuleRequestAdaptor(message, endpoint.getEndpointURI());
        
        MuleResponseAdapter res = new MuleResponseAdapter(req);
        
        application.handleRequest(req, res);

        res.commit();
        
        return res.getMessage();
    }

    public void doConnect() throws Exception {
        final Set<Class> resources = new HashSet<Class>();
        
        try {
            Class c = component.getDescriptor().getServiceFactory().create().getClass();
            resources.add(c);
        } catch (Exception e) {
            throw new ConnectException(e, this);
        }
        
        ResourceConfig resourceConfig = new ResourceConfig() {

            public Set<Class> getResourceClasses() {
                return resources;
            }

            public boolean isIgnoreMatrixParams() {
                return false;
            }

            public boolean isRedirectToNormalizedURI() {
                return false;
            }
            
        };
        
        application = WebApplicationFactory.createWebApplication();
        application.initiate(this, resourceConfig, null);
        
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
