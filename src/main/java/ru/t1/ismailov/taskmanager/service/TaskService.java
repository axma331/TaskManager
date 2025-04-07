package ru.t1.ismailov.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.ismailov.taskmanager.annotation.MeasureExecutionTime;
import ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler;
import ru.t1.ismailov.taskmanager.exception.TaskNotFoundException;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;
import ru.t1.ismailov.taskmanager.repository.TaskRepository;

import java.util.List;

@Service
@TaskExceptionHandler
public class TaskService {

    @Autowired
    private TaskRepository repository;
    @Autowired
    private TaskStatusEventPublisher eventPublisher;

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

    @Transactional
    public Task updateTask(Integer id, Task updatedTask) {
        Task foundTask = getTaskById(id);

        Task mergedTask = foundTask
                .setTitle(updatedTask.getTitle())
                .setDescription(updatedTask.getDescription())
                .setUserId(updatedTask.getUserId())
                .setStatus(TaskStatus.UPDATING);
        repository.save(mergedTask);

        eventPublisher.sendTaskStatusChangedEvent(mergedTask);

        return mergedTask;
    }

    public void removeTask(Integer id) {
        repository.deleteById(id);
    }
}
