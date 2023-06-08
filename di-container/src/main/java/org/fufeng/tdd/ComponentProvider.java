package org.fufeng.tdd;

import java.util.List;

public interface ComponentProvider<T> {
    T get(Context context);

    List<Class<?>> getDependencies();
}
