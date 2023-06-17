package org.fufeng.tdd;

import jakarta.inject.Provider;

import java.lang.annotation.Annotation;
import java.util.*;

public class ContextConfig {

    private final Map<Class<?>, ComponentProvider<?>> componentProviders = new HashMap<>();
    private final Map<Component, ComponentProvider<?>> components = new HashMap<>();

    record Component(Class<?> type, Annotation qualifier){}

    public <Type> void bind(Class<Type> componentType, Type instance) {
        componentProviders.put(componentType, ctx -> instance);
    }

    public <Type> void bind(Class<Type> componentType, Type instance, Annotation... qualifiers) {
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(componentType, qualifier), ctx -> instance);
        }
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        componentProviders.put(type, new InjectionProvider<>(implementation));
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation, Annotation... qualifiers) {
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(type, qualifier), new InjectionProvider<>(implementation));
        }
    }

    public Context getContext() {
        componentProviders.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {

            @Override
            public <ComponentType> Optional<ComponentType> get(Ref<ComponentType> ref) {
                if (ref.getQualifier() != null) {
                    return Optional.ofNullable(components.get(new Component(ref.getComponent(), ref.getQualifier()))).
                            map(provider -> (ComponentType) provider.get(this));
                }
                if (ref.isContainer()) {
                    if (ref.getContainer() != Provider.class) return Optional.empty();

                    return (Optional<ComponentType>) Optional.ofNullable(componentProviders.get(ref.getComponent())).
                            map(provider -> (Provider<Object>) () -> provider.get(this));
                }

                return Optional.ofNullable(componentProviders.get(ref.getComponent())).
                        map(provider -> (ComponentType) provider.get(this));
            }
        };
    }

    class Reference {

    }

    // ComponentRef、ContainerRef -> Ref

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Context.Ref ref : componentProviders.get(component).getDependencies()) {
            if (!componentProviders.containsKey(ref.getComponent())) {
                throw new DependencyNotFoundException(component, ref.getComponent());
            }
            if (ref.isComponent()) {
                if (visiting.contains(ref.getComponent())) {
                    throw new CyclicDependenciesException(visiting);
                }
                visiting.push(ref.getComponent());
                checkDependencies(ref.getComponent(), visiting);
                visiting.pop();
            }
        }
    }

}
