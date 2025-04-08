package ru.t1.ismailov.taskmanager.event;

import ru.t1.ismailov.taskmanager.model.TaskStatus;

public record TaskStatusChangedEvent(Integer taskId, TaskStatus newStatus) {
}
