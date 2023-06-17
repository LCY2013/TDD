package org.fufeng.tdd;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class ComponentRef<ComponentType> {

    private Component component;
    private Type container;

    protected void init(Type type, Annotation qualifier) {
        if (type instanceof ParameterizedType containerType) {
            this.container = containerType.getRawType();
            this.component = new Component((Class<ComponentType>) containerType.getActualTypeArguments()[0], qualifier);
        } else {
            this.component = new Component((Class<ComponentType>) type, qualifier);
        }
    }

    protected ComponentRef() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        init(type, null);
    }

    private ComponentRef(Type type, Annotation qualifier) {
        init(type, qualifier);
    }

    private ComponentRef(Class<ComponentType> component) {
        init(component, null);
    }

    public static ComponentRef of(Type type) {
        return new ComponentRef(type, null);
    }

    public static <ComponentType> ComponentRef<ComponentType> of(Class<ComponentType> type) {
        return new ComponentRef<>(type);
    }

    public static <ComponentType> ComponentRef<ComponentType> of(Class<ComponentType> type, Annotation qualifier) {
        return new ComponentRef<>(type, qualifier);
    }

    public Type getContainer() {
        return this.container;
    }

    public Component component() {
        return this.component;
    }

    public boolean isContainer() {
        return this.container != null;
    }

    public boolean getComponentType() {
        return this.container == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentRef<?> that = (ComponentRef<?>) o;
        return Objects.equals(component, that.component) && Objects.equals(container, that.container);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, container);
    }
}
