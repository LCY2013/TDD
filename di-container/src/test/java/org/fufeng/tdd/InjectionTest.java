package org.fufeng.tdd;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Nested
public class InjectionTest {
    Dependency dependency = mock(Dependency.class);
    Provider<Dependency> dependencyProvider = mock(Provider.class);
    ParameterizedType dependencyProviderType;

    Context context = mock(Context.class);

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        when(context.get(eq(Context.Ref.of(Dependency.class)))).thenReturn(Optional.of(dependency));

        dependencyProviderType = (ParameterizedType) InjectionTest.class.getDeclaredField("dependencyProvider").getGenericType();
        when(context.get(eq(Context.Ref.of(dependencyProviderType)))).thenReturn(Optional.of(dependencyProvider));
    }

    @Nested
    class ConstructorInjection {

        @Nested
        class Injection {
            // happy path
            //todo: no args constructor
            @Test
            public void should_call_default_constructor_if_no_inject_constructor() {
                InjectionProvider<ComponentWithDefaultConstructor> provider = new InjectionProvider<>(ComponentWithDefaultConstructor.class);
                Component component = provider.get(context);

                assertNotNull(component);
            }

            //todo: with dependencies
            @Test
            public void should_inject_dependency_via_inject_constructor() {
                ComponentWithInjectConstructor component =
                        new InjectionProvider<>(ComponentWithInjectConstructor.class).get(context);


                assertNotNull(component);
                assertSame(dependency, component.getDependency());
            }

            @Test
            public void should_include_dependency_from_inject_constructor() {
                InjectionProvider<ComponentWithInjectConstructor> provider = new InjectionProvider<>(ComponentWithInjectConstructor.class);
                assertArrayEquals(new Context.Ref[]{Context.Ref.of(Dependency.class)}, provider.getDependencies().toArray());
            }

            //todo include dependency type from inject constructor
            @Test
            public void should_include_provider_type_from_inject_constructor() {
                InjectionProvider<ProviderInjectConstructor> provider = new InjectionProvider<>(ProviderInjectConstructor.class);
                assertArrayEquals(new Context.Ref[]{Context.Ref.of(dependencyProviderType)}, provider.getDependencies().toArray());
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
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(AbstractComponent.class));
            }

            //todo: interface
            @Test
            public void should_throw_exception_if_component_is_interface() {
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(Component.class));
            }

            //sad path

            //todo: multi inject constructors
            @Test
            public void should_throw_exception_if_multi_inject_constructor_provided() {
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(ComponentWithMultiInjectConstructor.class));
            }

            //todo: no default constructor and inject constructor
            @Test
            public void should_throw_exception_if_no_inject_nor_default_constructor_provided() {
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(ComponentWithNoInjectConstructorNorDefaultConstructor.class));
            }
        }

        //todo support inject constructor
        static class ProviderInjectConstructor {
            private final Provider<Dependency> dependency;

            @Inject
            public ProviderInjectConstructor(Provider<Dependency> dependency) {
                this.dependency = dependency;
            }
        }

        @Test
        public void should_inject_provider_via_inject_constructor() {
            ProviderInjectConstructor injectConstructor = new InjectionProvider<>(ProviderInjectConstructor.class).get(context);
            assertSame(dependencyProvider, injectConstructor.dependency);
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
                InjectionProvider<SubclassWithFieldInjection> provider = new InjectionProvider<>(SubclassWithFieldInjection.class);
                SubclassWithFieldInjection component =
                        provider.get(context);


                assertSame(dependency, component.dependency);
            }

            //todo inject field
            @Test
            public void should_inject_dependency_via_field() {
                InjectionProvider<ComponentWithFieldInjection> provider = new InjectionProvider<>(ComponentWithFieldInjection.class);
                ComponentWithFieldInjection component =
                        provider.get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            public void should_create_component_with_inject_field() {
                Dependency dependency = mock(Dependency.class);
                Context context = mock(Context.class);
                when(context.get(eq(Context.Ref.of(Dependency.class)))).thenReturn(Optional.of(dependency));

                InjectionProvider<ComponentWithFieldInjection> provider =
                        new InjectionProvider<>(ComponentWithFieldInjection.class);
                ComponentWithFieldInjection component = provider.get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            public void should_include_dependency_from_field_dependencies_() {
                InjectionProvider<ComponentWithFieldInjection> provider =
                        new InjectionProvider<>(ComponentWithFieldInjection.class);

                assertArrayEquals(new Context.Ref[]{Context.Ref.of(Dependency.class)}, provider.getDependencies().toArray(Context.Ref[]::new));
            }

            @Test
            public void should_include_dependencies_from_field_dependency_in_dependencies() {
                InjectionProvider<ComponentWithFieldInjection> provider =
                        new InjectionProvider<>(ComponentWithFieldInjection.class);

                assertArrayEquals(new Context.Ref[]{Context.Ref.of(Dependency.class)}, provider.getDependencies().toArray(Context.Ref[]::new));
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
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(FinalInjectField.class));
            }
        }

        //todo support inject field
        static class ProviderInjectField {

            @Inject
            private Provider<Dependency> dependency;
        }

        @Test
        public void should_inject_provider_via_inject_constructor() {
            ProviderInjectField injectConstructor = new InjectionProvider<>(ProviderInjectField.class).get(context);
            assertSame(dependencyProvider, injectConstructor.dependency);
        }

        //todo include dependency type from inject field
        @Test
        public void should_include_provider_type_from_inject_field() {
            InjectionProvider<ProviderInjectField> provider = new InjectionProvider<>(ProviderInjectField.class);
            assertArrayEquals(new Context.Ref[]{Context.Ref.of(dependencyProviderType)}, provider.getDependencies().toArray());
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
                InjectionProvider<InjectMethodWithNoDependency> provider = new InjectionProvider<>(InjectMethodWithNoDependency.class);
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
                InjectionProvider<InjectMethodWithDependency> provider = new InjectionProvider<>(InjectMethodWithDependency.class);
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
                InjectionProvider<SubClassWithInjectMethod> provider = new InjectionProvider<>(SubClassWithInjectMethod.class);
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
                InjectionProvider<SubClassOverrideSuperClassWithInject> provider = new InjectionProvider<>(SubClassOverrideSuperClassWithInject.class);
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
                InjectionProvider<SubClassOverrideSuperClassWithNoInject> provider = new InjectionProvider<>(SubClassOverrideSuperClassWithNoInject.class);
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
                InjectionProvider<Injection.InjectMethodWithDependency> provider =
                        new InjectionProvider<>(Injection.InjectMethodWithDependency.class);

                assertArrayEquals(new Context.Ref[]{Context.Ref.of(Dependency.class)}, provider.getDependencies().toArray(Context.Ref[]::new));
            }

            //todo throw exception if type parameter defined
            static class InjectMethodWithTypeParameter {

                @Inject
                <T> void install() {

                }
            }

            @Test
            public void should_throw_exception_if_inject_method_has_type_parameter() {
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(InjectMethodWithTypeParameter.class));
            }
        }

        //todo support inject method
        static class ProviderInjectMethod {
            private Provider<Dependency> dependency;

            @Inject
            public void install(Provider<Dependency> dependency) {
                this.dependency = dependency;
            }
        }

        @Test
        public void should_inject_provider_via_inject_constructor() {
            ProviderInjectMethod injectConstructor = new InjectionProvider<>(ProviderInjectMethod.class).get(context);
            assertSame(dependencyProvider, injectConstructor.dependency);
        }

        //todo include dependency type from inject method
        @Test
        public void should_include_provider_type_from_inject_method() {
            InjectionProvider<ProviderInjectMethod> provider = new InjectionProvider<>(ProviderInjectMethod.class);
            assertArrayEquals(new Context.Ref[]{Context.Ref.of(dependencyProviderType)}, provider.getDependencies().toArray());
        }
    }
}
