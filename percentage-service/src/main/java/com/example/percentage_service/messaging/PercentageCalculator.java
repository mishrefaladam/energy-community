package com.example.percentage_service.messaging;

import com.example.percentage_service.repository.CurrentPercentageEntity;
import com.example.percentage_service.repository.CurrentPercentageRepository;
import com.example.percentage_service.repository.UsageEntity;
import com.example.percentage_service.repository.UsageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PercentageCalculator {

    private final UsageRepository usageRepository;
    private final CurrentPercentageRepository currentPercentageRepository;

    public PercentageCalculator(UsageRepository usageRepository,
                                CurrentPercentageRepository currentPercentageRepository) {
        this.usageRepository = usageRepository;
        this.currentPercentageRepository = currentPercentageRepository;
    }

    @RabbitListener(queues = "usage_updates")
    public void readFromUsageUpdates(String message) {
        System.out.println("Received message from queue");
        System.out.println(message);

        LocalDateTime hour = LocalDateTime.parse(message);

        UsageEntity usage = usageRepository.findById(hour).orElse(null);
        if (usage == null) {
            // no usage data for this hour yet
            return;
        }

        double communityDepleted = 0.0;
        if (usage.getCommunityProduced() > 0) {
            communityDepleted = usage.getCommunityUsed() / usage.getCommunityProduced() * 100.0;
        } else if (usage.getCommunityUsed() > 0) {
            communityDepleted = 100.0;
        }

        double totalUsed = usage.getCommunityUsed() + usage.getGridUsed();
        double gridPortion = 0.0;
        if (totalUsed > 0) {
            gridPortion = usage.getGridUsed() / totalUsed * 100.0;
        }

        // the percentage table only holds the information of the current hour
        currentPercentageRepository.deleteAll();

        CurrentPercentageEntity entity = new CurrentPercentageEntity();
        entity.setHour(hour);
        entity.setCommunityDepleted(round(communityDepleted));
        entity.setGridPortion(round(gridPortion));
        currentPercentageRepository.save(entity);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
