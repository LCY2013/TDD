package org.fufeng.tdd;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.internal.SingletonScope;
import jakarta.inject.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentConstructionTest {

    interface Car {
        Engine getEngine();
    }

    interface Engine {
        String getName();
    }

    @Named("V6")
    static class V6Engine implements Engine {
        @Override
        public String getName() {
            return "V6";
        }
    }

    @Named("V8")
    static class V8Engine implements Engine {
        @Override
        public String getName() {
            return "V8";
        }
    }

    @Nested
    class ConstructorInjection {
        static class CarInjectConstructor implements Car {

            private final Engine engine;

            @Inject
            public CarInjectConstructor(Engine engine) {
                this.engine = engine;
            }

            @Override
            public Engine getEngine() {
                return engine;
            }
        }

        @Test
        public void constructor_injection() {
            AbstractModule module = new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Engine.class).to(V8Engine.class);
                    bind(Car.class).to(CarInjectMethod.class);
                }
            };

            Injector injector = Guice.createInjector(module);

            Car car = injector.getInstance(Car.class); // new Car() postCreation(); Start(); Stop();

            assertEquals("V8", car.getEngine().getName());
        }

        interface Window {

        }

        interface Wheel {

        }

        static class CarWithInjectPoints implements Car {

            @Inject
            private Window window;

            private final Engine engine;
            private Wheel wheel;

            @Inject
            public CarWithInjectPoints(Engine engine) {
                this.engine = engine;
            }

            @Inject
            public void injectWheel(Wheel wheel) {
                this.wheel = wheel;
            }

            @Override
            public Engine getEngine() {
                return engine;
            }
        }

        static class CarInjectField implements Car {
            @Inject
            private Engine engine;

            public Engine getEngine() {
                return engine;
            }
        }

        static class CarInjectMethod implements Car {

            private Engine engine;

            @Override
            public Engine getEngine() {
                return engine;
            }

            @Inject
            public void injectMethod(Engine engine) {
                this.engine = engine;
            }
        }
    }

    @Nested
    class DependencySelection {
        static class A {

            private final Provider<B> b;

            @Inject
            public A(Provider<B> b) {
                this.b = b;
            }

            public B getB() {
                return b.get();
            }
        }

        static class B {

            private final A a;

            @Inject
            public B(A a) {
                this.a = a;
            }

            public A getA() {
                return a;
            }
        }

        /**
         *  JSR330 提供解决循环依赖的办法：{@link Provider}, 通过间接依赖解决循环注入问题
         */
        @Test
        public void cyclic_dependencies() {
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                   bind(A.class);
                   bind(B.class);
                }
            });

            A a = injector.getInstance(A.class);
            assertNotNull(a.getB());
        }

        static class V8Car implements Car {

            /**
             * JSR330 提供的注入tag能力
             */
            @Inject
            @Named("V8")
            private Engine engine;

            @Override
            public Engine getEngine() {
                return engine;
            }
        }

        record NameLiteral(String value) implements Named {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Named.class;
            }
        }

        @Test
        public void selection() {
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    //bind(Engine.class).annotatedWith(Named.class).to(V8Engine.class);
                    bind(Engine.class).annotatedWith(new NameLiteral("V8")).to(V8Engine.class);
                    bind(Engine.class).annotatedWith(new NameLiteral("V6")).to(V6Engine.class);
                    bind(Car.class).to(V8Car.class);
                }
            });

            Car car = injector.getInstance(Car.class);
            assertEquals("V8", car.getEngine().getName());
        }

        @Qualifier
        @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        public @interface Luxury {

        }

        record LuxuryLiteral() implements Luxury {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Luxury.class;
            }
        }

        static class LuxuryCar implements Car {

            @Inject
            @Luxury
            private Engine engine;

            @Override
            public Engine getEngine() {
                return engine;
            }

        }

        @Test
        public void selectionByCustomAnnotation() {
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Engine.class).annotatedWith(new LuxuryLiteral()).to(V8Engine.class);
                    bind(Engine.class).annotatedWith(new NameLiteral("V8")).to(V8Engine.class);
                    bind(Engine.class).annotatedWith(new NameLiteral("V6")).to(V6Engine.class);
                    bind(Car.class).to(LuxuryCar.class);
                }
            });

            Car car = injector.getInstance(Car.class);
            assertEquals("V8", car.getEngine().getName());
        }

    }

    @Nested
    public class ContextInScope {

        @Test
        public void singleton() {
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    //bindScope(BatchScoped.class, new SingletonScope());
                    bind(Engine.class).annotatedWith(new DependencySelection.LuxuryLiteral()).to(V8Engine.class).in(Singleton.class);
                    bind(Engine.class).annotatedWith(new DependencySelection.NameLiteral("V8")).to(V8Engine.class);
                    bind(Engine.class).annotatedWith(new DependencySelection.NameLiteral("V6")).to(V6Engine.class);
                    bind(Car.class).to(DependencySelection.LuxuryCar.class);
                }
            });

            Car car1 = injector.getInstance(Car.class);
            Car car2 = injector.getInstance(Car.class);

            assertNotSame(car1, car2);

            assertSame(car1.getEngine(), car2.getEngine());
        }

        @Scope
        @Target({ElementType.TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        public @interface BatchScoped {}

    }




}
