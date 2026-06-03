package com.example.demo.transaction.application.port.in;

import com.example.demo.transaction.domain.event.TransactionPendingEvent;

public interface ProcessTransactionUseCase {
	void process(TransactionPendingEvent event);
}
