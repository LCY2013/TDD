package org.fufeng.tdd;

import java.util.Arrays;

/**
 * <p>命令行解析</p>
 * 伦敦学派的做法是这样的：
 * 按照功能需求与架构愿景划分对象的角色和职责；
 * 根据角色与职责，明确对象之间的交互；
 * 按照调用栈（Call Stack）的顺序，自外向内依次实现不同的对象；
 * 在实现的过程中，依照交互关系，使用测试替身替换所有与被实现对象直接关联的对象；
 * 直到所有对象全部都实现完成。
 * <p>
 * 首先是明确我们的架构愿景，也就是对象的角色与职责划分。如下图所示：
 * <p>
 * 如上图所示，在系统中一共存在四个类：作为对外 API 的 Args，从参数列表中提取参数的 ValueRetriever，将 Java 类对象封装为选项的 OptionClass，以及根据类型和数据解析参数的 OptionParser。
 */
public class Args<T> {

    private final ValueRetriever valueRetriever;
    private final OptionParser optionParser;
    private final OptionClass<T> optionClass;

    public Args(ValueRetriever valueRetriever, OptionParser optionParser, OptionClass<T> optionClass) {
        this.valueRetriever = valueRetriever;
        this.optionParser = optionParser;
        this.optionClass = optionClass;
    }


    public T parse(String... args) {
        return optionClass.create(Arrays.stream(optionClass.getOptionNames()).
                map(
                        name -> optionParser.parse(
                                optionClass.getOptionType(name),
                                valueRetriever.getValue(name, args))
                ).toArray()
        );
    }
}
