package org.mule.providers.jersey;

import java.util.HashMap;
import java.util.Map;

import org.mule.extras.client.MuleClient;
import org.mule.providers.http.HttpConnector;
import org.mule.providers.http.HttpConstants;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;

public class BasicJerseyTest extends FunctionalTestCase {

    public void testBasic() throws Exception
    {
        MuleClient client = new MuleClient();
        
        UMOMessage result = client.send("http://localhost:10081/helloworld", "", null);
        assertEquals("Hello World", result.getPayloadAsString());
        assertEquals(200, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        
        result = client.send("http://localhost:10081/hello", "", null);
        assertEquals(404, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        
        Map<String, String> props = new HashMap<String, String>();
        props.put(HttpConnector.HTTP_METHOD_PROPERTY, HttpConstants.METHOD_GET);
        result = client.send("http://localhost:10081/helloworld", "", props);
        assertEquals(405, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
        
        props.put(HttpConnector.HTTP_METHOD_PROPERTY, HttpConstants.METHOD_DELETE);
        result = client.send("http://localhost:10081/helloworld", "", props);
        assertEquals("Hello World Delete", result.getPayloadAsString());
        assertEquals(200, result.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, 0));
    }
    
    @Override
    protected String getConfigResources() {
        return "basic-conf.xml";
    }

}
