package org.fufeng.tdd;

import jakarta.inject.Provider;
import jakarta.inject.Qualifier;

import java.lang.annotation.Annotation;
import java.util.*;

public class ContextConfig {

    private final Map<Component, ComponentProvider<?>> components = new HashMap<>();

    public <Type> void bind(Class<Type> componentType, Type instance) {
        components.put(new Component(componentType, null), ctx -> instance);
    }

    public <Type> void bind(Class<Type> componentType, Type instance, Annotation... qualifiers) {
        if (Arrays.stream(qualifiers).anyMatch(qualifier -> !qualifier.getClass().isAnnotationPresent(Qualifier.class))) throw new IllegalComponentException();
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(componentType, qualifier), ctx -> instance);
        }
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        components.put(new Component(type, null), new InjectionProvider<>(implementation));
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation, Annotation... qualifiers) {
        if (Arrays.stream(qualifiers).anyMatch(qualifier -> !qualifier.getClass().isAnnotationPresent(Qualifier.class))) throw new IllegalComponentException();
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(type, qualifier), new InjectionProvider<>(implementation));
        }
    }

    public Context getContext() {
        components.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {

            @Override
            public <ComponentType> Optional<ComponentType> get(ComponentRef<ComponentType> componentRef) {
                if (componentRef.component().qualifier() != null) {
                    return Optional.ofNullable(components.get(componentRef.component())).
                            map(provider -> (ComponentType) provider.get(this));
                }
                if (componentRef.isContainer()) {
                    if (componentRef.getContainer() != Provider.class) return Optional.empty();

                    return (Optional<ComponentType>) Optional.ofNullable(getProvider(componentRef)).
                            map(provider -> (Provider<Object>) () -> provider.get(this));
                }

                return Optional.ofNullable(getProvider(componentRef)).
                        map(provider -> (ComponentType) provider.get(this));
            }

            private <ComponentType> ComponentProvider<?> getProvider(ComponentRef<ComponentType> componentRef) {
                return components.get(componentRef.component());
            }
        };
    }

    // ComponentRef、ContainerRef -> Ref

    private void checkDependencies(Component component, Stack<Class<?>> visiting) {
        for (ComponentRef componentRef : components.get(component).getDependencies()) {
            if (!components.containsKey(componentRef.component())) {
                throw new DependencyNotFoundException(component.type(), componentRef.component().type());
            }
            if (componentRef.getComponentType()) {
                if (visiting.contains(componentRef.component().type())) {
                    throw new CyclicDependenciesException(visiting);
                }
                visiting.push(componentRef.component().type());
                checkDependencies(componentRef.component(), visiting);
                visiting.pop();
            }
        }
    }

}
