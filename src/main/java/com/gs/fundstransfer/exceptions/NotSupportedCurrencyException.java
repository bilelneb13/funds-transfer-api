package com.gs.fundstransfer.exceptions;

public class NotSupportedCurrencyException extends RuntimeException {
    public NotSupportedCurrencyException(String message) {
        super(message);
    }
}