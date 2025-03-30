package ru.t1.ismailov.taskmanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.ismailov.taskmanager.annotation.LoggingRequest;
import ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/tasks" )
@RequiredArgsConstructor
@LoggingRequest
public class TaskController {

    private final TaskService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody Task task) {
        return service.createTask(task);
    }

    @GetMapping("/{id}" )
    @TaskExceptionHandler
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTaskById(id));
    }

    @PutMapping("/{id}" )
    @TaskExceptionHandler
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(service.updateTask(id, task));
    }

    @DeleteMapping("/{id}" )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        service.removeTask(id);
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return service.getAllTasks();
    }
}
