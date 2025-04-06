package ru.t1.ismailov.taskmanager.exception;

public class KafkaPublishingException extends RuntimeException {
    public KafkaPublishingException(Throwable cause) {
        super("Failed to publish message to Kafka", cause);
    }
}
