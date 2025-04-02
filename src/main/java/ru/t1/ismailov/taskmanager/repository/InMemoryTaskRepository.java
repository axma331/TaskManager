package ru.t1.ismailov.taskmanager.repository;

import org.springframework.stereotype.Repository;
import ru.t1.ismailov.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskRepository {

    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong(1);


    public List<Task> findAll() {
        return new ArrayList<>(tasks);
    }

    public Optional<Task> findById(Long id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(counter.getAndIncrement());
        } else {
            tasks.removeIf(t -> t.getId().equals(task.getId()));
        }
        tasks.add(task);

        return task;
    }

    public void deleteById(Long id) {
        tasks.removeIf(task -> task.getId().equals(id));
    }
}
