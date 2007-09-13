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

import java.io.ByteArrayOutputStream;

import org.mule.impl.ThreadSafeAccess;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.providers.jersey.server.MuleResponseAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

/**
 * <code>AbderaMessageAdapter</code> TODO document
 */
public class JerseyMessageAdapter extends AbstractMessageAdapter
{
 
    /* For general guidelines on writing transports see
       http://mule.mulesource.org/display/MULE/Writing+Transports */

    /* IMPLEMENTATION NOTE: The MessageAdapter is used to wrap an underlying
       message. It should store a copy of the underlying message as an
       instance variable. */
    
    private MuleResponseAdapter res;

    public JerseyMessageAdapter(MuleResponseAdapter message) 
    {
        this.res = message;
    }

    public String getPayloadAsString(String encoding) throws Exception
    {
        return new String(getPayloadAsBytes(), encoding);
    }

    public byte[] getPayloadAsBytes() throws Exception
    {
        return ((ByteArrayOutputStream)res.getOutputStream()).toByteArray();
    }

    public Object getPayload()
    {
        try {
            return getPayloadAsString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ThreadSafeAccess newThreadCopy() {
        return new JerseyMessageAdapter(res);
    }

}
