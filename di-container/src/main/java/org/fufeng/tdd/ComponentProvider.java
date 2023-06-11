package org.fufeng.tdd;

import java.util.List;

import static java.util.List.of;

public interface ComponentProvider<T> {
    T get(Context context);

    default List<Class<?>> getDependencies() {
        return of();
    }
}
