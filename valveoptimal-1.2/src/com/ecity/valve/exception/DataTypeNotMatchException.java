package com.ecity.valve.exception;

public class DataTypeNotMatchException extends RuntimeException{
    private static final long serialVersionUID = -8229337722610670209L;
    public DataTypeNotMatchException(String message) {
        super(message);
    }
}
