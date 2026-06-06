package com.example.energy_user;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EnergyUserApplication {

	@Bean
	public Queue energyMessagesQueue() {
		return new Queue("energy_messages", true);
	}

	public static void main(String[] args) {
		SpringApplication.run(EnergyUserApplication.class, args);
	}

}
