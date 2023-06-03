package org.fufeng.tdd;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class ReflectionBasedOptionClass<T> implements OptionClass<T> {

    private Map<String, Class> OptionTypeMapping;
    private final Class<T> intOptionClass;

    public ReflectionBasedOptionClass(Map<String, Class> optionTypeMapping, Class<T> intOptionClass) {
        OptionTypeMapping = optionTypeMapping;
        this.intOptionClass = intOptionClass;
    }

    @Override
    public String[] getOptionNames() {
        return Arrays.stream(intOptionClass.getDeclaredConstructors()[0].getParameters()).
                filter(param -> param.isAnnotationPresent(Option.class)).
                map(param -> param.getAnnotation(Option.class).value()).toArray(String[]::new);
    }

    @Override
    public Class getOptionType(String name) {
        return optionType(name);
    }

    @Override
    public T create(Object[] values) {
        try {
            return (T)intOptionClass.getDeclaredConstructors()[0].newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerOptionType(String option, Class optionTypeClass) {
        OptionTypeMapping.put(option, optionTypeClass);
    }

    private Class optionType(String option) {
        return OptionTypeMapping.get(option);
    }
}
