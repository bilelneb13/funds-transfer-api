package com.gs.fundstransfer.exceptions;

import org.aspectj.bridge.IMessage;

public class UnavailableFXRatesException extends RuntimeException {
    public UnavailableFXRatesException(String message) {
        super(message);
    }
}
