package ru.t1.ismailov.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.ismailov.taskmanager.annotation.Logging;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;

import static ru.t1.ismailov.taskmanager.config.KafkaConfig.TASK_STATUS_EVENTS_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskStatusKafkaListener {

    private final NotificationService notificationService;

    @Logging
    @KafkaListener(topics = TASK_STATUS_EVENTS_TOPIC)
    public void handleTaskStatusChangedEvent(TaskStatusChangedEvent event) {
        final String recipient = "i7mailov@ya.ru";

        notificationService.sendStatusChangeEmail(
                recipient,
                "Статус задачи изменён",
                String.format("Статус задачи №%d изменён: %s", event.taskId(), event.newStatus().getValue())
        );
    }
}
