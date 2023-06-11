package org.fufeng.tdd;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

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
            Component instance = new Component() {
                @Override
                public <T> T dependency() {
                    return null;
                }
            };

            config.bind(Component.class, instance);

            assertSame(instance, config.getContext().get(Component.class).get());
        }

        @Test
        public void should_return_empty_if_component_not_defined() {
            Optional<Component> component = config.getContext().get(Component.class);
            assertTrue(component.isEmpty());
        }

        @ParameterizedTest(name = "supporting {0}")
        @MethodSource
        public void should_bind_type_to_an_injectable_component(Class<? extends Component> componentType) {
            Dependency dependency = new Dependency() {
            };

            config.bind(Dependency.class, dependency);
            config.bind(Component.class, componentType);

            Optional<Component> component = config.getContext().get(Component.class);

            assertTrue(component.isPresent());
            assertSame(dependency, component.get().dependency());
        }

        public static Stream<Arguments> should_bind_type_to_an_injectable_component() {
            return Stream.of(Arguments.of(Named.of("Constructor Injection", ConstructorInjection.class)),
                    Arguments.of(Named.of("Field Injection", FieldInjection.class)),
                    Arguments.of(Named.of("Method Injection", MethodInjection.class)));
        }

        static class ConstructorInjection implements Component {

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

        static class FieldInjection implements Component {

            @Inject
            private Dependency dependency;

            @Override
            public <T> T dependency() {
                return (T) dependency;
            }
        }

        static class MethodInjection implements Component {

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
            Optional<Component> component = config.getContext().get(Component.class);
            assertTrue(component.isEmpty());
        }

        //Context
        //todo could get Provider<T> from context context
        @Test
        public void should_retrieve_bind_type_as_provider() {
            Component component = new Component() {
            };

            config.bind(Component.class, component);

            Context context = config.getContext();

            ParameterizedType type = (ParameterizedType) new TypeLiteral<Provider<Component>>() {
            }.getType();
            //assertEquals(Provider.class, type.getRawType());
            //assertEquals(Component.class, type.getActualTypeArguments()[0]);

            Provider<Component> provider = (Provider<Component>) context.get(type).get();
            assertSame(component, provider.get());
        }

        @Test
        public void should_not_retrieve_bind_type_as_unsupported_container() {
            Component component = new Component() {
            };

            config.bind(Component.class, component);

            Context context = config.getContext();

            ParameterizedType type = (ParameterizedType) new TypeLiteral<List<Component>>() {
            }.getType();

            assertFalse(context.get(type).isPresent());
        }

        static abstract class TypeLiteral<T> {
            public Type getType() {
                return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            }
        }

        @Test
        public void should_not_get_generic_type_from_self_class() {
            Provider<Component> provider = () -> new Component() {
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
            assertEquals(Component.class, type);
        }

        static class FieldGenericType {
            Provider<Component> componentProvider;

        }

        @Test
        public void should_get_generic_type_from_class_method() throws NoSuchMethodException {
            Method method =
                    Arrays.stream(MethodGenericType.class.getDeclaredMethods()).
                            filter(m -> m.getName().equals("install")).findAny().get();

            Type type = method.getParameters()[0].getParameterizedType();

            if (type instanceof ParameterizedType) {
                type = ((ParameterizedType)type).getActualTypeArguments()[0];
            }

            assertEquals(Component.class, type);
        }

        static class MethodGenericType {

            public void install(Provider<Component> provider) {

            }

        }

    }

    @Nested
    class DependencyCheck {

        //todo: dependencies not exist

        @ParameterizedTest
        @MethodSource
        public void should_throw_exception_if_dependency_not_found(Class<? extends Component> component) {
            config.bind(Component.class, ComponentWithInjectConstructor.class);
            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext().get(Component.class));

            assertEquals(Dependency.class, exception.getDependency());
            assertEquals(Component.class, exception.getComponent());
        }

        public static Stream<Arguments> should_throw_exception_if_dependency_not_found() {
            return Stream.of(Arguments.of(Named.of("Constructor Injection", MissingDependencyConstructor.class)),
                    Arguments.of(Named.of("Field Injection", MissingDependecyField.class)),
                    Arguments.of(Named.of("Method Injection", MissingDependecyMethod.class)));
        }

        static class MissingDependencyConstructor implements Component {
            @Inject
            public MissingDependencyConstructor(Dependency dependency) {
            }
        }

        static class MissingDependecyField implements Component {
            @Inject
            Dependency dependency;
        }

        static class MissingDependecyMethod implements Component {
            @Inject
            public void install(Dependency dependency) {
            }
        }


        @ParameterizedTest(name = "cyclic dependency between {0} and {1}")
        @MethodSource
        public void should_throw_exception_if_cyclic_dependencies_found(
                Class<? extends Component> component,
                Class<? extends Dependency> dependency) {
            config.bind(Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependencyWithInjectConstructor.class);

            CyclicDependenciesException exception =
                    assertThrowsExactly(CyclicDependenciesException.class, () -> config.getContext().get(Component.class));

            Set<Class<?>> classes = exception.getComponents();

            assertEquals(2, classes.size());
            assertTrue(classes.contains(Component.class));
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

        static class CyclicComponentConstructor implements Component {

            @Inject
            public CyclicComponentConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentField implements Component {

            @Inject
            Dependency dependency;

        }

        static class CyclicComponentMethod implements Component {

            @Inject
            public void install(Dependency dependency) {

            }
        }

        static class CyclicDependencyConstructor implements Dependency {

            @Inject
            public CyclicDependencyConstructor(Component component) {
            }
        }

        static class CyclicDependencyField implements Dependency {

            @Inject
            Component component;
        }

        static class CyclicDependencyMethod implements Dependency {

            @Inject
            public void install(Component component) {

            }
        }

        @ParameterizedTest(name = "indirect cyclic dependency between {0}, {1} and {2}")
        @MethodSource
        public void should_throw_exception_if_transitive_cyclic_dependencies_found(
                Class<? extends Component> component,
                Class<? extends Dependency> dependency,
                Class<? extends AnotherDependency> anotherDependency) {
            config.bind(Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

            CyclicDependenciesException exception =
                    assertThrowsExactly(CyclicDependenciesException.class, () -> config.getContext().get(Component.class));

            Set<Class<?>> classes = exception.getComponents();

            assertEquals(3, classes.size());
            assertTrue(classes.contains(Component.class));
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

        static class CyclicComponentInjectionConstructor implements Component {

            @Inject
            public CyclicComponentInjectionConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentInjectionField implements Component {

            @Inject
            Dependency dependency;

        }

        static class CyclicComponentInjectionMethod implements Component {

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
            public CyclicAnotherDependencyInjectionConstructor(Component component) {
            }
        }

        static class CyclicAnotherDependencyInjectionField implements AnotherDependency {

            @Inject
            Component component;
        }

        static class CyclicAnotherDependencyInjectionMethod implements AnotherDependency {

            @Inject
            public void install(Component component) {

            }
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