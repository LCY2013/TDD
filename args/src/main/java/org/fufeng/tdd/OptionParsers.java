package org.fufeng.tdd;

import org.fufeng.tdd.exceptions.IllegalValueException;
import org.fufeng.tdd.exceptions.InsufficientArgmentsException;
import org.fufeng.tdd.exceptions.TooManyArgmentsException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class OptionParsers {

    public static OptionParser<Boolean> bool() {
        return (arguments, option) -> values(arguments, option, 0).isPresent();
    }

    public static <T> OptionParser<T> unary(Function<String, T> valueParser, Function<String, T> zeroValue) {
        return (arguments, option) -> values(arguments, option, 1).
                map(value -> parseValue(option, value.get(0), valueParser)).
                orElse(zeroValue.apply(null));
    }

    public static <T> OptionParser<T[]> list(Function<String, T> valueParser, IntFunction<T[]> generator) {
        return (arguments, option) -> values(arguments, option).
                map(
                        it -> it.stream().
                                map(value -> parseValue(option, value, valueParser)).
                                toArray(generator)
                ).orElse(generator.apply(0));
    }

    private static Optional<List<String>> values(List<String> arguments, Option option) {
        return Optional.ofNullable(
                !arguments.contains("-" + option.value()) ?
                        null :
                        valuesFrom(arguments, arguments.indexOf("-" + option.value()))
        );
    }

    private static Optional<List<String>> values(List<String> arguments, Option option, int expectedSize) {

        return values(arguments, option).map(
                it -> checkSize(option, expectedSize, it)
        );
    }

    private static List<String> checkSize(Option option, int expectedSize, List<String> values) {
        if (values.size() < expectedSize) {
            throw new InsufficientArgmentsException(option.value());
        }

        if (values.size() > expectedSize) {
            throw new TooManyArgmentsException(option.value());
        }

        return values;
    }

    private static <T> T parseValue(Option option, String value, Function<String, T> valueParser) {
        try {
            return valueParser.apply(value);
        } catch (Exception e) {
            throw new IllegalValueException(option.value(), value);
        }
    }

    private static List<String> valuesFrom(List<String> arguments, int index) {
        return arguments.subList(index + 1, IntStream.range(index + 1, arguments.size())
                .filter(it -> arguments.get(it).matches("^-[a-zA-Z]+$"))
                .findFirst().orElse(arguments.size()));
    }

}
