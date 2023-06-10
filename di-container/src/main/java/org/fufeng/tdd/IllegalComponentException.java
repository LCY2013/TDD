package org.fufeng.tdd;

public class IllegalComponentException extends RuntimeException {

    private Class<?> component;

    public IllegalComponentException() {
    }

    public <T> IllegalComponentException(Class<T> component) {
        this.component = component;
    }
}
