package ru.t1.ismailov.taskmanager.dto;

import ru.t1.ismailov.taskmanager.model.TaskStatus;

public record TaskResponseDto(Integer id, String title, String description, Integer userId, TaskStatus status) {
}