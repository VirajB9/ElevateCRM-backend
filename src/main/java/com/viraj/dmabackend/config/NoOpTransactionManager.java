package com.viraj.dmabackend.config;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Fallback transaction manager for standalone MongoDB instances that do not support
 * multi-document transactions (which require a replica set or mongos cluster).
 */
public class NoOpTransactionManager extends AbstractPlatformTransactionManager {

    @Override
    protected Object doGetTransaction() {
        return new Object();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // No-op for standalone MongoDB deployments
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        // No-op
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        // No-op
    }
}
