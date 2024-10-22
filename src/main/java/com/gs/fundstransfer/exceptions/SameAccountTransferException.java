package com.gs.fundstransfer.exceptions;

public class SameAccountTransferException extends RuntimeException {
    public SameAccountTransferException(String s) {
        super(s);
    }
}
