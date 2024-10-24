package com.gs.fundstransfer.exceptions;

public class UnavailableFXRatesException extends RuntimeException {
    public UnavailableFXRatesException(String message) {
        super(message);
    }
}
