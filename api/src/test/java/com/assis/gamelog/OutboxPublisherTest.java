package com.assis.gamelog;

import com.assis.gamelog.config.RabbitConfig;
import com.assis.gamelog.messaging.OutboxPublisher;
import com.assis.gamelog.model.OutboxEvent;
import com.assis.gamelog.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitOperations operations;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void setUp() {
        when(rabbitTemplate.invoke(any(RabbitOperations.OperationsCallback.class)))
                .thenAnswer(invocation -> invocation.<RabbitOperations.OperationsCallback<?>>getArgument(0).doInRabbit(operations));
    }

    @Test
    void shouldPublishPendingEventAndMarkAsPublished() {
        OutboxEvent outboxEvent = outboxEvent("event-1");
        when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(outboxEvent));

        outboxPublisher.publishPendingEvents();

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(operations).send(eq(RabbitConfig.GAME_EVENTS_EXCHANGE), eq("game.status.changed"), captor.capture());
        Message message = captor.getValue();
        assertEquals("event-1", message.getMessageProperties().getMessageId());
        assertEquals(MessageProperties.CONTENT_TYPE_JSON, message.getMessageProperties().getContentType());
        assertEquals("{\"eventId\":\"event-1\"}", new String(message.getBody(), StandardCharsets.UTF_8));
        verify(operations).waitForConfirmsOrDie(5000);

        assertNotNull(outboxEvent.getPublishedAt());
        verify(outboxEventRepository).save(outboxEvent);
    }

    @Test
    void shouldKeepEventPendingWhenBrokerDoesNotConfirm() {
        OutboxEvent first = outboxEvent("event-1");
        OutboxEvent second = outboxEvent("event-2");
        when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(first, second));
        doThrow(new AmqpException("nack")).when(operations).waitForConfirmsOrDie(anyLong());

        assertThrows(AmqpException.class, () -> outboxPublisher.publishPendingEvents());

        assertNull(first.getPublishedAt());
        assertNull(second.getPublishedAt());
        verify(outboxEventRepository, never()).save(any());
        verify(operations, times(1)).send(anyString(), anyString(), any(Message.class));
    }

    @Test
    void shouldDoNothingWhenThereAreNoPendingEvents() {
        when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of());

        outboxPublisher.publishPendingEvents();

        verifyNoInteractions(rabbitTemplate);
    }

    private OutboxEvent outboxEvent(String eventId) {
        return OutboxEvent.builder()
                .eventId(eventId)
                .routingKey("game.status.changed")
                .payload("{\"eventId\":\"" + eventId + "\"}")
                .build();
    }
}
