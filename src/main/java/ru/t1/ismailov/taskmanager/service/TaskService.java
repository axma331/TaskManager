package ru.t1.ismailov.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.ismailov.taskmanager.annotation.MeasureExecutionTime;
import ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.exception.TaskNotFoundException;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;
import ru.t1.ismailov.taskmanager.repository.TaskRepository;
import ru.t1.ismailov.taskmanager.utils.TaskMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@TaskExceptionHandler
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final TaskStatusEventPublisher eventPublisher;
    private final TaskMapper taskMapper;

    @MeasureExecutionTime(logOnError = true)
    public TaskResponseDto createTask(TaskRequestDto taskDto) {
        Task task = taskMapper.toEntity(taskDto);
        return taskMapper.toDto(repository.save(task));
    }

    public List<TaskResponseDto> getAllTasks() {
        return repository.findAll().stream()
                .map(taskMapper::toDto)
                .toList();
    }

    public TaskResponseDto getTaskById(Integer id) {
        return repository.findById(id)
                .map(taskMapper::toDto)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    public TaskResponseDto updateTask(Integer id, TaskRequestDto updatedDto) {
        TaskStatus oldStatus;
        TaskStatus newStatus;

        Task foundTask = repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        oldStatus = foundTask.getStatus();

        getUpdatedEntityFromDto(foundTask, updatedDto);

        Task updatedTask = repository.save(foundTask);
        newStatus = updatedTask.getStatus();

        if (checkAndLogStatusChange(oldStatus,  newStatus)) {
            eventPublisher.sendTaskStatusChangedEvent(updatedTask);
        }
        return taskMapper.toDto(updatedTask);
    }

    public void removeTask(Integer id) {
        repository.deleteById(id);
    }

    private void getUpdatedEntityFromDto(Task task, TaskRequestDto dto) {

        Optional.ofNullable(dto.title()).ifPresent(task::setTitle);
        Optional.ofNullable(dto.description()).ifPresent(task::setDescription);
        Optional.ofNullable(dto.userId()).ifPresent(task::setUserId);

        boolean isModified = Stream.of(dto.title(), dto.description(), dto.userId()).anyMatch(Objects::nonNull);

        if (dto.status() != null) {
            task.setStatus(dto.status());
        } else if (isModified) {
            task.setStatus(TaskStatus.UPDATING);
        }
    }

    private boolean checkAndLogStatusChange(TaskStatus oldStatus, TaskStatus newStatus) {
        boolean isUpdated = newStatus != oldStatus;

        log.info("Task status {}updated. Old status: {}, new status: {}.",
                isUpdated ? "" : "not ", oldStatus, newStatus);

        return isUpdated;
    }
}