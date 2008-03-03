package org.mule.transport.jersey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.mule.api.MuleEvent;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.OutputHandler;
import org.mule.transformer.AbstractDiscoverableTransformer;
import org.mule.transport.jersey.server.MuleResponseAdapter;

public class JerseyResponseToByteArray extends AbstractDiscoverableTransformer {

    public JerseyResponseToByteArray() {
        super();
        
        registerSourceType(MuleResponseAdapter.class);
        setReturnClass(byte[].class);
    }

    @Override
    protected Object doTransform(Object o, String arg1) throws TransformerException {

        final MuleResponseAdapter res = (MuleResponseAdapter) o;
        
        try {
            res.commitAll();
            
            return ((ByteArrayOutputStream)res.getUnderlyingOutputStream()).toByteArray();
        } catch (IOException e) {
            throw new TransformerException(this, e);
        }
    }

}
