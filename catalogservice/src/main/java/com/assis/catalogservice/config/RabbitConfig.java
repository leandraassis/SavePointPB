package com.assis.catalogservice.config;

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
    public static final String DEAD_LETTER_EXCHANGE = "catalogservice.dlx";
    public static final String GAME_ADDED_QUEUE = "catalogservice.game-added";
    public static final String GAME_ADDED_DLQ = "catalogservice.game-added.dlq";
    public static final String GAME_RESOLVED_ROUTING_KEY = "catalog.game.resolved";

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
    public Queue gameAddedQueue() {
        return QueueBuilder.durable(GAME_ADDED_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(GAME_ADDED_DLQ)
                .build();
    }

    @Bean
    public Queue gameAddedDeadLetterQueue() {
        return QueueBuilder.durable(GAME_ADDED_DLQ).build();
    }

    @Bean
    public Binding gameAddedBinding(Queue gameAddedQueue, TopicExchange gameEventsExchange) {
        return BindingBuilder.bind(gameAddedQueue).to(gameEventsExchange).with("game.added");
    }

    @Bean
    public Binding gameAddedDeadLetterBinding(Queue gameAddedDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(gameAddedDeadLetterQueue).to(deadLetterExchange).with(GAME_ADDED_DLQ);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
