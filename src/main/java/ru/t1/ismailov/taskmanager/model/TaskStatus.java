package ru.t1.ismailov.taskmanager.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskStatus {
    NEW("Новая задача"),
    UPDATING("Обновлена");

    private final String value;
}
