package org.fufeng.tdd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Args {

    public static <T> T parse(Class<T> optionClass, String... args) {
        try {
            List<String> arguments = Arrays.asList(args);
            Constructor<?> constructor = optionClass.getDeclaredConstructors()[0];

            Object[] initargs = Arrays.stream(constructor.getParameters()).map(param -> parseOption(param, arguments)).toArray();

            return (T) constructor.newInstance(initargs);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parseOption(Parameter parameter, List<String> arguments) {
        Class<?> type = parameter.getType();
        return PARSERS.get(type).parse(arguments, parameter.getAnnotation(Option.class));
    }

    private static Map<Class<?>, OptionParser> PARSERS = Map.of(
            boolean.class, BooleanParser.parser,
            Boolean.class, BooleanParser.parser,
            int.class, new SingleValueOptionParser<Integer>(Integer::parseInt, v -> 0),
            Integer.class, new SingleValueOptionParser<Integer>(Integer::parseInt, v -> 0),
            //String.class, new SingleValueOptionParser<String>(String::valueOf, v-> "")
            String.class, new SingleValueOptionParser<String>(Function.identity(), v-> "")
    );

}
