package ru.t1.ismailov.taskmanager.utils;

import org.springframework.stereotype.Component;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequestDto dto) {
        return new Task()
                .setTitle(dto.title())
                .setDescription(dto.description())
                .setUserId(dto.userId())
                .setStatus(TaskStatus.NEW);
    }

    public TaskResponseDto toDto(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getUserId(),
                task.getStatus()
        );
    }
}
