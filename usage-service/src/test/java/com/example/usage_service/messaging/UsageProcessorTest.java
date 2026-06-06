package com.example.usage_service.messaging;

import com.example.usage_service.repository.UsageEntity;
import com.example.usage_service.repository.UsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

// this annotation enables the @Mock
@ExtendWith(MockitoExtension.class)
class UsageProcessorTest {

    private UsageProcessor processor;

    @Mock
    private RabbitTemplate mockRabbitTemplate;

    @Mock
    private UsageRepository mockUsageRepository;

    @BeforeEach
    void setUp() {
        this.processor = new UsageProcessor(mockRabbitTemplate, mockUsageRepository);
    }

    @Test
    void producerMessageAddsToCommunityProduced() throws Exception {
        // Arrange
        String message = "{\"type\":\"PRODUCER\",\"association\":\"COMMUNITY\",\"kwh\":1.0,\"datetime\":\"2025-01-10T14:33:00\"}";
        when(mockUsageRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        processor.readFromEnergyMessages(message);

        // Assert
        ArgumentCaptor<UsageEntity> captor = ArgumentCaptor.forClass(UsageEntity.class);
        verify(mockUsageRepository).save(captor.capture());
        UsageEntity saved = captor.getValue();
        assertEquals(1.0, saved.getCommunityProduced());
        assertEquals(0.0, saved.getCommunityUsed());
        assertEquals(0.0, saved.getGridUsed());
        verify(mockRabbitTemplate).convertAndSend(eq("usage_updates"), any(Object.class));
    }

    @Test
    void userMessageWithinPoolAddsToCommunityUsed() throws Exception {
        // Arrange
        String message = "{\"type\":\"USER\",\"association\":\"COMMUNITY\",\"kwh\":0.3,\"datetime\":\"2025-01-10T14:34:00\"}";
        UsageEntity existing = new UsageEntity();
        existing.setHour(LocalDateTime.parse("2025-01-10T14:00:00"));
        existing.setCommunityProduced(1.0);
        existing.setCommunityUsed(0.5);
        when(mockUsageRepository.findById(any())).thenReturn(Optional.of(existing));

        // Act
        processor.readFromEnergyMessages(message);

        // Assert
        ArgumentCaptor<UsageEntity> captor = ArgumentCaptor.forClass(UsageEntity.class);
        verify(mockUsageRepository).save(captor.capture());
        UsageEntity saved = captor.getValue();
        assertEquals(1.0, saved.getCommunityProduced());
        assertEquals(0.8, saved.getCommunityUsed());
        assertEquals(0.0, saved.getGridUsed());
    }

    @Test
    void userMessageBeyondPoolAddsRestToGrid() throws Exception {
        // Arrange
        String message = "{\"type\":\"USER\",\"association\":\"COMMUNITY\",\"kwh\":0.05,\"datetime\":\"2025-01-10T14:34:00\"}";
        UsageEntity existing = new UsageEntity();
        existing.setHour(LocalDateTime.parse("2025-01-10T14:00:00"));
        existing.setCommunityProduced(18.05);
        existing.setCommunityUsed(18.02);
        existing.setGridUsed(1.056);
        when(mockUsageRepository.findById(any())).thenReturn(Optional.of(existing));

        // Act
        processor.readFromEnergyMessages(message);

        // Assert
        ArgumentCaptor<UsageEntity> captor = ArgumentCaptor.forClass(UsageEntity.class);
        verify(mockUsageRepository).save(captor.capture());
        UsageEntity saved = captor.getValue();
        // 0.03 comes from community pool, 0.02 from grid (example from project specification)
        assertEquals(18.05, saved.getCommunityUsed(), 0.0001);
        assertEquals(1.076, saved.getGridUsed(), 0.0001);
    }

}
