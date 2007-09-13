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

package org.mule.providers.jersey.server;

import com.sun.ws.rest.impl.HttpRequestContextImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.mule.config.MuleProperties;
import org.mule.providers.http.HttpConnector;
import org.mule.providers.http.HttpConstants;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

/**
 * Adapts a HttpServletRequest to provide the methods of HttpRequest
 */
public class MuleRequestAdaptor extends HttpRequestContextImpl {
    
    private UMOMessage message;
    
    /** Creates a new instance of HttpRequestAdaptor */
    public MuleRequestAdaptor(UMOMessage message, UMOEndpointURI endpointUri) throws IOException {
        super((String)message.getProperty(HttpConnector.HTTP_METHOD_PROPERTY), 
              getInputStream(message));
        this.message = message;
        
        String path = (String) message.getProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
        String query = null;
        int queryIdx = path.indexOf('?');
        if (queryIdx != -1) {
            query = path.substring(queryIdx);
            path = path.substring(0, queryIdx);
        }
        
        try {
            this.uri = new URI(endpointUri.getScheme(), null, endpointUri.getHost(), endpointUri.getPort(), path, query, null);
        } catch (URISyntaxException e) {
           
            e.printStackTrace();
        }
       
        this.queryString = uri.getQuery();
        this.uriPath = uri.getPath();
        if (this.uriPath.startsWith("/")) {
            this.uriPath = uriPath.substring(1);
        }
        this.baseURI = getBaseURI(this.uri, this.uriPath);
        
        this.queryParameters = extractQueryParameters(this.queryString, true);
        
        copyHttpHeaders();
    }
    
    private static InputStream getInputStream(UMOMessage message) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void copyHttpHeaders() {
        MultivaluedMap<String, String> headers = getRequestHeaders();
        for (Iterator names = message.getPropertyNames().iterator(); names.hasNext();) {
            String name = (String) names.next();
            List<String> valueList = Collections.singletonList((String)message.getProperty(name));
            
            headers.put(name, valueList);
        }
        
//        List<Cookie> cookies = getCookies();
//        javax.servlet.http.Cookie servletCookies[] = request.getCookies();
//        if (servletCookies != null) {
//            for (javax.servlet.http.Cookie c: servletCookies) {
//                NewCookie n = new NewCookie(c.getName(), c.getValue());
//                n.setComment(c.getComment());
//                n.setDomain(c.getDomain());
//                n.setPath(c.getPath());
//                n.setSecure(c.getSecure());
//                n.setVersion(c.getVersion());
//                n.setMaxAge(c.getMaxAge());
//                cookies.add(n);
//            }
//        }
    }

}
