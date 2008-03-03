package org.mule.transport.jersey;

import java.io.IOException;
import java.io.OutputStream;

import org.mule.api.MuleEvent;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.OutputHandler;
import org.mule.transformer.AbstractDiscoverableTransformer;
import org.mule.transport.jersey.server.MuleResponseAdapter;

public class JerseyResponseToOutputHandler extends AbstractDiscoverableTransformer {

    public JerseyResponseToOutputHandler() {
        super();
        
        registerSourceType(MuleResponseAdapter.class);
        setReturnClass(OutputHandler.class);
    }

    @Override
    protected Object doTransform(Object o, String arg1) throws TransformerException {
        final MuleResponseAdapter res = (MuleResponseAdapter) o;
        
        return new OutputHandler() {

            public void write(MuleEvent event, OutputStream out) throws IOException {
                res.setUnderlyingOutputStream(out);
                
                res.commitAll();
            }
        };
    }

}
