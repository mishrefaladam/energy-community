package com.example.energy_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//energy-api ist eine eigene Spring-Boot-Anwendung.
//Das passt zu SOA/Microservices: ein eigener Service mit eigener Aufgabe.
@SpringBootApplication
public class EnergyApiApplication {

	//args sind optionale parameter für den start (nicht zwingend notwentig)
	public static void main(String[] args) {
		SpringApplication.run(EnergyApiApplication.class, args);
	}

}
