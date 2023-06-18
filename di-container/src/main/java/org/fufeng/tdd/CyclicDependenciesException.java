package org.fufeng.tdd;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CyclicDependenciesException extends RuntimeException {
    private final Set<Component> classes = new HashSet<>();

    public CyclicDependenciesException(List<Component> visiting) {
        this.classes.addAll(visiting);
    }


    public Set<Class<?>> getComponents() {
        return classes.stream().map(Component::type).collect(Collectors.toSet());
    }
}
