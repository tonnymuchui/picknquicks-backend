package com.picknquicks.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name("product-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productStockChangedTopic() {
        return TopicBuilder.name("product-stock-changed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productViewedTopic() {
        return TopicBuilder.name("product-viewed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}