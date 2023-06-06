package org.fufeng.tdd;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Context {

    private final Map<Class<?>, Provider<?>> prividers = new HashMap<>();

    public <Type> void bind(Class<Type> componentType, Type instance) {
        prividers.put(componentType, () -> instance);
    }

    public <Type, Implementation extends Type>
    void bind(Class<Type> type, Class<Implementation> implementation) {
        Constructor<Implementation> constructor = getInjectConstructor(implementation);

        prividers.put(type, new CostructorInjectionProvider<>(type, constructor));
    }

    private  <Type> Constructor<Type> getInjectConstructor(Class<Type> implementation) {
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

    class CostructorInjectionProvider<T> implements Provider<T> {
        private final Class<?> componentType;
        private final Constructor<T> constructor;

        private boolean constructing;

        public CostructorInjectionProvider(Class<?> componentType, Constructor<T> constructor) {
            this.componentType = componentType;
            this.constructor = constructor;
        }

        @Override
        public T get() {
            if (constructing) {
                throw new CyclicDependenciesException(componentType);
            }
            try {
                constructing = true;
                return constructor.newInstance(Arrays.stream(constructor.getParameters()).
                        map(p -> Context.this.get(p.getType()).
                                orElseThrow(() -> new DependencyNotFoundException(componentType, p.getType()))).toArray());
            } catch (CyclicDependenciesException e) {
               throw new CyclicDependenciesException(componentType, e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                constructing = false;
            }
        }
    }

    public <Type> Optional<Type> get(Class<Type> type) {
        return Optional.ofNullable(prividers.get(type)).map(t -> (Type) t.get());
    }
}
