package org.fufeng.tdd;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

class SingleValueOptionParser<T> implements OptionParser<T> {

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

        List<String> values = valuesFrom(arguments, index);

        if (values.size() < 1) {
            throw new InsufficientArgmentsException(option.value());
        }

        if (values.size() > 1) {
            throw new TooManyArgmentsException(option.value());
        }

        String value = values.get(0);
        return valueParser.apply(value);
    }

    private static List<String> valuesFrom(List<String> arguments, int index) {
        return arguments.subList(index + 1, IntStream.range(index + 1, arguments.size())
                .filter(it -> arguments.get(it).startsWith("-"))
                .findFirst().orElse(arguments.size()));
    }

}
