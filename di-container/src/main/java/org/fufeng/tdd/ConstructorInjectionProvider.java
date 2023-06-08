package org.fufeng.tdd;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConstructorInjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> constructor;

    public ConstructorInjectionProvider(Class<T> component) {
        this.constructor = getInjectConstructor(component);
    }

    private static <Type> Constructor<Type> getInjectConstructor(Class<Type> implementation) {
        List<Constructor<?>> injectConstructors = Arrays.stream(implementation.getConstructors()).
                filter(c -> c.isAnnotationPresent(Inject.class)).toList();
        if (injectConstructors.size() > 1) {
            throw new IllegalComponentException();
        }

        return (Constructor<Type>) injectConstructors.stream().
                findFirst().orElseGet(() -> {
                    try {
                        return implementation.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException();
                    }
                });
    }

    @Override
    public T get(Context context) {
        try {
            return constructor.newInstance(Arrays.stream(constructor.getParameters()).
                    map(p -> {
                        Class<?> type = p.getType();
                        return context.get(type).get();
                    }).toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        return Arrays.stream(constructor.getParameters()).
                map(Parameter::getType).collect(Collectors.toList());
    }

}
