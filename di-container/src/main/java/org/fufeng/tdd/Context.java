package org.fufeng.tdd;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Context {

    private final Map<Class<?>, Provider<?>> prividers = new HashMap<>();

    public <Type> void bind(Class<Type> componentType, Type instance) {
        prividers.put(componentType, () -> instance);
    }

    public <Type, Implementation extends Type>
    void bind(Class<Type> type, Class<Implementation> implementation) {
        Constructor<?>[] injectConstructors = Arrays.stream(implementation.getConstructors()).
                filter(c -> c.isAnnotationPresent(Inject.class)).toArray(Constructor<?>[]::new);
        if (injectConstructors.length > 1) {
            throw new IllegalComponentException();
        }

        if (injectConstructors.length == 0 &&
                Arrays.stream(implementation.getConstructors()).
                        noneMatch(c -> c.getParameters().length == 0)) {
            throw new IllegalComponentException();
        }

        prividers.put(type, () -> {
            try {
                Constructor<Implementation> constructor = getInjectConstructor(implementation);
                return constructor.newInstance(Arrays.stream(constructor.getParameters()).map(p -> get(p.getType())).toArray());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static  <Type> Constructor<Type>
    getInjectConstructor(Class<Type> implementation) throws NoSuchMethodException {
        return (Constructor<Type>) Arrays.stream(implementation.getConstructors()).
                filter(c -> c.isAnnotationPresent(Inject.class)).
                findFirst().orElseGet(() -> {
                    try {
                        return implementation.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public <Type> Type get(Class<Type> componentType) {
        return (Type) prividers.get(componentType).get();
    }

}
