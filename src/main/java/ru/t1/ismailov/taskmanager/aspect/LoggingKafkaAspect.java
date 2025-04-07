package ru.t1.ismailov.taskmanager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.support.SendResult;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import ru.t1.ismailov.taskmanager.annotation.Logging;
import ru.t1.ismailov.taskmanager.event.TaskStatusChangedEvent;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingKafkaAspect {

    @AfterReturning(
            pointcut = "execution(* ru.t1.ismailov.taskmanager.service.TaskStatusEventPublisher.sendTaskStatusChangedEvent(..))" +
                    " && @annotation(logger)",
            returning = "result"
    )
    public void logKafkaRecordMetadataAfterReturning(JoinPoint jp, Logging logger,
                                                     SendResult<Integer, TaskStatusChangedEvent> result) {
        log.debug("""
                        Message sent successfully.
                        Kafka Record Metadata:
                        - Topic: {}
                        - Partition: {}
                        - Offset: {}
                        - Timestamp: {}
                        - Producer record value: {}
                        """,
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset(),
                result.getRecordMetadata().timestamp(),
                result.getProducerRecord().value()
        );
    }

    @Around("execution(* ru.t1.ismailov.taskmanager.service.TaskStatusKafkaListener.handleTaskStatusChangedEvent(..))" +
            "&& @annotation(logger)")
    public Object logMeasureExecutionTime(ProceedingJoinPoint jp, Logging logger) throws Throwable {
        log.info("Received task status change event. Processing started...");
        try {
            Object result = jp.proceed();
            log.info("Task status change event processed successfully.");
            return result;
        } catch (Throwable ex) {
            log.error("Failed to process task status change event. Error: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @AfterReturning(
            pointcut = "execution(* ru.t1.ismailov.taskmanager.service.NotificationService.sendStatusChangeEmail(..))" +
                    " && @annotation(logger)",
            returning = "result"
    )
    public void logEmailSendingResult(JoinPoint jp, Logging logger, SimpleMailMessage result) {
        log.info("""
                        Email sent successfully.
                        Mail message details:
                        - From: {}
                        - Recipient: {}
                        - Subject: {}
                        - Body: {}
                        """,
                result.getFrom(),
                Arrays.toString(result.getTo()),
                result.getSubject(),
                result.getText()
        );
    }
}
