package org.fufeng.tdd;

import java.util.List;

import static java.util.List.of;

public interface ComponentProvider<T> {
    T get(Context context);

    default List<ComponentRef> getDependencies(){
        return of();
    }

}
