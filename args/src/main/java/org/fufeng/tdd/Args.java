package org.fufeng.tdd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

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
        Option option = parameter.getAnnotation(Option.class);

        Object initargs = null;

        if (parameter.getType() == boolean.class || parameter.getType() == Boolean.class) {
            initargs = arguments.contains("-" + option.value());
        }

        if (parameter.getType() == int.class || parameter.getType() == Integer.class) {
            int index = arguments.indexOf("-" + option.value());
            if (index == -1) {
                initargs = 0;
            } else {
                initargs = Integer.parseInt(arguments.get(index + 1));
            }
        }

        if (parameter.getType() == String.class) {
            int index = arguments.indexOf("-" + option.value());
            if (index == -1) {
                initargs = "";
            } else {
                initargs = arguments.get(index + 1);
            }
        }
        return initargs;
    }

}
