package ru.t1.ismailov.taskmanager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;

@Aspect
@Component
@Slf4j
public class LoggingKafkaAspect {

    @AfterReturning(
            pointcut = "@annotation(ru.t1.ismailov.taskmanager.annotation.LogKafkaMetadata)",
            returning = "result"
    )
    public void logKafkaRecordMetadataAfterReturning(JoinPoint jp, SendResult<Integer, TaskStatusChangedEvent> result) {
        log.debug("Kafka Record Metadata - Topic: {}, Partition: {}, Offset: {}, Timestamp: {}, Producer ACK: {}",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset(),
                result.getRecordMetadata().timestamp(),
                result.getProducerRecord().value());

        log.info("Message sent successfully, {}", result.getRecordMetadata().toString());
    }
}
