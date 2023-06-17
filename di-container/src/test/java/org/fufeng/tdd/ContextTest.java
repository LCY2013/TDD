package org.fufeng.tdd;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;

public class ContextTest {

    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    class TypeBinding {

        //todo: instance
        @Test
        public void should_bind_type_to_a_specific_instance() {
            TestComponent instance = new TestComponent() {
            };

            config.bind(TestComponent.class, instance);

            Context context = config.getContext();
            assertSame(instance, context.get(ComponentRef.of(TestComponent.class)).get());
        }

        @Test
        public void should_return_empty_if_component_not_defined() {
            Context context = config.getContext();
            Optional<TestComponent> component = context.get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isEmpty());
        }

        @ParameterizedTest(name = "supporting {0}")
        @MethodSource
        public void should_bind_type_to_an_injectable_component(Class<? extends TestComponent> componentType) {
            Dependency dependency = new Dependency() {
            };

            config.bind(Dependency.class, dependency);
            config.bind(TestComponent.class, componentType);

            Context context = config.getContext();
            Optional<TestComponent> component = context.get(ComponentRef.of(TestComponent.class));

            assertTrue(component.isPresent());
            assertSame(dependency, component.get().dependency());
        }

        public static Stream<Arguments> should_bind_type_to_an_injectable_component() {
            return Stream.of(Arguments.of(Named.of("Constructor Injection", ConstructorInjection.class)),
                    Arguments.of(Named.of("Field Injection", FieldInjection.class)),
                    Arguments.of(Named.of("Method Injection", MethodInjection.class)));
        }

        static class InjectConstructor implements TestComponent {

            @Inject
            public InjectConstructor() {
            }
        }

        static class ConstructorInjection implements TestComponent {

            private Dependency dependency;

            @Inject
            public ConstructorInjection(Dependency dependency) {
                this.dependency = dependency;
            }

            @Override
            public <T> T dependency() {
                return (T) dependency;
            }
        }

        static class FieldInjection implements TestComponent {

            @Inject
            private Dependency dependency;

            @Override
            public <T> T dependency() {
                return (T) dependency;
            }
        }

        static class MethodInjection implements TestComponent {

            private Dependency dependency;

            @Inject
            public void install(Dependency dependency) {
                this.dependency = dependency;
            }

            @Override
            public <T> T dependency() {
                return (T) dependency;
            }
        }

        @Test
        public void should_retrieve_empty_for_unbind_type() {
            Context context = config.getContext();
            Optional<TestComponent> component = context.get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isEmpty());
        }

        //Context
        //todo could get Provider<T> from context context
        @Test
        public void should_retrieve_bind_type_as_provider() {
            TestComponent component = new TestComponent() {
            };

            config.bind(TestComponent.class, component);

            Context context = config.getContext();

            Provider<TestComponent> provider = context.get(new ComponentRef<Provider<TestComponent>>() {
            }).get();

            assertSame(component, provider.get());
        }

        @Test
        public void should_not_retrieve_bind_type_as_unsupported_container() {
            TestComponent component = new TestComponent() {
            };

            config.bind(TestComponent.class, component);

            Context context = config.getContext();

            assertFalse(context.get(new ComponentRef<List<TestComponent>>() {
            }).isPresent());
        }

        @Test
        public void should_not_get_generic_type_from_self_class() {
            Provider<TestComponent> provider = () -> new TestComponent() {
            };
            Type genericSuperclass = provider.getClass().getGenericSuperclass();

            assertEquals(Object.class, genericSuperclass);
        }

        @Test
        public void should_get_generic_type_from_class_field() throws NoSuchFieldException {
            Field componentProviderField = FieldGenericType.class.getDeclaredField("componentProvider");

            ParameterizedType genericType = (ParameterizedType) componentProviderField.getGenericType();
            //assertEquals(Provider<Component>.class, genericType);

            Type type = genericType.getActualTypeArguments()[0];
            assertEquals(TestComponent.class, type);
        }

        static class FieldGenericType {
            Provider<TestComponent> componentProvider;

        }

        @Test
        public void should_get_generic_type_from_class_method() throws NoSuchMethodException {
            Method method =
                    Arrays.stream(MethodGenericType.class.getDeclaredMethods()).
                            filter(m -> m.getName().equals("install")).findAny().get();

            Type type = method.getParameters()[0].getParameterizedType();

            if (type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getActualTypeArguments()[0];
            }

            assertEquals(TestComponent.class, type);
        }

        static class MethodGenericType {

            public void install(Provider<TestComponent> provider) {

            }

        }

        @Nested
        class WithQualifier {
            //todo binding component with qualifier
            @Test
            public void should_bind_instance_with_qualifier() {
                TestComponent instance = new TestComponent() {
                };

                config.bind(TestComponent.class, instance, new NameLiteral("ChoseOne"));

                Context context = config.getContext();
                TestComponent component = context.get(ComponentRef.of(TestComponent.class, new NameLiteral("ChoseOne"))).get();
                assertSame(component, instance);
            }

            @Test
            public void should_bind_component_with_qualifier() {
                Dependency dependency = new Dependency() {
                };

                config.bind(Dependency.class, dependency);
                config.bind(TestComponent.class, ConstructorInjection.class, new NameLiteral("ChoseOne"));

                Context context = config.getContext();
                TestComponent component = context.get(ComponentRef.of(TestComponent.class, new NameLiteral("ChoseOne"))).get();
                assertSame(component.dependency(), dependency);
            }

            //todo binding component with multi qualifier
            @Test
            public void should_bind_instance_with_multi_qualifiers() {
                TestComponent instance = new TestComponent() {
                };

                config.bind(TestComponent.class, instance, new NameLiteral("ChoseOne"), new SkywalkerLiteral());

                Context context = config.getContext();
                TestComponent choseOne = context.get(ComponentRef.of(TestComponent.class, new NameLiteral("ChoseOne"))).get();
                TestComponent skywalker = context.get(ComponentRef.of(TestComponent.class, new SkywalkerLiteral())).get();
                assertSame(choseOne, instance);
                assertSame(skywalker, instance);
            }

            @Test
            public void should_bind_component_with_multi_qualifiers() {
                Dependency dependency = new Dependency() {
                };

                config.bind(Dependency.class, dependency);
                config.bind(TestComponent.class, ConstructorInjection.class, new NameLiteral("ChoseOne"), new SkywalkerLiteral());

                Context context = config.getContext();
                TestComponent choseOne = context.get(ComponentRef.of(TestComponent.class, new NameLiteral("ChoseOne"))).get();
                TestComponent skywalker = context.get(ComponentRef.of(TestComponent.class, new SkywalkerLiteral())).get();
                assertSame(choseOne.dependency(), dependency);
                assertSame(skywalker .dependency(), dependency);
            }

            //todo binding illegal component with illegal qualifier
            @Test
            public void should_throw_exception_if_illegal_qualifier_given_to_instance() {
                TestComponent instance = new TestComponent() {
                };

                assertThrows(IllegalComponentException.class, () -> config.bind(TestComponent.class, instance, new TestLiteral()));
            }

            @Test
            public void should_throw_exception_if_illegal_qualifier_given_to_component() {
                assertThrows(IllegalComponentException.class, () ->  config.bind(InjectConstructor.class, InjectConstructor.class, new TestLiteral()));
            }
        }

    }


    @Nested
    class DependencyCheck {

        //todo: dependencies not exist

        @ParameterizedTest
        @MethodSource
        public void should_throw_exception_if_dependency_not_found(Class<? extends TestComponent> component) {
            config.bind(TestComponent.class, component);
            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> {
                Context context = config.getContext();
                context.get(ComponentRef.of(TestComponent.class));
            });

            assertEquals(Dependency.class, exception.getDependency());
            assertEquals(TestComponent.class, exception.getComponent());
        }

        public static Stream<Arguments> should_throw_exception_if_dependency_not_found() {
            return Stream.of(Arguments.of(Named.of("Constructor Injection", MissingDependencyConstructor.class)),
                    Arguments.of(Named.of("Field Injection", MissingDependencyField.class)),
                    Arguments.of(Named.of("Method Injection", MissingDependencyMethod.class)),
                    Arguments.of(Named.of("Provider Constructor Injection", MissingDependencyProviderConstructor.class)),
                    Arguments.of(Named.of("Provider Field Injection", MissingDependencyProviderField.class)),
                    Arguments.of(Named.of("Provider Method Injection", MissingDependencyProviderMethod.class))
            );
        }

        static class MissingDependencyConstructor implements TestComponent {
            @Inject
            public MissingDependencyConstructor(Dependency dependency) {
            }
        }

        static class MissingDependencyProviderConstructor implements TestComponent {
            @Inject
            public MissingDependencyProviderConstructor(Provider<Dependency> dependency) {
            }
        }

        static class MissingDependencyField implements TestComponent {
            @Inject
            Dependency dependency;
        }

        static class MissingDependencyProviderField implements TestComponent {
            @Inject
            Provider<Dependency> dependency;
        }

        static class MissingDependencyMethod implements TestComponent {
            @Inject
            public void install(Dependency dependency) {
            }
        }

        static class MissingDependencyProviderMethod implements TestComponent {
            @Inject
            public void install(Provider<Dependency> dependency) {
            }
        }


        @ParameterizedTest(name = "cyclic dependency between {0} and {1}")
        @MethodSource
        public void should_throw_exception_if_cyclic_dependencies_found(
                Class<? extends TestComponent> component,
                Class<? extends Dependency> dependency) {
            config.bind(TestComponent.class, component);
            config.bind(Dependency.class, dependency);

            CyclicDependenciesException exception =
                    assertThrowsExactly(CyclicDependenciesException.class, () -> {
                        Context context = config.getContext();
                        context.get(ComponentRef.of(TestComponent.class));
                    });

            Set<Class<?>> classes = exception.getComponents();

            assertEquals(2, classes.size());
            assertTrue(classes.contains(TestComponent.class));
            assertTrue(classes.contains(Dependency.class));
        }


        public static Stream<Arguments> should_throw_exception_if_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named component : List.of(Named.of("Constructor Injection", CyclicComponentConstructor.class),
                    Named.of("Field Injection", CyclicComponentField.class),
                    Named.of("Method Injection", CyclicComponentMethod.class))) {
                for (Named dependency : List.of(Named.of("Constructor Injection", CyclicDependencyConstructor.class),
                        Named.of("Field Injection", CyclicDependencyField.class),
                        Named.of("Method Injection", CyclicDependencyMethod.class))) {
                    arguments.add(Arguments.of(component, dependency));
                }
            }
            return arguments.stream();
        }

        static class CyclicComponentConstructor implements TestComponent {

            @Inject
            public CyclicComponentConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentField implements TestComponent {

            @Inject
            Dependency dependency;

        }

        static class CyclicComponentMethod implements TestComponent {

            @Inject
            public void install(Dependency dependency) {

            }
        }

        static class CyclicDependencyConstructor implements Dependency {

            @Inject
            public CyclicDependencyConstructor(TestComponent component) {
            }
        }

        static class CyclicDependencyField implements Dependency {

            @Inject
            TestComponent component;
        }

        static class CyclicDependencyMethod implements Dependency {

            @Inject
            public void install(TestComponent component) {

            }
        }

        @ParameterizedTest(name = "indirect cyclic dependency between {0}, {1} and {2}")
        @MethodSource
        public void should_throw_exception_if_transitive_cyclic_dependencies_found(
                Class<? extends TestComponent> component,
                Class<? extends Dependency> dependency,
                Class<? extends AnotherDependency> anotherDependency) {
            config.bind(TestComponent.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

            CyclicDependenciesException exception =
                    assertThrowsExactly(CyclicDependenciesException.class, () -> {
                        Context context = config.getContext();
                        context.get(ComponentRef.of(TestComponent.class));
                    });

            Set<Class<?>> classes = exception.getComponents();

            assertEquals(3, classes.size());
            assertTrue(classes.contains(TestComponent.class));
            assertTrue(classes.contains(Dependency.class));
            assertTrue(classes.contains(AnotherDependency.class));
        }

        public static Stream<Arguments> should_throw_exception_if_transitive_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named component : List.of(Named.of("Constructor Injection", CyclicComponentInjectionConstructor.class),
                    Named.of("Field Injection", CyclicComponentInjectionField.class),
                    Named.of("Method Injection", CyclicComponentInjectionMethod.class))) {
                for (Named dependency : List.of(Named.of("Constructor Injection", CyclicDependencyInjectionConstructor.class),
                        Named.of("Field Injection", CyclicDependencyInjectionField.class),
                        Named.of("Method Injection", CyclicDependencyInjectionMethod.class))) {
                    for (Named anotherDependency : List.of(Named.of("Constructor Injection", CyclicAnotherDependencyInjectionConstructor.class),
                            Named.of("Field Injection", CyclicAnotherDependencyInjectionField.class),
                            Named.of("Method Injection", CyclicAnotherDependencyInjectionMethod.class))) {
                        arguments.add(Arguments.of(component, dependency, anotherDependency));
                    }
                }
            }
            return arguments.stream();
        }

        static class CyclicComponentInjectionConstructor implements TestComponent {

            @Inject
            public CyclicComponentInjectionConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentInjectionField implements TestComponent {

            @Inject
            Dependency dependency;

        }

        static class CyclicComponentInjectionMethod implements TestComponent {

            @Inject
            public void install(Dependency dependency) {

            }
        }

        static class CyclicDependencyInjectionConstructor implements Dependency {

            @Inject
            public CyclicDependencyInjectionConstructor(AnotherDependency anotherDependency) {
            }
        }

        static class CyclicDependencyInjectionField implements Dependency {

            @Inject
            AnotherDependency anotherDependency;
        }

        static class CyclicDependencyInjectionMethod implements Dependency {

            @Inject
            public void install(AnotherDependency anotherDependency) {

            }
        }

        static class CyclicAnotherDependencyInjectionConstructor implements AnotherDependency {

            @Inject
            public CyclicAnotherDependencyInjectionConstructor(TestComponent component) {
            }
        }

        static class CyclicAnotherDependencyInjectionField implements AnotherDependency {

            @Inject
            TestComponent component;
        }

        static class CyclicAnotherDependencyInjectionMethod implements AnotherDependency {

            @Inject
            public void install(TestComponent component) {

            }
        }

        static class CyclicDependencyProviderConstructor implements Dependency {

            @Inject
            public CyclicDependencyProviderConstructor(Provider<TestComponent> component) {
            }
        }

        @Test
        public void should_not_throw_exception_if_cyclic_dependency_via_provider() {
            config.bind(TestComponent.class, CyclicComponentInjectionConstructor.class);
            config.bind(Dependency.class, CyclicDependencyProviderConstructor.class);

            Context context = config.getContext();
            assertTrue(context.get(ComponentRef.of(TestComponent.class)).isPresent());
        }


        @Test
        public void should_throw_exception_when_get_context_if_dependency_not_found() {
            config.bind(TestComponent.class, ComponentWithInjectConstructor.class);
            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertEquals(Dependency.class, exception.getDependency());
            assertEquals(TestComponent.class, exception.getComponent());
        }

        @Test
        public void should_throw_exception_if_transitive_dependency_not_found() {
            config.bind(TestComponent.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyWithInjectConstructor.class);
            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> {
                Context context = config.getContext();
                context.get(ComponentRef.of(TestComponent.class));
            });

            assertEquals(String.class, exception.getDependency());
            assertEquals(Dependency.class, exception.getComponent());
        }

        @Test
        public void should_throw_details_exception_if_cyclic_dependencies_found() {
            config.bind(TestComponent.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependencyWithInjectConstructor.class);

            CyclicDependenciesException exception = assertThrows(CyclicDependenciesException.class, () -> {
                Context context = config.getContext();
                context.get(ComponentRef.of(TestComponent.class));
            });

            Set<Class<?>> classes = exception.getComponents();
            assertEquals(2, classes.size());
            assertTrue(classes.contains(TestComponent.class));
            assertTrue(classes.contains(Dependency.class));
        }

        @Test
        public void should_throw_exception_when_get_context_if_transitive_cyclic_dependencies_found() {
            config.bind(TestComponent.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

            assertThrowsExactly(CyclicDependenciesException.class, () -> config.getContext());
        }

        @Test
        public void should_throw_details_exception_if_transitive_cyclic_dependencies_found() {
            config.bind(TestComponent.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

            CyclicDependenciesException exception = assertThrows(CyclicDependenciesException.class, () -> {
                Context context = config.getContext();
                context.get(ComponentRef.of(TestComponent.class));
            });

            Set<Class<?>> classes = exception.getComponents();
            assertEquals(3, classes.size());
            assertTrue(classes.contains(TestComponent.class));
            assertTrue(classes.contains(Dependency.class));
            assertTrue(classes.contains(AnotherDependency.class));
        }

        @Test
        public void should_throw_details_exception_when_get_context_if_transitive_cyclic_dependencies_found() {
            config.bind(TestComponent.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

            CyclicDependenciesException exception = assertThrows(CyclicDependenciesException.class, () -> config.getContext());

            Set<Class<?>> classes = exception.getComponents();
            assertEquals(3, classes.size());
            assertTrue(classes.contains(TestComponent.class));
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

        @Nested
        class WithQualifier {
            //todo dependency missing if qualifier not match
            //todo check cyclic dependencies with qualifier
        }
    }

}

record NameLiteral(String value) implements jakarta.inject.Named {

    @Override
    public Class<? extends Annotation> annotationType() {
        return jakarta.inject.Named.class;
    }

}

@Documented
@Retention(RUNTIME)
@Qualifier
@interface Skywalker {

}

record SkywalkerLiteral() implements Skywalker {

    @Override
    public Class<? extends Annotation> annotationType() {
        return Skywalker.class;
    }

}

record TestLiteral() implements Test {
    @Override
    public Class<? extends Annotation> annotationType() {
        return Test.class;
    }
}
