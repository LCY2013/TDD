package org.fufeng.tdd;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Context {

    private final Map<Class<?>, Class<?>> componentImplementations = new HashMap<>();

    private final Map<Class<?>, Provider<?>> prividers = new HashMap<>();

    public <ComponentType> void bind(Class<ComponentType> componentType, ComponentType instance) {
        prividers.put(componentType, () -> instance);
    }

    public <ComponentType, ComponentImplementation extends ComponentType>
    void bind(Class<ComponentType> type, Class<ComponentImplementation> implementation) {
        prividers.put(type, () -> {
            try {
                Constructor<ComponentImplementation> constructor = getInjectConstructor(implementation);
                return constructor.newInstance(Arrays.stream(constructor.getParameters()).map(p -> get(p.getType())).toArray());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static  <ComponentImplementation> Constructor<ComponentImplementation>
    getInjectConstructor(Class<ComponentImplementation> implementation) throws NoSuchMethodException {
        return (Constructor<ComponentImplementation>) Arrays.stream(implementation.getConstructors()).
                filter(c -> c.isAnnotationPresent(Inject.class)).
                findFirst().orElseGet(() -> {
                    try {
                        return implementation.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public <ComponentType> ComponentType get(Class<ComponentType> componentType) {
        return (ComponentType) prividers.get(componentType).get();
    }

}
