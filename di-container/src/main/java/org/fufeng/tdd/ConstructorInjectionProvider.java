package org.fufeng.tdd;

import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public class ConstructorInjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> constructor;
    private final List<Field> fields;
    private final List<Method> methods;

    public ConstructorInjectionProvider(Class<T> component) {
        this.constructor = getInjectConstructor(component);
        this.fields = getFields(component);
        this.methods = getMethods(component);
    }

    @Override
    public T get(Context context) {
        try {
            T instance = constructor.newInstance(Arrays.stream(constructor.getParameters()).
                    map(p -> context.get(p.getType()).get()).toArray());
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(instance, context.get(field.getType()).get());
            }
            for (Method method : methods) {
                method.invoke(instance, Arrays.stream(method.getParameterTypes()).map(pt -> context.get(pt).get()).toArray());
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        return concat(concat(Arrays.stream(constructor.getParameters()).
                        map(Parameter::getType), fields.stream().map(Field::getType)),
                methods.stream().flatMap(m -> Stream.of(m.getParameterTypes()))
        ).toList();
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

    private static List<Method> getMethods(Class<?> component) {
        List<Method> methodList = new ArrayList<>();
        Class<?> finalComponent = component;

        while (component != Object.class) {
            methodList.addAll(Arrays.stream(component.getDeclaredMethods()).
                    filter(m -> m.isAnnotationPresent(Inject.class)).
                    filter(m -> methodList.stream().
                            noneMatch(ml -> m.getName().equals(ml.getName()) && Arrays.equals(m.getParameterTypes(), ml.getParameterTypes()))).
                    filter(m ->
                            Arrays.stream(finalComponent.getDeclaredMethods()).
                                    filter(mc -> !mc.isAnnotationPresent(Inject.class)).
                                    noneMatch(mc -> m.getName().equals(mc.getName()) && Arrays.equals(m.getParameterTypes(), mc.getParameterTypes()))).
                    toList());
            component = component.getSuperclass();
        }

        Collections.reverse(methodList);

        return methodList;
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
