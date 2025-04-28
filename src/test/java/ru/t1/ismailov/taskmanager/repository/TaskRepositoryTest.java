package ru.t1.ismailov.taskmanager.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;

@ActiveProfiles("test")
@SpringBootTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repo;

    @Test
    @DisplayName("save / find / delete работают корректно")
    void basicCrud() {
        Task saved = repo.save(new Task(null, "title", "desc", 1, TaskStatus.NEW));

        Assertions.assertThat(saved.getId()).isNotNull();

        Task found = repo.findById(saved.getId()).orElseThrow();
        Assertions.assertThat(found).usingRecursiveComparison().isEqualTo(saved);

        repo.delete(found);
        Assertions.assertThat(repo.findById(saved.getId())).isEmpty();
    }
}
