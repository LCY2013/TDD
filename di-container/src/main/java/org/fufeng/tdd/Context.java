package org.fufeng.tdd;

import jakarta.inject.Provider;

import java.lang.reflect.InvocationTargetException;
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
                return implementation.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public <ComponentType> ComponentType get(Class<ComponentType> componentType) {
        return (ComponentType) prividers.get(componentType).get();
    }

}
