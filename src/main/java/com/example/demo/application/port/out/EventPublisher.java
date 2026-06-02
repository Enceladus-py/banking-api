package com.example.demo.application.port.out;

import com.example.demo.domain.event.TransactionEvent;

public interface EventPublisher {
    void publish(TransactionEvent event);
}
