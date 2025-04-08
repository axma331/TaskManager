package ru.t1.ismailov.taskmanager.dto;

import ru.t1.ismailov.taskmanager.model.TaskStatus;

public record TaskRequestDto(String title, String description, Integer userId, TaskStatus status) {
}
