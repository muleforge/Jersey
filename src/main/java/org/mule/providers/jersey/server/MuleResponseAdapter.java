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

import com.sun.ws.rest.impl.HttpResponseContextImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;

import org.mule.impl.MuleMessage;
import org.mule.providers.http.HttpConnector;
import org.mule.providers.jersey.JerseyMessageAdapter;
import org.mule.umo.UMOMessage;

/**
 * Adapts a HttpServletResponse to provide the methods of HttpResponse
 *
 */
public final class MuleResponseAdapter extends HttpResponseContextImpl {

    private OutputStream out;
    private MuleMessage message;
    
    public MuleResponseAdapter(MuleRequestAdaptor requestContext) {
        super(requestContext);
        
        this.message = new MuleMessage(new JerseyMessageAdapter(this));
    }

    @SuppressWarnings("unchecked")
    public void commit() throws IOException {
        for (Map.Entry<String, List<Object>> e : this.getHttpHeaders().entrySet()) {
            List<String> values = new ArrayList<String>();
            for (Object v : e.getValue())
                values.add(getHeaderValue(v));
            message.setProperty(e.getKey(), values);
        }
        
        message.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, this.getStatus());
        
        Object entity = this.getEntity();
        if (entity != null) {
            final EntityProvider p = ProviderFactory.getInstance().createEntityProvider(entity.getClass());
            p.writeTo(entity, this.getHttpHeaders(), this.getOutputStream());
            if (out != null) {
                out.flush();
                out.close();
            }
        }
                
    }

    public OutputStream getOutputStream() throws IOException {
        if (out != null)
            return out;
        
        return out = new ByteArrayOutputStream();
    }

    public UMOMessage getMessage() {
        return message;
    }
}