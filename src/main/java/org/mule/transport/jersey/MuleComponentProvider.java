/**
 * 
 */
package org.mule.transport.jersey;

import com.sun.jersey.spi.service.ComponentContext;
import com.sun.jersey.spi.service.ComponentProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.mule.api.component.JavaComponent;
import org.mule.api.service.Service;

public class MuleComponentProvider implements ComponentProvider {

    private final Service service;
    private final Class resourceType;

    public MuleComponentProvider(Service service, Class resourceType) {
        this.service = service;
        this.resourceType = resourceType;
    }

    public <T> T getInjectableInstance(T instance) {
        return instance;
    }

    public <T> T getInstance(ComponentContext cc, Scope scope, Class<T> c)
        throws InstantiationException, IllegalAccessException {
        return getInstance(scope, c);
    }

    public <T> T getInstance(Scope scope, Class<T> c) throws InstantiationException,
        IllegalAccessException {
        try {
            if (resourceType.isAssignableFrom(c)) { 
                Object result = ((JavaComponent)service.getComponent()).getObjectFactory().getInstance();
                return (T) c.cast(result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }

    public <T> T getInstance(Scope scope, Constructor<T> constructor, Object[] parameters)
        throws InstantiationException, IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
        return getInstance(null, scope, constructor.getDeclaringClass());
    }

    public void inject(Object instance) {
    }
}