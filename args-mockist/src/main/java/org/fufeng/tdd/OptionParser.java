package org.fufeng.tdd;

/**
 * 根据类型和数据解析参数的 OptionParser
 */
public interface OptionParser {

    Object parse(Class<?> type, String[] values);

}
