package com.better.community.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

/**
 * 配置es的 RestHighLevelClient
 * @Date 7/21/2022
 */
@Configuration
public class ESConfig {
    @Value("${elasticsearch.url}")
    private String esUrl;

    @Bean
    RestHighLevelClient restHighLevelClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(esUrl)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}
