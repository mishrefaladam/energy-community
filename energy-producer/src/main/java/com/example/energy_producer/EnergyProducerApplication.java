package com.example.energy_producer;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//energy-producer läuft als eigene Anwendung und erzeugt Produktionsdaten.
@SpringBootApplication
public class EnergyProducerApplication {

	@Bean
	public Queue energyMessagesQueue() {
		//Producer und User senden beide in dieselbe Queue energy_messages.
		return new Queue("energy_messages", true);
	}

	public static void main(String[] args) {
		SpringApplication.run(EnergyProducerApplication.class, args);
	}

}
