package org.fufeng.tdd;

import org.fufeng.tdd.exceptions.IllegalOptionException;
import org.fufeng.tdd.exceptions.InsufficientArgmentsException;
import org.fufeng.tdd.exceptions.TooManyArgmentsException;
import org.fufeng.tdd.exceptions.UnsupportedOptionTypeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;

public class Args<T> {

    public static Map<String, String[]> toMap(String... args) {
        Map<String, String[]> result = new HashMap<>();
        if (Objects.isNull(args) || args.length == 0) {
            return result;
        }
        String option = null;
        List<String> values = new ArrayList<>();
        for (String arg : args) {
            if (arg.matches("^-[a-zA-z]+$")) {
                if (option != null) {
                    result.put(option.substring(1), values.toArray(String[]::new));
                    values = new ArrayList<>();
                }
                option = arg;
            } else {
                values.add(arg);
            }
        }
        result.put(option.substring(1), values.toArray(String[]::new));
        return result;
    }

    private Class<T> optionClass;
    private Map<Class<?>, OptionParser> parsers;
    private Function<String[], Map<String, String[]>> optionParser;

    public Args(Class<T> optionClass, Map<Class<?>, OptionParser> parsers, Function<String[], Map<String, String[]>> optionParser) {
        this.optionClass = optionClass;
        this.parsers = parsers;
        this.optionParser = optionParser;
    }

    public T parse(String... args) {
        try {
            Map<String, String[]> options = optionParser.apply(args);
            Constructor<?> constructor = optionClass.getDeclaredConstructors()[0];

            Object[] initargs = Arrays.stream(constructor.getParameters()).map(param -> parseOption(param, options)).toArray();

            return (T) constructor.newInstance(initargs);
        } catch (IllegalOptionException | InsufficientArgmentsException | UnsupportedOptionTypeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object parseOption(Parameter parameter, Map<String, String[]> options) {
        if (!parameter.isAnnotationPresent(Option.class)) {
            throw new IllegalOptionException(parameter.getName());
        }
        Option option = parameter.getAnnotation(Option.class);
        if (!parsers.containsKey(parameter.getType())) {
            throw new UnsupportedOptionTypeException(option.value(), parameter.getType());
        }
        return parsers.get(parameter.getType()).parse(options.get(option.value()));
    }

    static void checkSize(int expectedSize, String[] values) {
        if (values == null) {
            values = new String[0];
        }
        if (values.length < expectedSize) {
            throw new InsufficientArgmentsException();
        }

        if (values.length > expectedSize) {
            throw new TooManyArgmentsException();
        }
    }

}