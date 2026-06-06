package com.example.percentage_service.messaging;

import com.example.percentage_service.repository.CurrentPercentageEntity;
import com.example.percentage_service.repository.CurrentPercentageRepository;
import com.example.percentage_service.repository.UsageEntity;
import com.example.percentage_service.repository.UsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PercentageCalculatorTest {

    private PercentageCalculator calculator;

    @Mock
    private UsageRepository mockUsageRepository;

    @Mock
    private CurrentPercentageRepository mockCurrentPercentageRepository;

    @BeforeEach
    void setUp() {
        this.calculator = new PercentageCalculator(mockUsageRepository, mockCurrentPercentageRepository);
    }

    @Test
    void calculatesPercentagesFromUsage() {
        // Arrange (numbers from the project specification)
        String message = "2025-01-10T14:00:00";
        UsageEntity usage = new UsageEntity();
        usage.setHour(LocalDateTime.parse(message));
        usage.setCommunityProduced(18.05);
        usage.setCommunityUsed(18.05);
        usage.setGridUsed(1.076);
        when(mockUsageRepository.findById(any())).thenReturn(Optional.of(usage));

        // Act
        calculator.readFromUsageUpdates(message);

        // Assert
        ArgumentCaptor<CurrentPercentageEntity> captor = ArgumentCaptor.forClass(CurrentPercentageEntity.class);
        verify(mockCurrentPercentageRepository).save(captor.capture());
        CurrentPercentageEntity saved = captor.getValue();
        assertEquals(100.0, saved.getCommunityDepleted(), 0.01);
        assertEquals(5.63, saved.getGridPortion(), 0.01);
    }

}
