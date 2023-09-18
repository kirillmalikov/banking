package com.tuum.banking.es.mq;

public interface EventPublisher<T> {

    void publish(T message);
}
