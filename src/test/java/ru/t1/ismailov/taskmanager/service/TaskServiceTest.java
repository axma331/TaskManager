package ru.t1.ismailov.taskmanager.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.exception.TaskNotFoundException;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;
import ru.t1.ismailov.taskmanager.repository.TaskRepository;
import ru.t1.ismailov.taskmanager.utils.TaskMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository repository;
    @Mock
    TaskStatusEventPublisher publisher;
    @Spy
    TaskMapper taskMapper = new TaskMapper();

    @InjectMocks
    private TaskService service;


    @Test
    @DisplayName("createTask — при валидном DTO сохраняет задачу и возвращает DTO с новым ID")
    void createTask_whenValidDto_thenSaveTaskAndReturnDto() {
        var dto = new TaskRequestDto("title", "desc", 5, null);
        Task expected = new Task(1, "title", "desc", 5, TaskStatus.NEW);
        Mockito.when(repository.save(any(Task.class))).thenReturn(expected);

        TaskResponseDto result = service.createTask(dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.id(), expected.getId());
        Assertions.assertEquals(result.status(), expected.getStatus());
        Mockito.verify(repository).save(any(Task.class));
    }

    @Test
    @DisplayName("getTaskById — при отсутствии задачи выбрасывает TaskNotFoundException")
    void getTaskById_whenNotFound_thenThrowTaskNotFoundException() {
        Mockito.when(repository.findById(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(TaskNotFoundException.class, () -> service.getTaskById(1));
    }

    @Test
    @DisplayName("updateTask — при смене статуса вызывает отправку события через publisher")
    void updateTask_whenStatusChanged_thenCallPublisher() {
        Integer id = 1;
        Task foundTask = new Task(id, "title", "desc", 5, TaskStatus.NEW);
        Mockito.when(repository.findById(any())).thenReturn(Optional.of(foundTask));
        Mockito.when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var updatedDto = new TaskRequestDto(null, "new desc", null, TaskStatus.UPDATING);

        service.updateTask(id, updatedDto);

        Mockito.verify(publisher).sendTaskStatusChangedEvent(
                Mockito.argThat(task -> task.getStatus() == TaskStatus.UPDATING)
        );
    }

    @Test
    @DisplayName("updateTask — без смены статуса не вызывает publisher")
    void updateTask_whenStatusNotChanged_thenDoNotCallPublisher() {
        Integer id = 1;
        var updatedDto = new TaskRequestDto("new title", null, null, null);
        var foundTask = new Task(id, "title", "desc", 5, TaskStatus.UPDATING);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(foundTask));
        Mockito.when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateTask(id, updatedDto);

        Mockito.verify(publisher, Mockito.never()).sendTaskStatusChangedEvent(any());
    }
}
