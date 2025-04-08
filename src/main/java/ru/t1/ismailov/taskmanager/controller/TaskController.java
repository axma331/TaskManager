package ru.t1.ismailov.taskmanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.t1.ismailov.taskmanager.annotation.LogRequest;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@LogRequest
public class TaskController {

    private final TaskService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto createTask(@RequestBody TaskRequestDto taskDto) {
        return service.createTask(taskDto);
    }

    @GetMapping("/{id}")
    public TaskResponseDto getTaskById(@PathVariable Integer id) {
        return service.getTaskById(id);
    }

    @PutMapping("/{id}")
    public TaskResponseDto updateTask(@PathVariable Integer id, @RequestBody TaskRequestDto taskDto) {
        return service.updateTask(id, taskDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Integer id) {
        service.removeTask(id);
    }

    @GetMapping
    public List<TaskResponseDto> getAllTasks() {
        return service.getAllTasks();
    }
}
