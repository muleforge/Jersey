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

import com.sun.ws.rest.spi.container.AbstractContainerResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.transport.DefaultMessageAdapter;
import org.mule.transport.http.HttpConnector;

/**
 * Adapts a HttpServletResponse to provide the methods of HttpResponse
 *
 */
public final class MuleResponseAdapter extends AbstractContainerResponse {

    private OutputStream out;
    private MuleMessage message;
    
    public MuleResponseAdapter(MuleRequestAdaptor requestContext) {
        super(requestContext);
        
        this.message = new DefaultMuleMessage(new DefaultMessageAdapter(this));
    }

    @Override
    public void commitStatusAndHeaders() throws IOException {
        for (Map.Entry<String, List<Object>> e : this.getHttpHeaders().entrySet()) {
            List<String> values = new ArrayList<String>();
            for (Object v : e.getValue())
                values.add(getHeaderValue(v));
            message.setProperty(e.getKey(), values);
        }
        
        message.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, this.getStatus());
    }

    @Override
    public OutputStream getUnderlyingOutputStream() throws IOException {
        if (out != null)
            return out;
        
        return out = new ByteArrayOutputStream();
    }

    public void setUnderlyingOutputStream(OutputStream out) {
        this.out = out;
    }
    
    public void commitAll() throws IOException {
        if (isCommitted()) return;
        
        writeEntity(getUnderlyingOutputStream());
    }    
    
    public MuleMessage getMessage() {
        return message;
    }
}