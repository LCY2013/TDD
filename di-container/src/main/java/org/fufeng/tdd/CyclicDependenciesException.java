package org.fufeng.tdd;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicDependenciesException extends RuntimeException {
    private final Set<Class<?>> classes = new HashSet<>();

    public CyclicDependenciesException(List<Class<?>> visiting) {
        this.classes.addAll(visiting);
    }


    public Set<Class<?>> getComponents() {
        return classes;
    }
}
