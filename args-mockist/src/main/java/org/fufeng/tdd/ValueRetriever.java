package org.fufeng.tdd;

/**
 * 从参数列表中提取参数的 ValueRetriever
 */
public interface ValueRetriever {

    String[] getValue(String name, String[] values);

}
