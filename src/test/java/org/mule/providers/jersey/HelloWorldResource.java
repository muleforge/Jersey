package org.mule.providers.jersey;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;

@UriTemplate("/helloworld")
public class HelloWorldResource {

    @HttpMethod("POST") 
    @ProduceMime("text/plain")
    public String sayHelloWorld() {
        return "Hello World";
    }
    
    @HttpMethod("DELETE") 
    @ProduceMime("text/plain")
    public String deleteHelloWorld() {
        return "Hello World Delete";
    }
}