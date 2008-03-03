/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.mule.transport.jersey.server;

import com.sun.ws.rest.impl.container.servlet.HttpRequestAdaptor;
import com.sun.ws.rest.spi.container.AbstractContainerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.mule.api.MuleMessage;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.transformer.TransformerException;
import org.mule.transport.http.HttpConnector;

/**
 * Adapts a HttpServletRequest to provide the methods of HttpRequest
 */
public class MuleRequestAdaptor extends AbstractContainerRequest {
    
    private MuleMessage message;
    
    /** Creates a new instance of HttpRequestAdaptor 
     * @throws TransformerException */
    public MuleRequestAdaptor(MuleMessage message, 
                              EndpointURI endpointUri) throws IOException, TransformerException {
        super((String)message.getProperty(HttpConnector.HTTP_METHOD_PROPERTY), 
              getInputStream(message));
        this.message = message;
        
        String path = (String) message.getProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
        String query = null;
        int queryIdx = path.indexOf('?');
        if (queryIdx != -1) {
            query = path.substring(queryIdx+1);
            path = path.substring(0, queryIdx);
        }
        
        try {
            this.baseUri = new URI(endpointUri.getScheme(), null, endpointUri.getHost(), endpointUri.getPort(), endpointUri.getPath(), null, null);
            this.completeUri = new URI(endpointUri.getScheme(), null, endpointUri.getHost(), endpointUri.getPort(), path, query, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not create URI for " + endpointUri.toString());
        }
        
        copyHttpHeaders();
    }
    
    private static InputStream getInputStream(MuleMessage message) throws TransformerException {
        return (InputStream) message.getPayload(InputStream.class);
    }

    @SuppressWarnings("unchecked")
    protected void copyHttpHeaders() {
        MultivaluedMap<String, String> headers = getRequestHeaders();
        for (Iterator names = message.getPropertyNames().iterator(); names.hasNext();) {
            String name = (String) names.next();
            List<String> valueList = Collections.singletonList((String)message.getProperty(name));
            
            headers.put(name, valueList);
        }
    }

}
