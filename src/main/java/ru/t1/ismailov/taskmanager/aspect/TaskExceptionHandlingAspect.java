package ru.t1.ismailov.taskmanager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.t1.ismailov.taskmanager.exception.TaskNotFoundException;

import java.time.LocalDateTime;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class TaskExceptionHandlingAspect extends BaseAspect {

    @Around("@annotation(ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler)" )
    public Object handleTaskNotFound(ProceedingJoinPoint jp) throws Throwable {
        try {
            return jp.proceed();
        } catch (TaskNotFoundException ex) {
            log.error("Task not found: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Not Found",
                            "message", ex.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    @AfterThrowing(
            pointcut = "@within(ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler)",
            throwing = "ex"
    )
    public void logUnexpectedExceptions(JoinPoint jp, Throwable ex) {
        if (!(ex instanceof TaskNotFoundException)) {
            log.error("Unexpected error in {} method: {}", getClassAndMethodName(jp), ex.getMessage(), ex);
        }
    }
}
