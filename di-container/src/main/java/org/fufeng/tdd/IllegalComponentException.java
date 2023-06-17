package org.fufeng.tdd;

//todo different component
public class IllegalComponentException extends RuntimeException {

    private Class<?> component;

    public IllegalComponentException() {
    }

    public <T> IllegalComponentException(Class<T> component) {
        this.component = component;
    }
}
