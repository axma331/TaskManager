package ru.t1.ismailov.taskmanager.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import ru.t1.ismailov.taskmanager.controller.TaskController;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice(assignableTypes = TaskController.class)
@Slf4j
public class TaskExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleTaskNotFound(TaskNotFoundException ex, WebRequest request) {
        log.warn("Not Found (404) | Path: {}", request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Not Found",
                        "message", ex.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }
}