package com.example.energy_producer.messaging;

import com.example.energy_producer.dto.EnergyMessageDto;
import com.example.energy_producer.weather.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class ProductionSender {

    private final RabbitTemplate rabbit;
    private final WeatherService weather;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();

    public ProductionSender(RabbitTemplate rabbit, WeatherService weather) {
        this.rabbit = rabbit;
        this.weather = weather;
    }

    @PostConstruct
    public void start() {
        //Nach dem Spring-Start beginnt ein eigener Thread, der regelmäßig Produktionsnachrichten erzeugt.
        Thread sender = new Thread(this::loop);
        sender.setName("production-sender");
        sender.start();
    }

    private void loop() {
        while (true) {
            try {
                send();
            } catch (Exception exception) {
                System.err.println("Could not send production message: " + exception.getMessage());
            }
            // sleep between 1 and 5 seconds before sending the next message
            int sleepMs = 1000 + random.nextInt(4001);
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void send() throws Exception {
        double sunFactor = weather.getSunFactor();
        //Die simulierte Produktion hängt vom Wetter ab: mehr Sonne bedeutet mehr kWh.
        double base = 0.001 + random.nextDouble() * 0.005;
        double kwh = round(base + sunFactor * 0.01);

        String datetime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        //type=PRODUCER ist wichtig, damit der Usage Service community_produced erhöht.
        EnergyMessageDto message = new EnergyMessageDto("PRODUCER", "COMMUNITY", kwh, datetime);

        String json = mapper.writeValueAsString(message);
        System.out.println("Sending production message");
        System.out.println(json);
        //RabbitTemplate sendet die JSON-Nachricht an RabbitMQ.
        this.rabbit.convertAndSend("energy_messages", json);
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

}
