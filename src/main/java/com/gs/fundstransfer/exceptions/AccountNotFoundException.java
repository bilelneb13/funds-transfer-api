package com.gs.fundstransfer.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("The account with ownerId " + id + " was not found.");
    }
}
