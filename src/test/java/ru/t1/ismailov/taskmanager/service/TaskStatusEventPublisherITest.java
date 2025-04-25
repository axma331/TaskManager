package ru.t1.ismailov.taskmanager.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;
import ru.t1.ismailov.taskmanager.exception.KafkaPublishingException;
import ru.t1.ismailov.taskmanager.kafka.EnableKafkaTestContainer;
import ru.t1.ismailov.taskmanager.kafka.KafkaTestContainer;
import ru.t1.ismailov.taskmanager.model.KafkaEventDelivery;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@ActiveProfiles("test")
@EnableKafkaTestContainer
@SpringBootTest
class TaskStatusEventPublisherITest {

    @Autowired
    private TaskStatusEventPublisher publisher;

    @MockitoSpyBean
    private KafkaTemplate<?, ?> template;

    @Mock
    private TaskStatusKafkaListener listener;


    @Test
    @DisplayName("publishTaskStatusChangedEvent — сообщение публикуется в Kafka, в топике появляется один корректный record и метод возвращает KafkaEventDelivery")
    void shouldPublishMessageAndReturnDelivery_whenPublishSucceeds() {
        Integer taskIdAndKye = 5;
        TaskStatus status = TaskStatus.UPDATING;
        Task task = new Task(taskIdAndKye, "title", "desc", 3, status);

        KafkaEventDelivery delivery = publisher.sendTaskStatusChangedEvent(task);

        Assertions.assertThat(delivery.payload().taskId()).isEqualTo(taskIdAndKye);

        try (var consumer = createConsumerAndSubscribeToTopic()) {

            var records = consumer.poll(Duration.ofSeconds(10));

            Assertions.assertThat(records.count()).as("должно прийти ровно одно сообщение").isEqualTo(1);

            var record = records.iterator().next();

            Assertions.assertThat(record.key()).isEqualTo(taskIdAndKye);
            Assertions.assertThat(record.value())
                    .extracting(TaskStatusChangedEvent::taskId,
                            TaskStatusChangedEvent::newStatus)
                    .containsExactly(taskIdAndKye, status);
        }
    }

    @Test
    @DisplayName("sendTaskStatusChangedEvent — при ошибке kafka-producer'а выбрасывается KafkaPublishingException")
    void shouldThrowKafkaPublishingException_whenProducerSendFails() {
        Task task = new Task().setId(1).setStatus(TaskStatus.NEW);
        Mockito.doThrow(RuntimeException.class).when(template).send(
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any()
        );

        Assertions.assertThatThrownBy(() -> publisher.sendTaskStatusChangedEvent(task))
                .as("должно выброситься исключение KafkaPublishingException")
                .isInstanceOf(KafkaPublishingException.class);
    }

    private static KafkaConsumer<Integer, TaskStatusChangedEvent> createConsumerAndSubscribeToTopic() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaTestContainer.KAFKA.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-event-publisher");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TaskStatusChangedEvent.class.getName());

        KafkaConsumer<Integer, TaskStatusChangedEvent> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(List.of(KafkaTestContainer.TOPIC_NAME));

        return consumer;
    }
}
