package com.example.usage_service.messaging;

import com.example.usage_service.dto.EnergyMessageDto;
import com.example.usage_service.repository.UsageEntity;
import com.example.usage_service.repository.UsageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class UsageProcessor {

    private final RabbitTemplate rabbit;
    private final UsageRepository usageRepository;
    //ObjectMapper wandelt die JSON-Nachricht aus RabbitMQ in ein EnergyMessageDto um.
    private final ObjectMapper mapper = new ObjectMapper();

    public UsageProcessor(RabbitTemplate rabbit, UsageRepository usageRepository) {
        this.rabbit = rabbit;
        this.usageRepository = usageRepository;
    }
    //Der Usage Service hört auf energy_messages, verarbeitet Nachrichten, speichert Daten und sendet danach ein Update.
    //Das ist Choreography, weil der Service auf Events reagiert und danach selbst ein neues Event auslöst.
    @RabbitListener(queues = "energy_messages")
    public void readFromEnergyMessages(String message) throws Exception {
        System.out.println("Received message from queue");
        System.out.println(message);

        EnergyMessageDto dto = mapper.readValue(message, EnergyMessageDto.class);

        //Alle Nachrichten werden auf die volle Stunde gekürzt.
        //Dadurch landen mehrere Events derselben Stunde im gleichen Datensatz.
        LocalDateTime hour = LocalDateTime
                .parse(dto.getDatetime())
                .truncatedTo(ChronoUnit.HOURS);
        //Der Service sucht zuerst, ob es für diese Stunde schon einen Datensatz gibt.
        //Wenn nicht, wird eine neue UsageEntity erstellt.
        UsageEntity entity = usageRepository
                .findById(hour)
                .orElseGet(() -> {
                    UsageEntity created = new UsageEntity();
                    created.setHour(hour);
                    return created;
                });
        // Hier wird entschieden, wie die empfangene Nachricht verarbeitet wird.
        // Eine PRODUCER-Nachricht erhöht die produzierte Community-Energie.
        // Eine USER-Nachricht erhöht den Verbrauch:
        // Zuerst wird verfügbare Community-Energie verwendet.
        // Reicht diese nicht aus, wird der restliche Bedarf aus dem Grid gedeckt.
        if ("PRODUCER".equals(dto.getType())) {
            entity.setCommunityProduced(entity.getCommunityProduced() + dto.getKwh());
        } else if ("USER".equals(dto.getType())) {
            //available zeigt, wie viel Community-Energie in dieser Stunde noch übrig ist.
            double available = entity.getCommunityProduced() - entity.getCommunityUsed();
            if (dto.getKwh() <= available) {
                // the community pool can cover the demand
                entity.setCommunityUsed(entity.getCommunityUsed() + dto.getKwh());
            } else {
                // take what is left from the community pool and get the rest from the grid
                double fromCommunity = Math.max(0.0, available);
                double fromGrid = dto.getKwh() - fromCommunity;
                entity.setCommunityUsed(entity.getCommunityUsed() + fromCommunity);
                entity.setGridUsed(entity.getGridUsed() + fromGrid);
            }
        }
        //Hier wird der Datensatz in PostgreSQL gespeichert.
        usageRepository.save(entity);

        //Nach dem Speichern wird nur die betroffene Stunde verschickt.
        //Der Percentage Service lädt sich die vollständigen Werte selbst aus der Datenbank.
        this.rabbit.convertAndSend("usage_updates", hour.toString());
    }

}
