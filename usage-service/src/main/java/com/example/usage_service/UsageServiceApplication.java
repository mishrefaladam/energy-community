package com.example.usage_service;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//usage-service läuft als eigene Anwendung und verarbeitet Energienachrichten.
@SpringBootApplication
public class UsageServiceApplication {

	@Bean
	//Diese Methode gibt ein Objekt vom Typ Queue zurück.
	public Queue energyMessagesQueue() {
		//erstellt ein queue, Diese Queue enthält die Rohdaten vom Producer und vom User.
		//true bedeutet durable: RabbitMQ behält die Queue nach einem Neustart.
		return new Queue("energy_messages", true);
	}

	@Bean
	public Queue usageUpdatesQueue() {
		//Diese Queue informiert den Percentage Service, dass sich eine Stunde geändert hat.
		return new Queue("usage_updates", true);
	}

	public static void main(String[] args) {
		SpringApplication.run(UsageServiceApplication.class, args);
	}

}
