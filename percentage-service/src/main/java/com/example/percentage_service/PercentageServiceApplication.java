package com.example.percentage_service;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//percentage-service läuft als eigene Anwendung und berechnet Prozentwerte.
@SpringBootApplication
public class PercentageServiceApplication {

	@Bean
	public Queue usageUpdatesQueue() {
		//Der Percentage Service hört nur auf usage_updates, nicht direkt auf energy_messages.
		return new Queue("usage_updates", true);
	}

	public static void main(String[] args) {
		SpringApplication.run(PercentageServiceApplication.class, args);
	}

}
