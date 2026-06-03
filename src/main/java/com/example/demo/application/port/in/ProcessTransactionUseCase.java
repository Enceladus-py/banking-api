package com.example.demo.application.port.in;

import com.example.demo.domain.event.TransactionPendingEvent;

public interface ProcessTransactionUseCase {
	void process(TransactionPendingEvent event);
}
