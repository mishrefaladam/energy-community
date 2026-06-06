package com.example.energy_user.messaging;

import com.example.energy_user.dto.EnergyMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class UsageSender {

    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();

    public UsageSender(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @PostConstruct
    public void start() {
        Thread sender = new Thread(this::loop);
        sender.setName("usage-sender");
        sender.start();
    }

    private void loop() {
        while (true) {
            try {
                send();
            } catch (Exception exception) {
                System.err.println("Could not send usage message: " + exception.getMessage());
            }
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
        LocalDateTime now = LocalDateTime.now();
        double peakFactor = peakFactorForHour(now.getHour());
        // peakFactor is between 0.0 (deep night) and 1.0 (peak hour)
        double base = 0.001 + random.nextDouble() * 0.003;
        double kwh = round(base + peakFactor * 0.008);

        String datetime = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        EnergyMessageDto message = new EnergyMessageDto("USER", "COMMUNITY", kwh, datetime);

        String json = mapper.writeValueAsString(message);
        System.out.println("Sending usage message");
        System.out.println(json);
        this.rabbit.convertAndSend("energy_messages", json);
    }

    /**
     * Returns a value between 0.0 and 1.0 describing how much energy is used
     * at the given hour. Higher values in the morning (around 8) and in the
     * evening (around 19), lower values during the night.
     */
    private double peakFactorForHour(int hourOfDay) {
        double morning = Math.exp(-Math.pow(hourOfDay - 8, 2) / 4.0);
        double evening = Math.exp(-Math.pow(hourOfDay - 19, 2) / 4.0);
        return Math.min(1.0, morning + evening);
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

}
