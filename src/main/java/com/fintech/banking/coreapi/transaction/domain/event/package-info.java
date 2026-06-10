/**
 * Domain events for the {@code transaction} module. Exposed as part of the
 * module's public API so that event listeners in other modules (e.g. the Kafka
 * adapter) can reference the event types.
 */
@org.springframework.modulith.NamedInterface("events")
package com.fintech.banking.coreapi.transaction.domain.event;
