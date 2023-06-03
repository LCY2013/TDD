package org.fufeng.tdd;

/**
 * 将 Java 类对象封装为选项的 OptionClass
 */
public interface OptionClass<T> {

    String[] getOptionNames();

    Class getOptionType(String name);

    T create(Object[] values);

}
