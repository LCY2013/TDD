package org.fufeng.tdd;

import jakarta.inject.Provider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ContextConfig {

    private final Map<Class<?>, ComponentProvider<?>> componentPrividers = new HashMap<>();

    public <Type> void bind(Class<Type> componentType, Type instance) {
        componentPrividers.put(componentType, ctx -> instance);
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        componentPrividers.put(type, new InjectionProvider<>(implementation));
    }

    public Context getContext() {
        componentPrividers.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {
            @Override
            public <Type> Optional<Type> get(Class<Type> type) {
                return Optional.ofNullable(componentPrividers.get(type)).
                        map(provider -> (Type) provider.get(this));
            }

            @Override
            public Optional get(ParameterizedType type) {
                if (type.getRawType() != Provider.class) return Optional.empty();
                Type componentType = type.getActualTypeArguments()[0];
                return Optional.ofNullable(componentPrividers.get(componentType)).
                        map(provider -> (Provider<Object>) () -> provider.get(this));
            }
        };
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Class<?> dependency : componentPrividers.get(component).getDependencies()) {
            if (!componentPrividers.containsKey(dependency)) {
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
}
