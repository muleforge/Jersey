package org.mule.transport.jersey;


public class NonRootServletTest extends AbstractServletTest 
{
    public NonRootServletTest() 
    {
        super("/context/*");
    }
    
    public void testBasic() throws Exception
    {
        testBasic("http://localhost:63088/context/base");
    }
    
    @Override
    protected String getConfigResources() 
    {
        return "non-root-servlet-conf.xml";
    }

}
