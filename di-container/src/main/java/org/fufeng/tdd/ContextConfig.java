package org.fufeng.tdd;

import jakarta.inject.Provider;

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
            public Optional<?> get(Ref ref) {
                if (ref.isContainer()) {
                    if (ref.getContainer() != Provider.class) return Optional.empty();

                    return Optional.ofNullable(componentProviders.get(ref.getComponent())).
                            map(provider -> (Provider<Object>) () -> provider.get(this));
                }

                return Optional.ofNullable(componentProviders.get(ref.getComponent())).
                        map(provider -> provider.get(this));
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
