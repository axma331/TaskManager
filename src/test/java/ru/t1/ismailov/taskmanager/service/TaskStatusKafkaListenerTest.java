package ru.t1.ismailov.taskmanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;
import ru.t1.ismailov.taskmanager.model.TaskStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
class TaskStatusKafkaListenerTest {

    @Mock
    private NotificationService mailer;
    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private TaskStatusKafkaListener listener;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "recipient", "i7mailov@ya.ru");
    }

    @Test
    @DisplayName("handleTaskStatusChangedEvent — при одном событии отправляет email и подтверждает обработку")
    void handleTaskStatusChangedEvent_whenSingleEvent_thenSendsEmailAndAcks() {
        var event = new TaskStatusChangedEvent(5, TaskStatus.NEW);

        listener.handleTaskStatusChangedEvent(List.of(event), ack);

        Mockito.verify(mailer).sendStatusChangeEmail(
                Mockito.eq("i7mailov@ya.ru"),
                Mockito.anyString(),
                Mockito.contains("№5")
        );
        Mockito.verify(ack).acknowledge();
    }

    @Test
    @DisplayName("handleTaskStatusChangedEvent — при нескольких событиях отправляет по одному письму на каждое и подтверждает обработку")
    void handleTaskStatusChangedEvent_whenMultipleEvents_thenSendsEmailForEachAndAcks() {
        var events = List.of(
                new TaskStatusChangedEvent(1, TaskStatus.NEW),
                new TaskStatusChangedEvent(2, TaskStatus.UPDATING)
        );

        listener.handleTaskStatusChangedEvent(events, ack);

        Mockito.verify(mailer, Mockito.times(2))
                .sendStatusChangeEmail(
                        Mockito.eq("i7mailov@ya.ru"),
                        Mockito.anyString(),
                        Mockito.anyString()
                );
        Mockito.verify(ack).acknowledge();
    }

    @Test
    @DisplayName("handleTaskStatusChangedEvent — при ошибке отправки email подтверждает обработку и пробрасывает исключение")
    void handleTaskStatusChangedEvent_whenMailerThrows_thenAcksAndRethrows() {
        var event = new TaskStatusChangedEvent(5, TaskStatus.NEW);
        Mockito.doThrow(new RuntimeException("SMTP error"))
                .when(mailer)
                .sendStatusChangeEmail(
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString()
                );

        assertThatThrownBy(() ->
                listener.handleTaskStatusChangedEvent(List.of(event), ack)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP error");

        Mockito.verify(ack).acknowledge();
    }
}
