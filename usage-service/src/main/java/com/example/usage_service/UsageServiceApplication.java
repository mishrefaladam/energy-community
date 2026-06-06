package com.example.usage_service;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UsageServiceApplication {

	@Bean
	public Queue energyMessagesQueue() {
		return new Queue("energy_messages", true);
	}

	@Bean
	public Queue usageUpdatesQueue() {
		return new Queue("usage_updates", true);
	}

	public static void main(String[] args) {
		SpringApplication.run(UsageServiceApplication.class, args);
	}

}
