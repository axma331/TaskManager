package ru.t1.ismailov.taskmanager.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;

import static ru.t1.ismailov.taskmanager.config.KafkaConfig.TASK_STATUS_EVENTS_TOPIC;

@Service
public class TaskStatusKafkaListener {

    @KafkaListener(topics = TASK_STATUS_EVENTS_TOPIC)
    public void handleTaskStatusChangedEvent(TaskStatusChangedEvent event) {
        System.out.println("Received task status event: " + event);
    }
}
