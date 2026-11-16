package com.viraj.dmabackend.common.counter;

public interface CounterRepository {

    long getNextSequence(String sequenceName);

    boolean decrementSequenceIfCurrent(String sequenceName, long expectedCurrentValue);
}
