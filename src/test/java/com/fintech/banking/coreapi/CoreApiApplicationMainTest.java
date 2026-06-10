package com.fintech.banking.coreapi;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class CoreApiApplicationMainTest {

	@Test
	void mainMethodTest() {
		String[] args = new String[]{"test-arg"};
		try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
			springApplication.when(() -> SpringApplication.run(CoreApiApplication.class, args)).thenReturn(null);

			CoreApiApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(CoreApiApplication.class, args));
		}
	}
}
