package ru.t1.ismailov.taskmanager.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.model.Task;
import ru.t1.ismailov.taskmanager.model.TaskStatus;


class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper();

    @Test
    @DisplayName("toEntity — когда Dto передаёт все поля, Entity получает те же значения и статус NEW")
    void toEntity_whenDtoWithAllFields_thenEntityWithSameFieldsAndStatusNew() {

        var dto = new TaskRequestDto("title", "desc", 5, null);
        var expected = new Task(null, "title", "desc", 5, TaskStatus.NEW);

        Task entity = mapper.toEntity(dto);

        Assertions.assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("toDto: Преобразование всех полей Entity в Dto")
    void toDto_shouldTransferAllFields() {
        var entity = new Task(1, "title", "desc", 5, TaskStatus.UPDATING);
        var expected = new TaskResponseDto(1, "title", "desc", 5, TaskStatus.UPDATING);

        TaskResponseDto dto = mapper.toDto(entity);

        Assertions.assertThat(dto)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("toDto — если у Entity id == null, то и в Dto поле id будет null")
    void toDto_whenEntityIdIsNull_thenDtoIdIsNull() {
        var entity = new Task(null, "title", "desc", 2, TaskStatus.NEW);

        TaskResponseDto dto = mapper.toDto(entity);

        Assertions.assertThat(dto.id()).isNull();
    }
}