package ru.t1.ismailov.taskmanager.kafka;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class KafkaTestContainer {

    public static final String TOPIC_NAME = "tasks-status-updates-topic-test";

    public static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName
                    .parse("apache/kafka:latest"))
                    .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                    .withReuse(true);

    static {
        KAFKA.start();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers(),
                    "spring.kafka.topics.task-status-event=" + TOPIC_NAME
            ).applyTo(context.getEnvironment());
        }
    }
}
