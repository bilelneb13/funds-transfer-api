package com.gs.fundstransfer.exceptions;

public class UnsufficientFundsException extends RuntimeException {
    public UnsufficientFundsException(String message) {
        super(message);
    }
}