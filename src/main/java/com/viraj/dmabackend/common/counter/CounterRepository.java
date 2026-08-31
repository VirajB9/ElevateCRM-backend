package com.viraj.dmabackend.common.counter;

public interface CounterRepository {

    long getNextSequence(String sequenceName);
}
