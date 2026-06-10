package com.fintech.banking.coreapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application entry point.
 */
@SpringBootApplication
@EnableScheduling
public class CoreApiApplication {

	/**
	 * Default constructor.
	 */
	public CoreApiApplication() {
	}

	/**
	 * Application entry point method.
	 *
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(CoreApiApplication.class, args);
	}
}
