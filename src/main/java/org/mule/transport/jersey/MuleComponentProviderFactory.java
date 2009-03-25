/**
 * 
 */
package org.mule.transport.jersey;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import org.mule.api.component.JavaComponent;
import org.mule.api.service.Service;

public class MuleComponentProviderFactory implements IoCComponentProviderFactory {

    private final Service service;
    private final Class resourceType;
    
    public MuleComponentProviderFactory(Service service, Class resourceType) {
        this.service = service;
        this.resourceType = resourceType;
    }

    public IoCComponentProvider getComponentProvider(Class<?> cls) {
        if (resourceType.isAssignableFrom(cls)) {
            return getComponentProvider(null, cls);
        }
        return null;
    }

    public IoCComponentProvider getComponentProvider(ComponentContext ctx, final Class<?> cls) {
        return new IoCInstantiatedComponentProvider() {
            public Object getInjectableInstance(Object o) {
                return o;
            }

            public Object getInstance() {
                try {
                    return ((JavaComponent)service.getComponent()).getObjectFactory().getInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


}