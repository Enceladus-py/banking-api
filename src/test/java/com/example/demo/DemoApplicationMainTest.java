package com.example.demo;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class DemoApplicationMainTest {

	@Test
	void mainMethodTest() {
		String[] args = new String[]{"test-arg"};
		try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
			springApplication.when(() -> SpringApplication.run(DemoApplication.class, args)).thenReturn(null);

			DemoApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(DemoApplication.class, args));
		}
	}
}
