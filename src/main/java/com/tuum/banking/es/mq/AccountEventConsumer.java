package com.tuum.banking.es.mq;

import com.tuum.banking.es.EventHandler;
import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.BaseEvent;
import com.tuum.banking.es.events.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountEventConsumer implements EventConsumer<BaseEvent> {

    private final EventHandler eventHandler;

    @Override
    @RabbitListener(queues = "${tuum.rabbitmq.common.queue}")
    public void consume(BaseEvent event) {
        if (event instanceof AccountCreatedEvent) {
            eventHandler.on((AccountCreatedEvent) event);
        } else if (event instanceof TransactionEvent) {
            eventHandler.on((TransactionEvent) event);
        }
    }
}
