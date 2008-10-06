package org.mule.transport.jersey;

import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.transport.OutputHandler;
import org.mule.transport.http.HttpConnector;

public class MuleResponseWriter implements ContainerResponseWriter {

    private ByteArrayOutputStream out;
    private DefaultMuleMessage message;

    public MuleResponseWriter() {
        super();
        this.out = new ByteArrayOutputStream();
        this.message = new DefaultMuleMessage(new OutputHandler() {

            public void write(MuleEvent arg0, OutputStream realOut) throws IOException {
                realOut.write(out.toByteArray());
            }
            
        });
    }

    public OutputStream writeStatusAndHeaders(long x, ContainerResponse response) throws IOException {
        for (Map.Entry<String, List<Object>> e : response.getHttpHeaders().entrySet()) {
            List<String> values = new ArrayList<String>();
            for (Object v : e.getValue())
                values.add(ContainerResponse.getHeaderValue(v));
            message.setProperty(e.getKey(), values);
        }

        message.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, response.getStatus());

        return out;
    }

    public MuleMessage getMessage() {
        return message;
    }
}
