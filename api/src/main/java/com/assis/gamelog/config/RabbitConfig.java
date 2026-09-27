package com.assis.gamelog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String GAME_EVENTS_EXCHANGE = "gamelog.game-events";
    public static final String CATALOG_EVENTS_EXCHANGE = "catalogservice.catalog-events";
    public static final String DEAD_LETTER_EXCHANGE = "gamelog.dlx";
    public static final String GAME_HISTORY_QUEUE = "gamelog.game-history";
    public static final String GAME_HISTORY_DLQ = "gamelog.game-history.dlq";
    public static final String CATALOG_GAME_RESOLVED_QUEUE = "gamelog.catalog-game-resolved";
    public static final String CATALOG_GAME_RESOLVED_DLQ = "gamelog.catalog-game-resolved.dlq";

    @Bean
    public TopicExchange gameEventsExchange() {
        return new TopicExchange(GAME_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange catalogEventsExchange() {
        return new TopicExchange(CATALOG_EVENTS_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue gameHistoryQueue() {
        return QueueBuilder.durable(GAME_HISTORY_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(GAME_HISTORY_DLQ)
                .build();
    }

    @Bean
    public Queue gameHistoryDeadLetterQueue() {
        return QueueBuilder.durable(GAME_HISTORY_DLQ).build();
    }

    @Bean
    public Queue catalogGameResolvedQueue() {
        return QueueBuilder.durable(CATALOG_GAME_RESOLVED_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(CATALOG_GAME_RESOLVED_DLQ)
                .build();
    }

    @Bean
    public Queue catalogGameResolvedDeadLetterQueue() {
        return QueueBuilder.durable(CATALOG_GAME_RESOLVED_DLQ).build();
    }

    @Bean
    public Binding gameHistoryChangedBinding(Queue gameHistoryQueue, TopicExchange gameEventsExchange) {
        return BindingBuilder.bind(gameHistoryQueue).to(gameEventsExchange).with("game.*.changed");
    }

    @Bean
    public Binding gameHistoryDeletedBinding(Queue gameHistoryQueue, TopicExchange gameEventsExchange) {
        return BindingBuilder.bind(gameHistoryQueue).to(gameEventsExchange).with("game.deleted");
    }

    @Bean
    public Binding gameHistoryDeadLetterBinding(Queue gameHistoryDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(gameHistoryDeadLetterQueue).to(deadLetterExchange).with(GAME_HISTORY_DLQ);
    }

    @Bean
    public Binding catalogGameResolvedBinding(Queue catalogGameResolvedQueue, TopicExchange catalogEventsExchange) {
        return BindingBuilder.bind(catalogGameResolvedQueue).to(catalogEventsExchange).with("catalog.game.resolved");
    }

    @Bean
    public Binding catalogGameResolvedDeadLetterBinding(Queue catalogGameResolvedDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(catalogGameResolvedDeadLetterQueue).to(deadLetterExchange).with(CATALOG_GAME_RESOLVED_DLQ);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
