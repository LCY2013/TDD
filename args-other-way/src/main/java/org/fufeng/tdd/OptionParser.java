package org.fufeng.tdd;

import java.util.List;

public interface OptionParser<T> {

    T parse(String[] arguments);

}
