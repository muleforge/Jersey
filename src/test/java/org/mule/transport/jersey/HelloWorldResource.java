package org.mule.transport.jersey;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;

@Path("/helloworld")
public class HelloWorldResource {

    @POST
    @ProduceMime("text/plain")
    public String sayHelloWorld() {
        return "Hello World";
    }
    
    @DELETE
    @ProduceMime("text/plain")
    public String deleteHelloWorld() {
        return "Hello World Delete";
    }
    
    @GET
    @ProduceMime("text/plain")
    @Path("/sayHelloWithUri/{name}")
    public String sayHelloWithUri(@UriParam("name") String name) {
        return "Hello " + name;
    }
    
    @GET
    @ProduceMime("text/plain")
    @Path("/sayHelloWithQuery")
    public String sayHelloWithQuery(@QueryParam("name") String name) {
        return "Hello " + name;
    }
}