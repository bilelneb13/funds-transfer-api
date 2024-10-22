package com.gs.fundstransfer.model;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;


public class AccountIdGenerator extends SequenceStyleGenerator {

    private static final long LOWER_BOUND = 1000000000L;  // Minimum value
    private static final long UPPER_BOUND = 9999999999L;  // Maximum value
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final AtomicLong currentId = new AtomicLong();

    static {
        // Initialize currentId with a random starting point within the range
        long randomStart = LOWER_BOUND + (long) (secureRandom.nextDouble() * (UPPER_BOUND - LOWER_BOUND + 1));
        currentId.set(randomStart);
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) {
        // Return the next sequential ID
        return currentId.incrementAndGet();
    }
}