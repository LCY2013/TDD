package org.fufeng.tdd;

import jakarta.inject.Provider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ContextConfig {

    private final Map<Class<?>, ComponentProvider<?>> componentProviders = new HashMap<>();

    public <Type> void bind(Class<Type> componentType, Type instance) {
        componentProviders.put(componentType, ctx -> instance);
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        componentProviders.put(type, new InjectionProvider<>(implementation));
    }

    public Context getContext() {
        componentProviders.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {

            @Override
            public Optional get(Type type) {
                if (isContainerType(type)) {
                    return getContainer((ParameterizedType) type);
                }
                return getComponent((Class<?>) type);
            }

            private Optional<?> getComponent(Class<?> type) {
                return Optional.ofNullable(componentProviders.get(type)).
                        map(provider -> (Object) provider.get(this));
            }

            private Optional getContainer(ParameterizedType type) {
                if (type.getRawType() != Provider.class) return Optional.empty();
                Type componentType = getContainerType(type);
                return Optional.ofNullable(componentProviders.get(componentType)).
                        map(provider -> (Provider<Object>) () -> provider.get(this));
            }

        };
    }

    private static boolean isContainerType(Type type) {
        return type instanceof ParameterizedType;
    }

    private static Type getContainerType(ParameterizedType type) {
        return type.getActualTypeArguments()[0];
    }

    private static boolean isComponentType(Type dependency) {
        return dependency instanceof Class;
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Type dependency : componentProviders.get(component).getDependencies()) {
            if (isComponentType(dependency)) {
                checkComponentDependency(component, visiting, (Class<?>) dependency);
                continue;
            }
            if (isContainerType(dependency)) {
                checkContainerTypeDependency(component, (ParameterizedType) dependency);
            }
        }
    }

    private void checkContainerTypeDependency(Class<?> component, ParameterizedType dependency) {
        if (!componentProviders.containsKey((Class<?>) getContainerType(dependency))) {
            throw new DependencyNotFoundException(component, (Class<?>) getContainerType(dependency));
        }
    }

    private void checkComponentDependency(Class<?> component, Stack<Class<?>> visiting, Class<?> dependency) {
        if (!componentProviders.containsKey(dependency)) {
            throw new DependencyNotFoundException(component, dependency);
        }
        if (visiting.contains(dependency)) {
            throw new CyclicDependenciesException(visiting);
        }
        visiting.push(dependency);
        checkDependencies(dependency, visiting);
        visiting.pop();
    }
}
