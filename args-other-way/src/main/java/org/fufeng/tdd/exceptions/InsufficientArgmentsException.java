package org.fufeng.tdd.exceptions;

public class InsufficientArgmentsException extends RuntimeException {

    private Object option;

    public InsufficientArgmentsException() {
    }

    public InsufficientArgmentsException(String message) {
        super(message);
        this.option = message;
    }

    public Object getOption() {
        return option;
    }

    public void setOption(Object option) {
        this.option = option;
    }
}