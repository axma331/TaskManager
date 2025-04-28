package ru.t1.ismailov.taskmanager.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.exception.TaskNotFoundException;
import ru.t1.ismailov.taskmanager.kafka.EnableKafkaTestContainer;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@EnableKafkaTestContainer
class TaskServiceITest {

    @Autowired
    private TaskService service;

    @MockitoSpyBean
    private TaskStatusEventPublisher publisher;


    @Test
    @DisplayName("createTask — при валидном DTO сохраняет задачу и возвращает DTO с новым ID")
    @Sql(scripts = "classpath:cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
    void createTask_whenValidDto_thenSaveTaskAndReturnDto() {
        var dto = new TaskRequestDto("title", "desc", 5, null);
        Task expected = new Task(1, "title", "desc", 5, TaskStatus.NEW);

        TaskResponseDto result = service.createTask(dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.id(), expected.getId());
        Assertions.assertEquals(result.status(), expected.getStatus());
    }


    @Test
    @DisplayName("getTaskById — при отсутствии задачи выбрасывает TaskNotFoundException")
    @Sql(scripts = "classpath:cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
    void getTaskById_whenNotFound_thenThrowTaskNotFoundException() {
        Assertions.assertThrows(TaskNotFoundException.class, () -> service.getTaskById(1));
    }

    @Test
    @DisplayName("updateTask — при смене статуса вызывает отправку события через publisher")
    @Sql(scripts = "classpath:fill_users_for_update_test.sql", executionPhase = BEFORE_TEST_METHOD)
    void updateTask_whenStatusChanged_thenCallPublisher() {

        Integer id = 1;
        var updatedDto = new TaskRequestDto(null, "new desc_1", null, null);
        var expectedDto = new TaskRequestDto("title_1", "new desc_1", 1, TaskStatus.UPDATING);

        var returnDto = service.updateTask(id, updatedDto);

        Assertions.assertNotNull(returnDto);
        Assertions.assertEquals(returnDto.title(), expectedDto.title());
        Assertions.assertEquals(returnDto.description(), expectedDto.description());
        Assertions.assertEquals(returnDto.userId(), expectedDto.userId());
        Assertions.assertEquals(returnDto.status(), expectedDto.status());

        Mockito.verify(publisher).sendTaskStatusChangedEvent(
                Mockito.argThat(task -> task.getStatus() == TaskStatus.UPDATING)
        );
    }

    @Test
    @DisplayName("updateTask — если обновляемой сущности нет или она уже со статусом UPDATING, то publisher не вызывается")
    @Sql(scripts = "classpath:fill_users_for_update_test.sql", executionPhase = BEFORE_TEST_METHOD)
    void updateTask_whenStatusNotChanged_thenDoNotCallPublisher() {
        var updatedDto = new TaskRequestDto("new title", null, null, null);

        Assertions.assertThrows(TaskNotFoundException.class,
                () -> service.updateTask(99, updatedDto));

        service.updateTask(2, updatedDto);

        Mockito.verify(publisher, Mockito.never()).sendTaskStatusChangedEvent(any());

    }
}
