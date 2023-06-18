package org.fufeng.tdd;

public class DependencyNotFoundException extends RuntimeException {

    private Component component;
    private Component dependency;

    public DependencyNotFoundException(Component componentComponent, Component dependencyComponent) {
        this.component = componentComponent;
        this.dependency = dependencyComponent;
    }

    public Component getDependency() {
        return dependency;
    }

    public Component getComponent() {
        return component;
    }
}
