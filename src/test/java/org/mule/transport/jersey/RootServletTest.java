package org.mule.transport.jersey;


public class RootServletTest extends AbstractServletTest 
{
    public RootServletTest() 
    {
        super("/*");
    }

    public void testBasic() throws Exception
    {
        testBasic("http://localhost:63088/base");
    }
    
    @Override
    protected String getConfigResources() 
    {
        return "servlet-conf.xml";
    }

}
