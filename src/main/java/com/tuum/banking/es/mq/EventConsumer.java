package com.tuum.banking.es.mq;

public interface EventConsumer<T> {

    void consume(T message);
}
