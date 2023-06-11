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
public class InjectionTest {
    Dependency dependency = mock(Dependency.class);

    Context context = mock(Context.class);

    @BeforeEach
    public void setup() {
        when(context.get(Dependency.class)).thenReturn(Optional.of(dependency));
    }

    @Nested
    class ConstructorInjection {

        @Nested
        class Injection {
            // happy path
            //todo: no args constructor
            @Test
            public void should_call_default_constructor_if_no_inject_constructor() {
                ConstructorInjectionProvider<ComponentWithDefaultConstructor> provider = new ConstructorInjectionProvider<>(ComponentWithDefaultConstructor.class);
                Component component = provider.get(context);

                assertNotNull(component);
            }

            //todo: with dependencies
            @Test
            public void should_inject_dependency_via_inject_constructor() {
                ComponentWithInjectConstructor component =
                        new ConstructorInjectionProvider<>(ComponentWithInjectConstructor.class).get(context);


                assertNotNull(component);
                assertSame(dependency, component.getDependency());
            }

            @Test
            public void should_include_dependency_from_inject_constructor() {
                ConstructorInjectionProvider<ComponentWithInjectConstructor> provider = new ConstructorInjectionProvider<>(ComponentWithInjectConstructor.class);
                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray());
            }
        }

        @Nested
        class IllegalInjectConstructors {
            static abstract class AbstractComponent implements Component {

                @Inject
                public AbstractComponent() {
                }
            }

            //todo: abstract class
            @Test
            public void should_throw_exception_if_component_is_abstract() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(AbstractComponent.class));
            }

            //todo: interface
            @Test
            public void should_throw_exception_if_component_is_interface() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(Component.class));
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
        }

    }

    @Nested
    class FieldInjection {

        @Nested
        class Injection {
            static class ComponentWithFieldInjection {
                @Inject
                Dependency dependency;
            }


            static class SubclassWithFieldInjection extends ComponentWithFieldInjection {

            }

            @Test
            public void should_inject_dependency_via_superclass_inject_field() {
                ConstructorInjectionProvider<SubclassWithFieldInjection> provider = new ConstructorInjectionProvider<>(SubclassWithFieldInjection.class);
                SubclassWithFieldInjection component =
                        provider.get(context);


                assertSame(dependency, component.dependency);
            }

            //todo inject field
            @Test
            public void should_inject_dependency_via_field() {
                ConstructorInjectionProvider<ComponentWithFieldInjection> provider = new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);
                ComponentWithFieldInjection component =
                        provider.get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            public void should_create_component_with_inject_field() {
                Dependency dependency = mock(Dependency.class);
                Context context = mock(Context.class);
                when(context.get(eq(Dependency.class))).thenReturn(Optional.of(dependency));

                ConstructorInjectionProvider<ComponentWithFieldInjection> provider =
                        new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);
                ComponentWithFieldInjection component = provider.get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            public void should_include_dependency_from_field_dependencies_() {
                ConstructorInjectionProvider<ComponentWithFieldInjection> provider =
                        new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);

                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
            }

            @Test
            public void should_include_dependencies_from_field_dependency_in_dependencies() {
                ConstructorInjectionProvider<ComponentWithFieldInjection> provider =
                        new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);

                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
            }
        }

        @Nested
        class IllegalInjectFields {

            //todo throw exception if field is final
            static class FinalInjectField {
                @Inject
                private final Dependency dependency = null;
            }

            @Test
            public void should_throw_exception_if_inject_field_is_final() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(FinalInjectField.class));
            }
        }
    }

    @Nested
    class MethodInjection {

        @Nested
        class Injection {
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
                ConstructorInjectionProvider<InjectMethodWithNoDependency> provider = new ConstructorInjectionProvider<>(InjectMethodWithNoDependency.class);
                InjectMethodWithNoDependency component =
                        provider.get(context);

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
                ConstructorInjectionProvider<InjectMethodWithDependency> provider = new ConstructorInjectionProvider<>(InjectMethodWithDependency.class);
                InjectMethodWithDependency component =
                        provider.get(context);

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

            static class SubClassWithInjectMethod extends SuperClassWithInjectMethod {
                int subClassCalled;

                @Inject
                public void installSub() {
                    this.subClassCalled = super.superClassCalled + 1;
                }
            }

            @Test
            public void should_inject_dependencies_via_inject_method_from_superclass() {
                ConstructorInjectionProvider<SubClassWithInjectMethod> provider = new ConstructorInjectionProvider<>(SubClassWithInjectMethod.class);
                SubClassWithInjectMethod component =
                        provider.get(context);

                assertEquals(1, component.superClassCalled);
                assertEquals(2, component.subClassCalled);
            }

            static class SubClassOverrideSuperClassWithInject extends SuperClassWithInjectMethod {

                @Inject
                @Override
                public void installSuper() {
                    super.installSuper();
                }

            }

            @Test
            public void should_only_call_once_if_subclass_override_inject_method_with_inejct() {
                ConstructorInjectionProvider<SubClassOverrideSuperClassWithInject> provider = new ConstructorInjectionProvider<>(SubClassOverrideSuperClassWithInject.class);
                SubClassOverrideSuperClassWithInject component =
                        provider.get(context);

                assertEquals(1, component.superClassCalled);
            }

            static class SubClassOverrideSuperClassWithNoInject extends SuperClassWithInjectMethod {
                @Override
                public void installSuper() {
                    super.installSuper();
                }
            }

            @Test
            public void should_not_call_inject_method_if_override_method_no_inject() {
                ConstructorInjectionProvider<SubClassOverrideSuperClassWithNoInject> provider = new ConstructorInjectionProvider<>(SubClassOverrideSuperClassWithNoInject.class);
                SubClassOverrideSuperClassWithNoInject component =
                        provider.get(context);

                assertEquals(0, component.superClassCalled);
            }
        }

        @Nested
        class IllegalInjectMethods {
            //todo include dependencies from inject methods
            @Test
            public void shoudl_include_dependencies_from_inject_methods() {
                ConstructorInjectionProvider<Injection.InjectMethodWithDependency> provider =
                        new ConstructorInjectionProvider<>(Injection.InjectMethodWithDependency.class);

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
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(InjectMethodWithTypeParameter.class));
            }
        }

    }
}
