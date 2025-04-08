package ru.t1.ismailov.taskmanager.model;

import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;

public record KafkaEventDelivery(String topic, TaskStatusChangedEvent payload) {
}