package org.fufeng.tdd;

import java.lang.reflect.Type;
import java.util.List;

import static java.util.List.of;

public interface ComponentProvider<T> {
    T get(Context context);

    default List<Type> getDependencies() {
        return of();
    }
}
