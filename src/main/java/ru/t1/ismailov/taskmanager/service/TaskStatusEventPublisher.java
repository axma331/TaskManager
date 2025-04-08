package ru.t1.ismailov.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.t1.ismailov.taskmanager.annotation.Logging;
import ru.t1.ismailov.taskmanager.model.KafkaEventDelivery;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;
import ru.t1.ismailov.taskmanager.exception.KafkaPublishingException;
import ru.t1.ismailov.taskmanager.model.Task;

@Service
@RequiredArgsConstructor
public class TaskStatusEventPublisher {

    private final KafkaTemplate<Integer, TaskStatusChangedEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.task-status-event}")
    private String topic;

    @Logging
    public KafkaEventDelivery sendTaskStatusChangedEvent(Task task) {
        try {
            var changedEvent = new TaskStatusChangedEvent(task.getId(), task.getStatus());
            kafkaTemplate.send(
                    topic,
                    task.getId(),
                    changedEvent
            );
            kafkaTemplate.flush();

            return new KafkaEventDelivery(topic, changedEvent);
        } catch (Exception ex) {
            throw new KafkaPublishingException(ex);
        }
    }
}
