package ru.t1.ismailov.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.t1.ismailov.taskmanager.annotation.LogKafkaMetadata;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;
import ru.t1.ismailov.taskmanager.exception.KafkaPublishingException;
import ru.t1.ismailov.taskmanager.model.Task;

import static ru.t1.ismailov.taskmanager.config.KafkaConfig.TASK_STATUS_EVENTS_TOPIC;

@Service
@RequiredArgsConstructor
public class TaskStatusEventPublisher {

    private final KafkaTemplate<Integer, TaskStatusChangedEvent> kafkaTemplate;

    @LogKafkaMetadata
    public SendResult<Integer, TaskStatusChangedEvent> sendTaskStatusChangedEvent(Task task) {
        try {
            var result = kafkaTemplate.send(
                            TASK_STATUS_EVENTS_TOPIC,
                            task.getId(),
                            new TaskStatusChangedEvent(task.getId(), task.getStatus()))
                    .get();

            return result;
        } catch (Exception ex) {
            throw new KafkaPublishingException(ex);
        }
    }
}
