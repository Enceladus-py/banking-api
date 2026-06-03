package com.example.demo.transaction.application.port.out;

import com.example.demo.transaction.domain.event.TransactionEvent;

public interface EventPublisher {
	void publish(TransactionEvent event);
}
