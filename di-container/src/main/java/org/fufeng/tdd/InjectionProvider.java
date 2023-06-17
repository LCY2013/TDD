package org.fufeng.tdd;

import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public class InjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> injectConstructor;
    private final List<Field> injectFields;
    private final List<Method> injectMethods;

    public InjectionProvider(Class<T> component) {
        if (Modifier.isAbstract(component.getModifiers())) throw new IllegalComponentException(component);
        if (Modifier.isInterface(component.getModifiers())) throw new IllegalComponentException(component);
        this.injectConstructor = getInjectConstructor(component);
        this.injectFields = getFields(component);
        this.injectMethods = getMethods(component);

        if (injectFields.stream().anyMatch(field -> Modifier.isFinal(field.getModifiers())))
            throw new IllegalComponentException(component);
        if (injectMethods.stream().anyMatch(method -> Arrays.stream(method.getTypeParameters()).anyMatch(t -> true)))
            throw new IllegalComponentException(component);
    }

    @Override
    public T get(Context context) {
        try {
            T instance = injectConstructor.newInstance(toDependencies(context, injectConstructor));
            for (Field field : injectFields) {
                field.setAccessible(true);
                field.set(instance, toDependency(context, field));
            }
            for (Method method : injectMethods) {
                method.setAccessible(true);
                method.invoke(instance, toDependencies(context, method));
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ComponentRef> getDependencies() {
        return concat(concat(Arrays.stream(injectConstructor.getParameters()).map(Parameter::getParameterizedType),
                        injectFields.stream().map(Field::getGenericType)),
                injectMethods.stream().flatMap(m -> Arrays.stream(m.getParameters()).map(Parameter::getParameterizedType))
        ).map(ComponentRef::of).toList() ;
    }

    private static List<Field> getFields(Class<?> component) {
        return traverse(component, (fields, currentComponent) -> injectable(currentComponent.getDeclaredFields()).collect(Collectors.toList()));
    }

    private static List<Method> getMethods(Class<?> component) {
        List<Method> methodList = traverse(component, (methods, currentComponent) -> injectable(currentComponent.getDeclaredMethods()).
                filter(m -> isOverrideByInjectMethod(methods, m)).
                filter(m -> isOverrideByNoInjectMethod(component, m)).
                toList());

        Collections.reverse(methodList);
        return methodList;
    }

    private static <Type> Constructor<Type> getInjectConstructor(Class<Type> implementation) {
        List<Constructor<?>> injectConstructors = injectable(implementation.getConstructors()).toList();
        if (injectConstructors.size() > 1) {
            throw new IllegalComponentException();
        }

        return (Constructor<Type>) injectConstructors.stream().findFirst().orElseGet(() -> getDefaultConstructor(implementation));
    }

    private static <Type> Constructor<Type> getDefaultConstructor(Class<Type> implementation) {
        try {
            return implementation.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalComponentException();
        }
    }

    private static <T extends AnnotatedElement> Stream<T> injectable(T[] declaredElement) {
        return Arrays.stream(declaredElement).
                filter(f -> f.isAnnotationPresent(Inject.class));
    }

    private static boolean isOverride(Method m, Method ml) {
        return m.getName().equals(ml.getName()) && Arrays.equals(m.getParameterTypes(), ml.getParameterTypes());
    }

    private static boolean isOverrideByNoInjectMethod(Class<?> finalComponent, Method m) {
        return Arrays.stream(finalComponent.getDeclaredMethods()).
                filter(mc -> !mc.isAnnotationPresent(Inject.class)).
                noneMatch(mc -> isOverride(m, mc));
    }

    private static boolean isOverrideByInjectMethod(List<Method> injectMethods, Method m) {
        return injectMethods.stream().
                noneMatch(ml -> isOverride(m, ml));
    }

    private static Object[] toDependencies(Context context, Executable executable) {
        return Arrays.stream(executable.getParameters()).
                map(pt -> toDependency(context, pt.getParameterizedType())).toArray();
    }

    private static Object toDependency(Context context, Field field) {
        return toDependency(context, field.getGenericType());
    }

    private static Object toDependency(Context context, Type type) {
        return context.get(ComponentRef.of(type)).get();
    }

    private static <T> List<T> traverse(Class<?> component, BiFunction<List<T>, Class<?>, List<T>> finder) {
        List<T> members = new ArrayList<>();
        while (component != Object.class) {
            members.addAll(finder.apply(members, component));
            component = component.getSuperclass();
        }
        return members;
    }

}
