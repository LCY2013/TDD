package org.fufeng.tdd.exceptions;

public class IllegalOptionException extends RuntimeException {

    private Object option;

    public IllegalOptionException() {
    }

    public IllegalOptionException(String message) {
        this.option = message;
    }

    public Object getOption() {
        return option;
    }

    public void setOption(Object option) {
        this.option = option;
    }
}
