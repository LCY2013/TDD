package org.fufeng.tdd;

import java.util.HashSet;
import java.util.Set;

public class CyclicDependenciesException extends RuntimeException {
    private final Set<Class<?>> classes = new HashSet<>();

    public CyclicDependenciesException(Class<?> clazz) {
        this.classes.add(clazz);
    }

    public CyclicDependenciesException(Class<?> clazz, CyclicDependenciesException e) {
        this.classes.add(clazz);
        this.classes.addAll(e.getComponents());
    }


    public Set<Class<?>> getComponents() {
        return classes;
    }
}
