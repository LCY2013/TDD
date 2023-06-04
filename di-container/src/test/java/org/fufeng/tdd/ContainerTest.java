package org.fufeng.tdd;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest {

    interface Component {

    }

    static class ComponentWithDefaultConstructor implements Component {

        public ComponentWithDefaultConstructor() {
        }

    }

    @Nested
    class ComponentConstruct {
        //todo: instance
        @Test
        public void should_bind_type_to_a_specific_instance() {
            Context context = new Context();

            Component instance = new Component() {};

            context.bind(Component.class, instance);

            assertSame(instance, context.get(Component.class));
        }

        //todo: abstract class
        //todo: interface
        @Nested
        class ConstructorInjection {
            //todo: no args constructor
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                Context context = new Context();

                context.bind(Component.class, ComponentWithDefaultConstructor.class);

                Component instance = context.get(Component.class);

                assertNotNull(instance);
                assertTrue(instance instanceof ComponentWithDefaultConstructor);
            }

            //todo: with dependencies
            //todo: A -> B -> C
        }

        @Nested
        class FieldInjection {

        }

        @Nested
        class MethodInjection {

        }
    }

    @Nested
    class DependenciesSelection {

    }

    @Nested
    class LifecycleManagement {

    }

}
