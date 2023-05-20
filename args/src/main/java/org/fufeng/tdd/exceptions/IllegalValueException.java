package org.fufeng.tdd.exceptions;

public class IllegalValueException extends RuntimeException {

    private final String option;

    private final String value;

    public IllegalValueException(String option, String value) {
        this.option = option;
        this.value = value;
    }

    public String getOption() {
        return option;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "IllegalValueException{" +
                "option='" + option + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
