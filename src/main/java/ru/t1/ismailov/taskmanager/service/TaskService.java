package ru.t1.ismailov.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.ismailov.taskmanager.annotation.MeasureExecutionTime;
import ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler;
import ru.t1.ismailov.taskmanager.exception.TaskNotFoundException;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.repository.TaskRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@TaskExceptionHandler
public class TaskService {

    private final TaskRepository repository;

    @MeasureExecutionTime(logOnError = true)
    public Task createTask(Task task) {
        return repository.save(task);
    }

    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    public Task getTaskById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task updateTask(Integer id, Task updatedTask) {
        Task foundTask = getTaskById(id);

        return repository.save(foundTask
                .setTitle(updatedTask.getTitle())
                .setDescription(updatedTask.getDescription())
                .setUserId(updatedTask.getUserId()));
    }

    public void removeTask(Integer id) {
        repository.deleteById(id);
    }
}
