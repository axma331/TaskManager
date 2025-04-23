package ru.t1.ismailov.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.exception.TaskNotFoundException;
import ru.t1.ismailov.taskmanager.model.TaskStatus;
import ru.t1.ismailov.taskmanager.service.TaskService;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
class TaskControllerTest {

    @MockitoBean
    private TaskService service;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper json;


    @Test
    @DisplayName("POST /tasks — при валидном запросе возвращает 201 и делегирует в сервис")
    void createTask_whenValidRequest_thenReturns201AndDelegatesToService() throws Exception {
        var taskDto = new TaskRequestDto("title", "desc", 5, null);
        var requestBody = json.writeValueAsString(taskDto);
        var mockRequest = post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated());

        Mockito.verify(service).createTask(taskDto);
    }

    @Test
    @DisplayName("GET /tasks/{id} — при существующей задаче возвращает 200 и тело задачи")
    void getTaskById_whenExists_thenReturns200AndTask() throws Exception {
        Integer id = 1;
        var responseDto = new TaskResponseDto(id, "title", "desc", 5, TaskStatus.UPDATING);
        Mockito.when(service.getTaskById(id)).thenReturn(responseDto);
        var mockRequest = get("/tasks/{id}", id);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(responseDto.title()))
                .andExpect(jsonPath("$.description").value(responseDto.description()))
                .andExpect(jsonPath("$.userId").value(responseDto.userId()))
                .andExpect(jsonPath("$.status").value(responseDto.status().name()));

        Mockito.verify(service).getTaskById(id);
    }

    @Test
    @DisplayName("GET /tasks/{id} — при отсутствии задачи возвращает 404 и сообщение об ошибке")
    void getTaskById_whenNotFound_thenReturns404AndErrorMessage() throws Exception {
        Integer id = 1;
        Mockito.when(service.getTaskById(id)).thenThrow(new TaskNotFoundException(id));
        var mockRequest = get("/tasks/{id}", id);

        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + id));

        Mockito.verify(service).getTaskById(id);
    }

    @Test
    @DisplayName("PUT /tasks/{id} — при успешном обновлении возвращает 200 и тело задачи")
    void updateTask_whenValidRequest_thenReturns200AndUpdatedTask() throws Exception {
        Integer id = 1;
        var requestDto = new TaskRequestDto("title", "desc", 5, TaskStatus.NEW);
        var responseDto = new TaskResponseDto(id, "title", "desc", 5, TaskStatus.UPDATING);
        Mockito.when(service.updateTask(id, requestDto)).thenReturn(responseDto);
        String requestBody = json.writeValueAsString(requestDto);
        var mockRequest = put("/tasks/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(responseDto.title()))
                .andExpect(jsonPath("$.description").value(responseDto.description()))
                .andExpect(jsonPath("$.userId").value(responseDto.userId()))
                .andExpect(jsonPath("$.status").value(responseDto.status().name()));

        Mockito.verify(service).updateTask(id, requestDto);
    }

    @Test
    @DisplayName("PUT /tasks/{id} — при отсутствии задачи возвращает 404 и сообщение об ошибке")
    void updateTask_whenNotFound_thenReturns404AndErrorMessage() throws Exception {
        Integer id = 1;
        var requestDto = new TaskRequestDto("title", "desc", 5, TaskStatus.NEW);
        Mockito.when(service.updateTask(id, requestDto)).thenThrow(new TaskNotFoundException(id));

        String requestBody = json.writeValueAsString(requestDto);
        var mockRequest = put("/tasks/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + id));

        Mockito.verify(service).updateTask(id, requestDto);
    }

    @Test
    @DisplayName("DELETE /tasks/{id} — при удалении возвращает 204")
    void deleteTask_whenExists_thenReturns204() throws Exception {
        Integer id = 1;
        var mockRequest = delete("/tasks/{id}", id);

        mockMvc.perform(mockRequest)
                .andExpect(status().isNoContent());

        Mockito.verify(service).removeTask(id);
    }

    @Test
    @DisplayName("GET /tasks — при наличии задач возвращает 200 и непустой список")
    void getAllTasks_whenTasksExist_thenReturns200AndNonEmptyList() throws Exception {
        var responseDtoList = List.of(
                new TaskResponseDto(1, "title_1", "desc_1", 5, TaskStatus.NEW),
                new TaskResponseDto(2, "title_2", "desc_2", 6, TaskStatus.UPDATING)
        );
        Mockito.when(service.getAllTasks()).thenReturn(responseDtoList);
        var mockRequest = get("/tasks");

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(responseDtoList.size()))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("title_1"))
                .andExpect(jsonPath("$[0].description").value("desc_1"))
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("title_2"))
                .andExpect(jsonPath("$[1].description").value("desc_2"))
                .andExpect(jsonPath("$[1].userId").value(6))
                .andExpect(jsonPath("$[1].status").value("UPDATING"));

        Mockito.verify(service).getAllTasks();
    }

    @Test
    @DisplayName("GET /tasks — при отсутствии задач возвращает 200 и пустой список")
    void getAllTasks_whenNoTasks_thenReturns200AndEmptyList() throws Exception {
        List<TaskResponseDto> responseDtoEmptyList = Collections.emptyList();
        Mockito.when(service.getAllTasks()).thenReturn(responseDtoEmptyList);
        var mockRequest = get("/tasks");

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        Mockito.verify(service).getAllTasks();
    }
}
