package org.fufeng.tdd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class Args {

    public static <T> T parse(Class<T> optionClass, String... args) {
        try {
            Constructor<?> constructor = optionClass.getDeclaredConstructors()[0];
            Parameter parameter = constructor.getParameters()[0];
            Option option = parameter.getAnnotation(Option.class);
            List<String> arguments = Arrays.asList(args);

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

            return (T) constructor.newInstance(initargs);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
