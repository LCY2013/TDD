package org.fufeng.tdd;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Nested
class InjectionTest {
    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    class ConstructorInjection {
        // happy path
        //todo: no args constructor
        @Test
        public void should_bind_type_to_a_class_with_default_constructor() {
            config.bind(Component.class, ComponentWithDefaultConstructor.class);

            Component instance = config.getContext().get(Component.class).get();

            assertNotNull(instance);
            assertTrue(instance instanceof ComponentWithDefaultConstructor);
        }

        //todo: with dependencies
        @Test
        public void should_bind_type_to_a_class_with_injection_constructor() {
            Dependency dependency = new Dependency() {
            };

            config.bind(Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, dependency);

            Component component = config.getContext().get(Component.class).get();

            assertNotNull(component);
            assertSame(dependency, ((ComponentWithInjectConstructor) component).getDependency());
        }

        //todo: A -> B -> C
        @Test
        public void should_bind_type_to_a_class_with_transitive_dependencies() {
            config.bind(Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyWithInjectConstructor.class);
            config.bind(String.class, "indirect dependency");

            Component component = config.getContext().get(Component.class).get();
            assertNotNull(component);

            Dependency dependency = ((ComponentWithInjectConstructor) component).getDependency();
            assertTrue(dependency instanceof DependencyWithInjectConstructor);

            assertEquals("indirect dependency", ((DependencyWithInjectConstructor) dependency).getDependency());
        }

        static abstract class AbstractComponent implements Component {

            @Inject
            public AbstractComponent() {
            }
        }

        //todo: abstract class
        @Test
        public void should_throw_exception_if_component_is_abstract() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(ConstructorInjection.AbstractComponent.class).get(config.getContext()));
        }

        //todo: interface
        @Test
        public void should_throw_exception_if_component_is_interface() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(Component.class).get(config.getContext()));
        }

        //sad path

        //todo: multi inject constructors
        @Test
        public void should_throw_exception_if_multi_inject_constructor_provided() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(ComponentWithMultiInjectConstructor.class));
        }

        //todo: no default constructor and inject constructor
        @Test
        public void should_throw_exception_if_no_inject_nor_default_constructor_provided() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(ComponentWithNoInjectConstructorNorDefaultConstructor.class));
        }

        @Test
        public void should_include_dependency_from_inject_constructor() {
            ConstructorInjectionProvider<ComponentWithInjectConstructor> provider = new ConstructorInjectionProvider<>(ComponentWithInjectConstructor.class);
            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray());
        }

    }

    @Nested
    class FieldInjection {
        static class ComponentWithFieldInjection {
            @Inject
            Dependency dependency;
        }

        static class SubclassWithFieldInjection extends FieldInjection.ComponentWithFieldInjection {

        }

        @Test
        public void should_inject_dependency_via_superclass_inject_field() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(FieldInjection.SubclassWithFieldInjection.class, FieldInjection.SubclassWithFieldInjection.class);

            FieldInjection.SubclassWithFieldInjection component =
                    config.getContext().get(FieldInjection.SubclassWithFieldInjection.class).get();

            assertSame(dependency, component.dependency);
        }

        //todo inject field
        @Test
        public void should_inject_dependency_via_field() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(FieldInjection.ComponentWithFieldInjection.class, FieldInjection.ComponentWithFieldInjection.class);

            FieldInjection.ComponentWithFieldInjection component =
                    config.getContext().get(FieldInjection.ComponentWithFieldInjection.class).get();

            assertSame(dependency, component.dependency);
        }

        @Test
        public void should_create_component_with_inject_field() {
            Dependency dependency = mock(Dependency.class);
            Context context = mock(Context.class);
            when(context.get(eq(Dependency.class))).thenReturn(Optional.of(dependency));

            ConstructorInjectionProvider<FieldInjection.ComponentWithFieldInjection> provider =
                    new ConstructorInjectionProvider<>(FieldInjection.ComponentWithFieldInjection.class);
            FieldInjection.ComponentWithFieldInjection component = provider.get(context);

            assertSame(dependency, component.dependency);
        }

        //todo throw exception if field is final
        static class FinalInjectField {
            @Inject
            private final Dependency dependency = null;
        }

        @Test
        public void should_throw_exception_if_inject_field_is_final() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(FieldInjection.FinalInjectField.class));
        }

        //todo throw exception if dependency not found
        //todo throw exception if cyclic dependency
        //todo provide dependency information for field injection
        @Test
        public void should_throw_exception_when_field_dependency_miss() {
            config.bind(FieldInjection.ComponentWithFieldInjection.class, FieldInjection.ComponentWithFieldInjection.class);

            assertThrows(DependencyNotFoundException.class, () -> config.getContext());
        }

        @Test
        public void should_include_field_dependency_in_dependencies() {
            ConstructorInjectionProvider<FieldInjection.ComponentWithFieldInjection> provider =
                    new ConstructorInjectionProvider<>(FieldInjection.ComponentWithFieldInjection.class);

            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
        }

        static class DependencyWithFieldInjection implements Dependency {
            @Inject
            FieldInjection.ComponentWithFieldInjection component;
        }

        @Test
        public void should_throw_exception_when_field_has_cyclic_dependencies() {
            config.bind(FieldInjection.ComponentWithFieldInjection.class, FieldInjection.ComponentWithFieldInjection.class);
            config.bind(Dependency.class, FieldInjection.DependencyWithFieldInjection.class);

            assertThrows(CyclicDependenciesException.class, () -> config.getContext());
        }

        @Test
        public void should_include_field_dependency_in_dependencies_() {
            ConstructorInjectionProvider<FieldInjection.ComponentWithFieldInjection> provider =
                    new ConstructorInjectionProvider<>(FieldInjection.ComponentWithFieldInjection.class);

            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
        }
    }

    @Nested
    class MethodInjection {

        static class InjectMethodWithNoDependency {
            boolean called;

            @Inject
            public void install() {
                this.called = true;
            }
        }

        //todo inject method with no dependencies will be called
        @Test
        public void should_call_inject_method_even_if_no_dependency_declared() {
            config.bind(MethodInjection.InjectMethodWithNoDependency.class, MethodInjection.InjectMethodWithNoDependency.class);

            MethodInjection.InjectMethodWithNoDependency component =
                    config.getContext().get(MethodInjection.InjectMethodWithNoDependency.class).get();

            assertTrue(component.called);
        }

        static class InjectMethodWithDependency {
            Dependency dependency;

            @Inject
            public void install(Dependency dependency) {
                this.dependency = dependency;
            }

        }

        @Test
        public void should_inject_dependency_via_inject_method() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(MethodInjection.InjectMethodWithDependency.class, MethodInjection.InjectMethodWithDependency.class);

            MethodInjection.InjectMethodWithDependency component = config.getContext().get(MethodInjection.InjectMethodWithDependency.class).get();
            assertSame(dependency, component.dependency);
        }

        //todo inject method with dependencies will be injected
        //todo override inject method from superclass
        static class SuperClassWithInjectMethod {
            int superClassCalled;

            @Inject
            public void installSuper() {
                this.superClassCalled++;
            }
        }

        static class SubClassWithInjectMethod extends MethodInjection.SuperClassWithInjectMethod {
            int subClassCalled;

            @Inject
            public void installSub() {
                this.subClassCalled = super.superClassCalled + 1;
            }
        }

        @Test
        public void should_inject_dependencies_via_inject_method_from_superclass() {
            config.bind(MethodInjection.SubClassWithInjectMethod.class, MethodInjection.SubClassWithInjectMethod.class);

            MethodInjection.SubClassWithInjectMethod component = config.getContext().get(MethodInjection.SubClassWithInjectMethod.class).get();

            assertEquals(1, component.superClassCalled);
            assertEquals(2, component.subClassCalled);
        }

        static class SubClassOverrideSuperClassWithInject extends MethodInjection.SuperClassWithInjectMethod {

            @Inject
            @Override
            public void installSuper() {
                super.installSuper();
            }

        }

        @Test
        public void should_only_call_once_if_subclass_override_inject_method_with_inejct() {
            config.bind(MethodInjection.SubClassOverrideSuperClassWithInject.class, MethodInjection.SubClassOverrideSuperClassWithInject.class);

            MethodInjection.SubClassOverrideSuperClassWithInject component = config.getContext().get(MethodInjection.SubClassOverrideSuperClassWithInject.class).get();
            assertEquals(1, component.superClassCalled);
        }

        static class SubClassOverrideSuperClassWithNoInject extends MethodInjection.SuperClassWithInjectMethod {
            @Override
            public void installSuper() {
                super.installSuper();
            }
        }

        @Test
        public void should_not_call_inject_method_if_override_method_no_inject() {
            config.bind(MethodInjection.SubClassOverrideSuperClassWithNoInject.class, MethodInjection.SubClassOverrideSuperClassWithNoInject.class);

            MethodInjection.SubClassOverrideSuperClassWithNoInject component = config.getContext().get(MethodInjection.SubClassOverrideSuperClassWithNoInject.class).get();
            assertEquals(0, component.superClassCalled);
        }

        //todo include dependencies from inject methods
        @Test
        public void shoudl_include_dependencies_from_inject_methods() {
            ConstructorInjectionProvider<MethodInjection.InjectMethodWithDependency> provider =
                    new ConstructorInjectionProvider<>(MethodInjection.InjectMethodWithDependency.class);

            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
        }

        //todo throw exception if type parameter defined
        static class InjectMethodWithTypeParameter {

            @Inject
            <T> void install() {

            }
        }

        @Test
        public void should_throw_exception_if_inject_method_has_type_parameter() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(MethodInjection.InjectMethodWithTypeParameter.class));
        }
    }
}
