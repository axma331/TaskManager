package ru.t1.ismailov.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.t1.ismailov.taskmanager.annotation.Logging;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class TaskStatusKafkaListener {

    private final NotificationService notificationService;

    @Value("${spring.mail.recipient}")
    private String recipient;

    @Logging
    @KafkaListener(topics = "${spring.kafka.topics.task-status-event}")
    public void handleTaskStatusChangedEvent(@Payload List<TaskStatusChangedEvent> events, Acknowledgment ack) {

        try {
            events.forEach(event -> {
                notificationService.sendStatusChangeEmail(
                        recipient,
                        "Статус задачи изменён",
                        String.format("Статус задачи №%d изменён: %s", event.taskId(), event.newStatus().getValue())
                );
            });
        } finally {
            ack.acknowledge();
        }
    }
}