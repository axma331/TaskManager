package ru.t1.ismailov.taskmanager.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TASK_STATUS_EVENTS_TOPIC = "tasks-status-updates-topic";

    @Bean
    NewTopic taskUpdatesTopic() {
        return TopicBuilder.name(TASK_STATUS_EVENTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
