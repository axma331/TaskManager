package ru.t1.ismailov.taskmanager.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TaskStatus {
    NEW("Новая задача"),
    UPDATING("Обновлена");

    private final String value;

    public String getValue() {
        return value;
    }
}
