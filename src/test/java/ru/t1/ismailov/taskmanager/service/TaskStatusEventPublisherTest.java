package ru.t1.ismailov.taskmanager.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;
import ru.t1.ismailov.taskmanager.exception.KafkaPublishingException;
import ru.t1.ismailov.taskmanager.model.KafkaEventDelivery;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;


@ExtendWith(MockitoExtension.class)
class TaskStatusEventPublisherTest {

    @Mock
    private KafkaTemplate<Integer, TaskStatusChangedEvent> template;

    @InjectMocks
    private TaskStatusEventPublisher publisher;

    @BeforeEach
    void initTopic() {
        ReflectionTestUtils.setField(publisher, "topic", "tasks-status-updates-topic");
    }

    @Test
    @DisplayName("sendTaskStatusChangedEvent — при успешной отправке в Kafka возвращает KafkaEventDelivery с корректным payload")
    void sendTaskStatusChangedEvent_whenSendSucceeds_thenReturnsKafkaEventDelivery() {
        Task task = new Task(5, "title", "desc", 3, TaskStatus.UPDATING);

        KafkaEventDelivery delivery = publisher.sendTaskStatusChangedEvent(task);

        Mockito.verify(template).send(
                Mockito.eq("tasks-status-updates-topic"),
                Mockito.eq(5),
                Mockito.any(TaskStatusChangedEvent.class)
        );
        Mockito.verify(template).flush();
        Assertions.assertThat(delivery.payload().taskId()).isEqualTo(5);
    }

    @Test
    @DisplayName("sendTaskStatusChangedEvent — при ошибке отправки в Kafka выбрасывает KafkaPublishingException")
    void sendTaskStatusChangedEvent_whenSendFails_thenThrowsKafkaPublishingException() {
        Task task = new Task().setId(1).setStatus(TaskStatus.NEW);
        Mockito.doThrow(RuntimeException.class).when(template).send(
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.any()
        );

        Assertions.assertThatThrownBy(() -> publisher.sendTaskStatusChangedEvent(task))
                .isInstanceOf(KafkaPublishingException.class);
    }
}
