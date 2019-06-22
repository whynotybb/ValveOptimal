package com.ecity.valve.exception;

public class NotFoundPropException extends RuntimeException{
    private static final long serialVersionUID = 5196636813580901033L;
    public NotFoundPropException(String message) {
        super(message);
    }
}
