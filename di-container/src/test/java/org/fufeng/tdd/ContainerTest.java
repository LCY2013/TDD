package org.fufeng.tdd;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest {

    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    class ComponentConstruct {

        //todo: instance
        @Test
        public void should_bind_type_to_a_specific_instance() {
            Component instance = new Component() {
            };

            config.bind(Component.class, instance);

            assertSame(instance, config.getContext().get(Component.class).get());
        }

        @Test
        public void should_return_empty_if_component_not_defined() {
            Optional<Component> component = config.getContext().get(Component.class);
            assertTrue(component.isEmpty());
        }

        @Nested
        class DependencyCheck {
            //todo: dependencies not exist
            @Test
            public void should_throw_exception_if_dependency_not_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext().get(Component.class));

                assertEquals(Dependency.class, exception.getDependency());
                assertEquals(Component.class, exception.getComponent());
            }

            @Test
            public void should_throw_exception_when_get_context_if_dependency_not_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

                assertEquals(Dependency.class, exception.getDependency());
                assertEquals(Component.class, exception.getComponent());
            }

            @Test
            public void should_throw_exception_if_transitive_dependency_not_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyWithInjectConstructor.class);
                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext().get(Component.class));

                assertEquals(String.class, exception.getDependency());
                assertEquals(Dependency.class, exception.getComponent());
            }

            @Test
            public void should_throw_exception_if_cyclic_dependencies_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyDependencyWithInjectConstructor.class);

                assertThrowsExactly(CyclicDependenciesException.class, () -> config.getContext().get(Component.class));
            }

            @Test
            public void should_throw_details_exception_if_cyclic_dependencies_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyDependencyWithInjectConstructor.class);

                CyclicDependenciesException exception = assertThrows(CyclicDependenciesException.class, () -> config.getContext().get(Component.class));

                Set<Class<?>> classes = exception.getComponents();
                assertEquals(2, classes.size());
                assertTrue(classes.contains(Component.class));
                assertTrue(classes.contains(Dependency.class));
            }

            @Test
            public void should_throw_exception_if_transitive_cyclic_dependencies_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
                config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

                assertThrowsExactly(CyclicDependenciesException.class, () -> config.getContext().get(Component.class));
            }

            @Test
            public void should_throw_exception_when_get_context_if_transitive_cyclic_dependencies_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
                config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

                assertThrowsExactly(CyclicDependenciesException.class, () -> config.getContext());
            }

            @Test
            public void should_throw_details_exception_if_transitive_cyclic_dependencies_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
                config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

                CyclicDependenciesException exception = assertThrows(CyclicDependenciesException.class, () -> config.getContext().get(Component.class));

                Set<Class<?>> classes = exception.getComponents();
                assertEquals(3, classes.size());
                assertTrue(classes.contains(Component.class));
                assertTrue(classes.contains(Dependency.class));
                assertTrue(classes.contains(AnotherDependency.class));
            }

            @Test
            public void should_throw_details_exception_when_get_context_if_transitive_cyclic_dependencies_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
                config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

                CyclicDependenciesException exception = assertThrows(CyclicDependenciesException.class, () -> config.getContext());

                Set<Class<?>> classes = exception.getComponents();
                assertEquals(3, classes.size());
                assertTrue(classes.contains(Component.class));
                assertTrue(classes.contains(Dependency.class));
                assertTrue(classes.contains(AnotherDependency.class));
            }

            //todo throw exception if dependency not found
            //todo throw exception if cyclic dependency
            //todo provide dependency information for field injection

            static class DependencyNotFoundWithFieldInjection {
                @Inject
                String dependency;
            }

            @Test
            public void should_throw_exception_when_field_dependency_miss() {
                config.bind(DependencyNotFoundWithFieldInjection.class, DependencyNotFoundWithFieldInjection.class);

                assertThrows(DependencyNotFoundException.class, () -> config.getContext());
            }

            static class DependencyWithFieldInjection implements Dependency {
                @Inject
                ComponentWithFieldInjection component;
            }

            static class ComponentWithFieldInjection {
                @Inject
                Dependency dependency;
            }

            @Test
            public void should_throw_exception_when_field_has_cyclic_dependencies() {
                config.bind(ComponentWithFieldInjection.class, ComponentWithFieldInjection.class);
                config.bind(Dependency.class, DependencyWithFieldInjection.class);

                assertThrows(CyclicDependenciesException.class, () -> config.getContext());
            }
        }

    }

    @Nested
    class DependenciesSelection {

    }

    @Nested
    class LifecycleManagement {

    }

}

interface Component {

}

interface Dependency {

}

interface AnotherDependency {

}

class ComponentWithDefaultConstructor implements Component {

    public ComponentWithDefaultConstructor() {
    }

}

class ComponentWithInjectConstructor implements Component {

    private final Dependency dependency;

    @Inject
    public ComponentWithInjectConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }
}

class ComponentWithMultiInjectConstructor implements Component {

    private final String name;
    private Double value;

    @Inject
    public ComponentWithMultiInjectConstructor(String name) {
        this.name = name;
    }

    @Inject
    public ComponentWithMultiInjectConstructor(String name, Double value) {
        this.name = name;
        this.value = value;
    }
}

class ComponentWithNoInjectConstructorNorDefaultConstructor implements Component {
    public ComponentWithNoInjectConstructorNorDefaultConstructor(String name) {
    }
}

class DependencyWithInjectConstructor implements Dependency {

    private final String dependency;

    @Inject
    public DependencyWithInjectConstructor(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }

}

class DependencyDependencyWithInjectConstructor implements Dependency {

    private final Component component;

    @Inject
    public DependencyDependencyWithInjectConstructor(Component component) {
        this.component = component;
    }

}

class AnotherDependencyDependedOnComponent implements AnotherDependency {

    private final Component component;

    @Inject
    public AnotherDependencyDependedOnComponent(Component component) {
        this.component = component;
    }

}

class DependencyDependedOnAnotherDependency implements Dependency {

    private final AnotherDependency anotherDependency;

    @Inject
    public DependencyDependedOnAnotherDependency(AnotherDependency anotherDependency) {
        this.anotherDependency = anotherDependency;
    }
}
