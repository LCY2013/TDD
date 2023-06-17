package org.fufeng.tdd;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

public interface Context {

    <ComponentType> Optional<ComponentType> get(Ref<ComponentType> ref);

    class Ref<ComponentType> {
        private Type container;
        private Class<ComponentType> component;

        protected void init(Type type) {
            if (type instanceof ParameterizedType containerType) {
                this.container = containerType.getRawType();
                this.component = (Class<ComponentType>) containerType.getActualTypeArguments()[0];
            } else {
                this.component = (Class<ComponentType>) type;
            }
        }

        protected Ref() {
            Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            init(type);
        }

        private Ref(Type type) {
            init(type);
        }

        private Ref(Class<ComponentType> component) {
            init(component);
        }

        public static Ref of(Type type) {
            return new Ref(type);
        }

        public static <ComponentType> Ref<ComponentType> of(Class<ComponentType> type) {
            return new Ref<>(type);
        }

        public Type getContainer() {
            return this.container;
        }

        public Class<ComponentType> getComponent() {
            return this.component;
        }

        public boolean isContainer() {
            return this.container != null;
        }

        public boolean isComponent() {
            return this.container == null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ref ref = (Ref) o;
            return Objects.equals(container, ref.container) && Objects.equals(component, ref.component);
        }

        @Override
        public int hashCode() {
            return Objects.hash(container, component);
        }
    }
}
