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

    //Der Percentage Service reagiert auf usage_updates und berechnet neue Prozentwerte.
    //Die Message enthält hier nur die Stunde, nicht die kompletten Energiewerte.
    @RabbitListener(queues = "usage_updates")
    public void readFromUsageUpdates(String message) {
        System.out.println("Received message from queue");
        System.out.println(message);

        LocalDateTime hour = LocalDateTime.parse(message);

        //Der percentage-service liest zuerst die Energiedaten für die betroffene Stunde aus der Tabelle energy_usage.
        UsageEntity usage = usageRepository.findById(hour).orElse(null);
        if (usage == null) {
            // no usage data for this hour yet
            return;
        }
        //communityDepleted bedeutet: Wie viel Prozent der produzierten Community-Energie wurden verbraucht?
        double communityDepleted = 0.0;
        if (usage.getCommunityProduced() > 0) {
            communityDepleted = usage.getCommunityUsed() / usage.getCommunityProduced() * 100.0;
        } else if (usage.getCommunityUsed() > 0) {
            communityDepleted = 100.0;
        }

        double totalUsed = usage.getCommunityUsed() + usage.getGridUsed();
        //gridPortion bedeutet: Welcher Anteil des Gesamtverbrauchs kam aus dem öffentlichen Netz?
        double gridPortion = 0.0;
        if (totalUsed > 0) {
            gridPortion = usage.getGridUsed() / totalUsed * 100.0;
        }

        //current_percentage hält nur den aktuellen Wert.
        //Deshalb wird die Tabelle zuerst geleert und danach mit dem neuen Wert befüllt.
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
