package org.mule.transport.jersey;

import java.util.HashMap;
import java.util.Map;

import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.InetAddrPort;
import org.mule.api.MuleMessage;
import org.mule.extras.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.transport.http.servlet.MuleReceiverServlet;

public class ServletJerseyTest extends FunctionalTestCase {
    public static final int HTTP_PORT = 63088;

    private Server httpServer;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        httpServer = new Server();
        SocketListener socketListener = new SocketListener(new InetAddrPort(HTTP_PORT));
        httpServer.addListener(socketListener);

        HttpContext context = httpServer.getContext("/");
        context.setRequestLog(null);

        ServletHandler handler = new ServletHandler();
        handler.addServlet("MuleReceiverServlet", "/*", MuleReceiverServlet.class
            .getName());

        context.addHandler(handler);
        httpServer.start();
    }
    
    @Override
    protected void doTearDown() throws Exception
    {
        super.doTearDown();
        if (httpServer != null && httpServer.isStarted())
        {
            httpServer.stop();
        }
    }
    
    public void testBasic() throws Exception
    {
        MuleClient client = new MuleClient();
        
        MuleMessage result = client.send("http://localhost:63088/base/helloworld", "", null);
        assertEquals(200, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        assertEquals("Hello World", result.getPayloadAsString());
        
        result = client.send("http://localhost:63088/base/hello", "", null);
        assertEquals(404, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        
        Map<String, String> props = new HashMap<String, String>();
        props.put(HttpConnector.HTTP_METHOD_PROPERTY, HttpConstants.METHOD_GET);
        result = client.send("http://localhost:63088/base/helloworld", "", props);
        assertEquals(405, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        
        props.put(HttpConnector.HTTP_METHOD_PROPERTY, HttpConstants.METHOD_DELETE);
        result = client.send("http://localhost:63088/base/helloworld", "", props);
        assertEquals("Hello World Delete", result.getPayloadAsString());
        assertEquals(200, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
    }
    
    @Override
    protected String getConfigResources() {
        return "servlet-conf.xml";
    }

}
