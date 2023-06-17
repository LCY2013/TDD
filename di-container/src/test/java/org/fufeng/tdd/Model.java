package org.fufeng.tdd;

import jakarta.inject.Inject;

public class Model {
}

interface TestComponent {

    default <T> T dependency() {
        return null;
    };

}

interface Dependency {

}

interface AnotherDependency {

}

class ComponentWithDefaultConstructor implements TestComponent {

    public ComponentWithDefaultConstructor() {
    }

}

class ComponentWithInjectConstructor implements TestComponent {

    private final Dependency dependency;

    @Inject
    public ComponentWithInjectConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }

}

class ComponentWithMultiInjectConstructor implements TestComponent {

    private final String name;
    private Double value;

    @Inject
    public ComponentWithMultiInjectConstructor(String name) {
        this.name = name;
    }

    @Inject
    public ComponentWithMultiInjectConstructor(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> T dependency() {
        return null;
    }
}

class ComponentWithNoInjectConstructorNorDefaultConstructor implements TestComponent {
    public ComponentWithNoInjectConstructorNorDefaultConstructor(String name) {
    }

}

class DependencyWithInjectConstructor implements Dependency {

    private final String dependency;

    @Inject
    public DependencyWithInjectConstructor(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }

}

class DependencyDependencyWithInjectConstructor implements Dependency {

    private final TestComponent component;

    @Inject
    public DependencyDependencyWithInjectConstructor(TestComponent component) {
        this.component = component;
    }

}

class AnotherDependencyDependedOnComponent implements AnotherDependency {

    private final TestComponent component;

    @Inject
    public AnotherDependencyDependedOnComponent(TestComponent component) {
        this.component = component;
    }

}

class DependencyDependedOnAnotherDependency implements Dependency {

    private final AnotherDependency anotherDependency;

    @Inject
    public DependencyDependedOnAnotherDependency(AnotherDependency anotherDependency) {
        this.anotherDependency = anotherDependency;
    }
}

