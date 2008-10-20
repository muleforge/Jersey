package org.mule.transport.jersey;

import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;

public class BasicJerseyTest extends FunctionalTestCase {

    public void testBasic() throws Exception
    {
        MuleClient client = new MuleClient();
        
        MuleMessage result = client.send("http://localhost:63081/helloworld", "", null);
        assertEquals(200, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        assertEquals("Hello World", result.getPayloadAsString());
        
        // try invalid url
        result = client.send("http://localhost:63081/hello", "", null);
        assertEquals(404, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        
        Map<String, String> props = new HashMap<String, String>();
        props.put(HttpConnector.HTTP_METHOD_PROPERTY, HttpConstants.METHOD_GET);
        result = client.send("http://localhost:63081/helloworld", "", props);
        assertEquals(405, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        
        props.put(HttpConnector.HTTP_METHOD_PROPERTY, HttpConstants.METHOD_DELETE);
        result = client.send("http://localhost:63081/helloworld", "", props);
        assertEquals("Hello World Delete", result.getPayloadAsString());
        assertEquals(200, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
    }
    
    public void testParams() throws Exception
    {
        MuleClient client = new MuleClient();

        Map<String, String> props = new HashMap<String, String>();
        props.put(HttpConnector.HTTP_METHOD_PROPERTY, HttpConstants.METHOD_GET);
        MuleMessage result = client.send("http://localhost:63081/helloworld/sayHelloWithUri/Dan", "", props);
        assertEquals(200, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        assertEquals("Hello Dan", result.getPayloadAsString());
        
        result = client.send("http://localhost:63081/helloworld/sayHelloWithQuery?name=Dan", "", props);
        assertEquals(200, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        assertEquals("Hello Dan", result.getPayloadAsString());

        props.put("X-Name", "Dan");
        result = client.send("http://localhost:63081/helloworld/sayHelloWithHeader", "", props);
        assertEquals(201, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        assertEquals("Hello Dan", result.getPayloadAsString());
        assertEquals("Dan", result.getProperty("X-ResponseName"));
    }
    
    @Override
    protected String getConfigResources() {
        return "basic-conf.xml";
    }

}
