package ru.t1.ismailov.taskmanager.service;


import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;
import ru.t1.ismailov.taskmanager.kafka.EnableKafkaTestContainer;
import ru.t1.ismailov.taskmanager.kafka.KafkaTestContainer;
import ru.t1.ismailov.taskmanager.model.TaskStatus;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@EnableKafkaTestContainer
@SpringBootTest
class TaskStatusKafkaListenerITest {

    @Autowired
    private KafkaTemplate<Integer, TaskStatusChangedEvent> kafkaTemplate;

    @MockitoBean
    private NotificationService mailer;

    @Mock
    JavaMailSender mailSender;

    private final String topic = KafkaTestContainer.TOPIC_NAME;

    @Test
    @DisplayName("При одном событии Listener отправляет ровно одно письмо")
    void whenSingleEvent_thenSendOneEmail() {
        var event = new TaskStatusChangedEvent(5, TaskStatus.NEW);

        kafkaTemplate.send(topic, event.taskId(), event);
        kafkaTemplate.flush();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        verify(mailer).sendStatusChangeEmail(
                                eq("i7mailov@ya.ru"),
                                eq("Статус задачи изменён"),
                                contains("№5")
                        ));
    }

    @Test
    @DisplayName("Batch-обработка: отправляются два сообщения")
    void whenTwoEventsInBatch_thenSendTwoEmails() {
        var events = List.of(
                new TaskStatusChangedEvent(6, TaskStatus.NEW),
                new TaskStatusChangedEvent(7, TaskStatus.UPDATING)
        );

        events.forEach(e -> kafkaTemplate.send(topic, e.taskId(), e));
        kafkaTemplate.flush();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        verify(mailer, timeout(4_000).times(2))
                                .sendStatusChangeEmail(
                                        anyString(),
                                        eq("Статус задачи изменён"),
                                        anyString()
                                ));
    }
}
