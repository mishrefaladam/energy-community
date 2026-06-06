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
    private final ObjectMapper mapper = new ObjectMapper();

    public UsageProcessor(RabbitTemplate rabbit, UsageRepository usageRepository) {
        this.rabbit = rabbit;
        this.usageRepository = usageRepository;
    }

    @RabbitListener(queues = "energy_messages")
    public void readFromEnergyMessages(String message) throws Exception {
        System.out.println("Received message from queue");
        System.out.println(message);

        EnergyMessageDto dto = mapper.readValue(message, EnergyMessageDto.class);

        LocalDateTime hour = LocalDateTime
                .parse(dto.getDatetime())
                .truncatedTo(ChronoUnit.HOURS);

        UsageEntity entity = usageRepository
                .findById(hour)
                .orElseGet(() -> {
                    UsageEntity created = new UsageEntity();
                    created.setHour(hour);
                    return created;
                });

        if ("PRODUCER".equals(dto.getType())) {
            entity.setCommunityProduced(entity.getCommunityProduced() + dto.getKwh());
        } else if ("USER".equals(dto.getType())) {
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

        usageRepository.save(entity);

        // tell the percentage service that new usage data is available
        this.rabbit.convertAndSend("usage_updates", hour.toString());
    }

}
