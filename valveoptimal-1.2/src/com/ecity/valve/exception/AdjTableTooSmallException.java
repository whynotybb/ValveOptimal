package com.ecity.valve.exception;

public class AdjTableTooSmallException extends RuntimeException{
    private static final long serialVersionUID = -4966943489942716331L;
    public AdjTableTooSmallException(String message) {
        super(message);
    }
}
