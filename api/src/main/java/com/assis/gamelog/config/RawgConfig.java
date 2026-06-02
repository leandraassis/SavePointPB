package com.assis.gamelog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RawgConfig {
    @Value("${rawg.api.base-url}")
    private String baseUrl;

    @Bean
    public RestClient rawgRestClient() {
        return RestClient.builder().baseUrl(baseUrl).defaultHeader("Accept", "application/json").build();
    }

}
