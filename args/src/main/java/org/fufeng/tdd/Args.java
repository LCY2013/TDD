package org.fufeng.tdd;

import org.fufeng.tdd.exceptions.IllegalOptionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Args<T> {

    // 集成测试到单元测试的抽取

    private static Map<Class<?>, OptionParser> PARSERS = Map.of(
            boolean.class, OptionParsers.bool(),
            Boolean.class, OptionParsers.bool(),
            int.class, OptionParsers.unary(Integer::parseInt, v -> 0),
            Integer.class, OptionParsers.unary(Integer::parseInt, v -> 0),
            //String.class, new SingleValueOptionParser<String>(String::valueOf, v-> "")
            String.class, OptionParsers.unary(Function.identity(), v-> ""),
            String[].class, OptionParsers.list(Function.identity(), String[]::new),
            Integer[].class, OptionParsers.list(Integer::parseInt, Integer[]::new),
            int[].class, OptionParsers.list(Integer::parseInt, Integer[]::new)
    );

    public static <T> T parse(Class<T> optionClass, String... args) {
        return new Args<T>(optionClass, PARSERS).parse(args);
    }

    private Class<T> optionClass;
    private Map<Class<?>, OptionParser> parsers;

    public Args(Class<T> optionClass, Map<Class<?>, OptionParser> parsers) {
        this.optionClass = optionClass;
        this.parsers = parsers;
    }

    public T parse(String... args) {
        try {
            List<String> arguments = Arrays.asList(args);
            Constructor<?> constructor = optionClass.getDeclaredConstructors()[0];

            Object[] initargs = Arrays.stream(constructor.getParameters()).map(param -> parseOption(param, arguments)).toArray();

            return (T) constructor.newInstance(initargs);
        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object parseOption(Parameter parameter, List<String> arguments) {
        if (!parameter.isAnnotationPresent(Option.class)) {
            throw new IllegalOptionException(parameter.getName());
        }
        return parsers.get(parameter.getType()).parse(arguments, parameter.getAnnotation(Option.class));
    }

}
