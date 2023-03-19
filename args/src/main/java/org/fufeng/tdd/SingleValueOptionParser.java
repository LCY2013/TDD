package org.fufeng.tdd;

import java.util.List;
import java.util.function.Function;

class SingleValueOptionParser<T> implements OptionParser {

    private Function<String, T> valueParser;

    private Function<String, T> zeroValue;

    public SingleValueOptionParser(Function<String, T> valueParser, Function<String, T> zeroValue) {
        this.valueParser = valueParser;
        this.zeroValue = zeroValue;
    }

    @Override
    public T parse(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        if (index == -1) {
            return zeroValue.apply(null);
        }
        String value = arguments.get(index + 1);
        return valueParser.apply(value);
    }

}
