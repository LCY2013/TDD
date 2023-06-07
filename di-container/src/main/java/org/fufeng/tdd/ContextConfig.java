package org.fufeng.tdd;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class ContextConfig {

    private final Map<Class<?>, ComponentProvider<?>> componentPrividers = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> dependencies = new HashMap<>();

    public <Type> void bind(Class<Type> componentType, Type instance) {
        componentPrividers.put(componentType, (ctx) -> instance);
        dependencies.put(componentType, List.of());
    }

    public <Type, Implementation extends Type>
    void bind(Class<Type> type, Class<Implementation> implementation) {
        Constructor<Implementation> constructor = getInjectConstructor(implementation);
        componentPrividers.put(type, new CostructorInjectionProvider<>(type, constructor));
        dependencies.put(type,
                Arrays.stream(constructor.getParameters()).
                        map(Parameter::getType).collect(Collectors.toList()));
    }

    private <Type> Constructor<Type> getInjectConstructor(Class<Type> implementation) {
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

    interface ComponentProvider<T> {
        T get(Context context);
    }

    class CostructorInjectionProvider<T> implements ComponentProvider<T> {
        private final Class<?> componentType;
        private final Constructor<T> constructor;

        private boolean constructing;

        public CostructorInjectionProvider(Class<?> componentType, Constructor<T> constructor) {
            this.componentType = componentType;
            this.constructor = constructor;
        }

        @Override
        public T get(Context context) {
            if (constructing) {
                throw new CyclicDependenciesException(componentType);
            }
            try {
                constructing = true;
                return constructor.newInstance(Arrays.stream(constructor.getParameters()).
                        map(p -> {
                            Class<?> type = p.getType();
                            return context.get(type).
                                    orElseThrow(() -> new DependencyNotFoundException(componentType, p.getType()));
                        }).toArray());
            } catch (CyclicDependenciesException e) {
                throw new CyclicDependenciesException(componentType, e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                constructing = false;
            }
        }
    }

    public Context getContext() {
        for (Class<?> component : dependencies.keySet()) {
            for (Class<?> dependency : dependencies.get(component)) {
                if (!dependencies.containsKey(dependency)) {
                    throw new DependencyNotFoundException(component, dependency);
                }
            }
        }

        return new Context() {
            @Override
            public <Type> Optional<Type> get(Class<Type> type) {
                return Optional.ofNullable(componentPrividers.get(type)).map(t -> (Type) t.get(this));
            }
        };
    }
}
