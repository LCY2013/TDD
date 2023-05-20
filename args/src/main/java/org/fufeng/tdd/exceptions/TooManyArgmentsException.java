package org.fufeng.tdd.exceptions;

public class TooManyArgmentsException extends RuntimeException {

    private Object option;

    public TooManyArgmentsException() {
    }

    public TooManyArgmentsException(String message) {
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
