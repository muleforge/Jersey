package org.mule.transport.jersey.transformer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractDiscoverableTransformer;
import org.mule.transport.jersey.server.MuleResponseAdapter;

public class JerseyResponseToString extends AbstractDiscoverableTransformer {

    public JerseyResponseToString() {
        super();;
        registerSourceType(MuleResponseAdapter.class);
        setReturnClass(String.class);
    }

    @Override
    protected Object doTransform(Object o, String enc) throws TransformerException {
        final MuleResponseAdapter res = (MuleResponseAdapter) o;
        
        try {
            res.commitAll();
            
            return new String(((ByteArrayOutputStream)res.getUnderlyingOutputStream()).toByteArray(), enc);
        } catch (IOException e) {
            throw new TransformerException(this, e);
        }
    }

}
