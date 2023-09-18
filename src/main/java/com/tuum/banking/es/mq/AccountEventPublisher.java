package com.tuum.banking.es.mq;

import com.tuum.banking.es.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountEventPublisher implements EventPublisher<BaseEvent> {

    private final FanoutExchange exchange;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(BaseEvent message) {
        rabbitTemplate.convertAndSend(exchange.getName(), "", message);
    }
}
