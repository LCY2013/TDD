package org.fufeng.tdd;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstructorInjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> constructor;
    private final List<Field> fields;

    public ConstructorInjectionProvider(Class<T> component) {
        this.constructor = getInjectConstructor(component);
        this.fields = getFields(component);
    }

    @Override
    public T get(Context context) {
        try {
            T instance = constructor.newInstance(Arrays.stream(constructor.getParameters()).
                    map(p -> {
                        return context.get(p.getType()).get();
                    }).toArray());
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(instance, context.get(field.getType()).get());
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        return Stream.concat(Arrays.stream(constructor.getParameters()).
                map(Parameter::getType), fields.stream().map(Field::getType)).toList();
    }

    private static List<Field> getFields(Class<?> component) {
        List<Field> componentFields = Arrays.stream(component.getDeclaredFields()).
                filter(f -> f.isAnnotationPresent(Inject.class)).
                collect(Collectors.toList());
        while (component != Object.class) {
            componentFields.addAll(getFields(component.getSuperclass()));
            component = component.getSuperclass();
        }
        return componentFields;
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
                        return implementation.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException();
                    }
                });
    }

}
