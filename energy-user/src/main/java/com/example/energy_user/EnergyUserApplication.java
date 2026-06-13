package com.example.energy_user;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//energy-user läuft als eigene Anwendung und erzeugt Verbrauchsdaten.
@SpringBootApplication
public class EnergyUserApplication {

	@Bean
	public Queue energyMessagesQueue() {
		//Der User-Service nutzt dieselbe Queue wie der Producer.
		return new Queue("energy_messages", true);
	}

	public static void main(String[] args) {
		SpringApplication.run(EnergyUserApplication.class, args);
	}

}
