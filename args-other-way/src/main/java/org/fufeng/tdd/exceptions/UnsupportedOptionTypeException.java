package org.fufeng.tdd.exceptions;

public class UnsupportedOptionTypeException extends RuntimeException{

    private String value;
    private Class<?> type;

    public UnsupportedOptionTypeException(String value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return "UnsupportedOptionTypeException{" +
                "value='" + value + '\'' +
                ", type=" + type +
                '}';
    }
}
