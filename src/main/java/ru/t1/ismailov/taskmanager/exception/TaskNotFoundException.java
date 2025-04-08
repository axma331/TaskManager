package ru.t1.ismailov.taskmanager.exception;

import java.util.NoSuchElementException;

public class TaskNotFoundException extends NoSuchElementException {
    public TaskNotFoundException(Integer id) {
        super("Task not found with id: " + id);
    }
}
