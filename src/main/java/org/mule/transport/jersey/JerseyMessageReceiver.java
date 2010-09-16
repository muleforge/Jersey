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

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.server.impl.model.method.dispatch.FormDispatchProvider;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.component.JavaComponent;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.Callable;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.service.Service;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.ConnectException;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.servlet.HttpRequestMessageAdapter;

/**
 * <code>JerseyMessageReceiver</code> TODO document
 */
public class JerseyMessageReceiver extends AbstractMessageReceiver implements Callable {

    protected transient Log logger = LogFactory.getLog(getClass());
    
    private WebApplication application;
    private boolean applySecurityToProtocol;
    private boolean applyTransformersToProtocol;
    private boolean applyFiltersToProtocol;

    public JerseyMessageReceiver(Connector connector, 
                                 Service service, 
                                 InboundEndpoint endpoint)
        throws CreateException {
        super(connector, service, endpoint);
    }

    public Object onCall(MuleEventContext event) throws Exception {
        MuleMessage message = event.getMessage();
        
        String path = (String) message.getProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
        String contextPath = (String) message.getProperty(HttpConnector.HTTP_CONTEXT_PATH_PROPERTY);
        String query = null;
        int queryIdx = path.indexOf('?');
        if (queryIdx != -1) {
            query = path.substring(queryIdx+1);
            path = path.substring(0, queryIdx);
        }
        
        EndpointURI endpointUri = endpoint.getEndpointURI();
        String host = (String) message.getProperty("Host", endpointUri.getHost());
        String method = (String)message.getProperty(HttpConnector.HTTP_METHOD_PROPERTY);
        InBoundHeaders headers = new InBoundHeaders();
        for (Object prop : message.getPropertyNames()) {
            headers.add(prop.toString(), message.getProperty(prop.toString()).toString());
        }
                
        String scheme;
        if ("servlet".equals(endpointUri.getScheme())) {
            scheme = "http";
        } else {
            scheme = endpointUri.getScheme();
        }
        
        URI baseUri = getBaseUri(endpointUri, scheme, host, contextPath);
        URI completeUri = getCompleteUri(endpointUri, scheme, host, path, query);
        ContainerRequest req = new ContainerRequest(application,
                                                    method,
                                                    baseUri,
                                                    completeUri,
                                                    headers,
                                                    getInputStream(message));
        if (logger.isDebugEnabled())
        {
            logger.debug("Base URI: " + baseUri);
            logger.debug("Complete URI: " + completeUri);
        }
        
        MuleResponseWriter writer = new MuleResponseWriter(message);
        ContainerResponse res = new ContainerResponse(application, req, writer);
        
        MessageAdapter messageAdapter = message.getAdapter();
        if (messageAdapter instanceof HttpRequestMessageAdapter) {
            HttpRequestMessageAdapter httpRequestMessageAdapter = (HttpRequestMessageAdapter) messageAdapter;
            HttpServletRequest request = httpRequestMessageAdapter.getRequest();
            if (request.getMethod().equalsIgnoreCase("POST")) {
                Map requestParameters = request.getParameterMap();

                Form form = new Form();
                req.getProperties().put(FormDispatchProvider.FORM_PROPERTY, form);

                for (Object keyObject : requestParameters.keySet()) {
                    String key = (String) keyObject;
                    form.add(key, request.getParameter(key));
                }
            }
        }

        application.handleRequest(req, res);
        
        return writer.getMessage();
    }

    protected static URI getCompleteUri(EndpointURI endpointUri, String scheme, String host, String path, String query) throws URISyntaxException 
    {
        String uri = scheme + "://" + host + path;
        if (query != null) {
            uri += "?" + query;
        }
            
        return new URI(uri);
    }

    protected static URI getBaseUri(EndpointURI endpointUri, String scheme, String host, String contextPath) throws URISyntaxException {
        if (!contextPath.endsWith("/")) {
            contextPath += "/";
        }
        
        return new URI(scheme + "://" + host + contextPath);
    }
    
    protected static InputStream getInputStream(MuleMessage message) throws TransformerException {
        return (InputStream) message.getPayload(InputStream.class);
    }
    
    public void doConnect() throws Exception {
        Map endpointProps = endpoint.getProperties();
        applyFiltersToProtocol = isTrue((String) endpointProps.get("applyFiltersToProtocol"), true);
        applySecurityToProtocol = isTrue((String) endpointProps.get("applySecurityToProtocol"), true);
        applyTransformersToProtocol = isTrue((String) endpointProps.get("applyTransformersToProtocol"), true);
        
        final Set<Class<?>> resources = new HashSet<Class<?>>();
        
        Class c;
        try {
            c = ((JavaComponent) service.getComponent()).getObjectType();
            resources.add(c);
        } catch (Exception e) {
            throw new ConnectException(e, this);
        }
        
        DefaultResourceConfig resourceConfig = createConfiguration(resources);

        application = WebApplicationFactory.createWebApplication();
        application.initiate(resourceConfig, getComponentProvider(c));
        
        ((JerseyConnector) connector).registerReceiverWithMuleService(this, getEndpointURI());
    }

    protected DefaultResourceConfig createConfiguration(final Set<Class<?>> resources) {
        return new DefaultResourceConfig(resources);
    }


    private boolean isTrue(String string, boolean defaultValue)
    {
        if (string == null) return defaultValue;
        
        return BooleanUtils.toBoolean(string);
    }

    protected IoCComponentProviderFactory getComponentProvider(Class resourceType) {
        return new MuleComponentProviderFactory(service, resourceType);
    }

    public boolean isApplySecurityToProtocol() {
        return applySecurityToProtocol;
    }

    public boolean isApplyTransformersToProtocol() {
        return applyTransformersToProtocol;
    }

    public boolean isApplyFiltersToProtocol() {
        return applyFiltersToProtocol;
    }

}
