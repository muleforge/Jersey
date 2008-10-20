package org.mule.transport.jersey;

import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, String> customHeaders = new HashMap<String,String>();
        for (Map.Entry<String, List<Object>> e : response.getHttpHeaders().entrySet()) {
            customHeaders.put(e.getKey(), getHeaderValue(e.getValue()));
        }

        message.setProperty(HttpConnector.HTTP_CUSTOM_HEADERS_MAP_PROPERTY, customHeaders);
        message.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, response.getStatus());

        return out;
    }

    private String getHeaderValue(List<Object> values) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object o : values) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }

            sb.append(ContainerResponse.getHeaderValue(o));
        }
        return sb.toString();
    }

    public void finish() throws IOException {
    }

    public MuleMessage getMessage() {
        return message;
    }
}
