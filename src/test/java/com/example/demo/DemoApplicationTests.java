package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"outbox.scheduler.delay=9999999"})
@org.springframework.kafka.test.context.EmbeddedKafka(partitions = 1)
@org.springframework.test.context.TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
