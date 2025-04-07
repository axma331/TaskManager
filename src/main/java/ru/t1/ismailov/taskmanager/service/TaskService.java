package ru.t1.ismailov.taskmanager.service;

import lombok.RequiredArgsConstructor;
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
        Task foundTask = repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        updateEntityFromDto(foundTask, updatedDto);
        foundTask.setStatus(TaskStatus.UPDATING);

        Task updatedTask = repository.save(foundTask);
        eventPublisher.sendTaskStatusChangedEvent(updatedTask);

        return taskMapper.toDto(updatedTask);
    }

    public void removeTask(Integer id) {
        repository.deleteById(id);
    }

    private void updateEntityFromDto(Task task, TaskRequestDto dto) {
        if (dto.title() != null) {
            task.setTitle(dto.title());
        }
        if (dto.description() != null) {
            task.setDescription(dto.description());
        }
        if (dto.userId() != null) {
            task.setUserId(dto.userId());
        }
    }
}