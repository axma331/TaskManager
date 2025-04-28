package ru.t1.ismailov.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.kafka.EnableKafkaTestContainer;
import ru.t1.ismailov.taskmanager.model.TaskStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableKafkaTestContainer
class TaskControllerITest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper json;


    @Test
    @DisplayName("POST /tasks создаёт задачу и возвращает код 201, GET /tasks/{id} возвращает созданную задачу и код 200")
    @Sql(scripts = "classpath:cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
    void shouldCreateTaskAndThenFetchItById() throws Exception {
        var requestDto = new TaskRequestDto("title_IT", "desc_IT", 22, null);
        //post
        String requestJsonBody = json.writeValueAsString(requestDto);
        var postRequest = post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJsonBody);

        String postResponseJson = mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(requestDto.title()))
                .andExpect(jsonPath("$.description").value(requestDto.description()))
                .andExpect(jsonPath("$.userId").value(requestDto.userId()))
                .andExpect(jsonPath("$.status").value(TaskStatus.NEW.name()))
                .andReturn()
                .getResponse().getContentAsString();
        var createdDto = json.readValue(postResponseJson, TaskResponseDto.class);

        assertThat(createdDto.id()).isNotNull();

        //get
        var getRequest = get("/tasks/{id}", createdDto.id());

        String getResponseJson = mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var gettingDto = json.readValue(getResponseJson, TaskResponseDto.class);

        assertThat(gettingDto)
                .usingRecursiveComparison()
                .isEqualTo(createdDto);
    }

    @Test
    @DisplayName("GET /tasks/{id} — при отсутствии задачи возвращает 404 и сообщение об ошибке")
    @Sql(scripts = "classpath:cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
    void getTaskById_whenNotFound_thenReturns404AndErrorMessage() throws Exception {
        Integer id = 1;
        var mockRequest = get("/tasks/{id}", id);

        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + id));
    }

    @Test
    @DisplayName("PUT /tasks/{id} — при успешном обновлении возвращает 200 и тело задачи")
    @Sql(scripts = "classpath:fill_users_for_update_test.sql", executionPhase = BEFORE_TEST_METHOD)
    void updateTask_whenValidRequest_thenReturns200AndUpdatedTask() throws Exception {
        Integer id = 1;
        var requestDto = new TaskRequestDto(null, "new desc_1", 5, null);
        String requestBody = json.writeValueAsString(requestDto);

        var mockRequest = put("/tasks/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.description").value(requestDto.description()))
                .andExpect(jsonPath("$.userId").value(requestDto.userId()))
                .andExpect(jsonPath("$.status").value(TaskStatus.UPDATING.name()));
    }

    @Test
    @DisplayName("PUT /tasks/{id} — при отсутствии задачи возвращает 404 и сообщение об ошибке")
    @Sql(scripts = "classpath:cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
    void updateTask_whenNotFound_thenReturns404AndErrorMessage() throws Exception {
        Integer id = 1;
        var requestDto = new TaskRequestDto("new title", "desc", 5, null);

        String requestBody = json.writeValueAsString(requestDto);
        var mockRequest = put("/tasks/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + id));
    }

    @Test
    @DisplayName("DELETE /tasks/{id} — при удалении возвращает 204")
    @Sql(scripts = "classpath:cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
    void deleteTask_whenExists_thenReturns204() throws Exception {
        Integer id = 1;
        var mockRequest = delete("/tasks/{id}", id);

        mockMvc.perform(mockRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /tasks — при наличии задач возвращает 200 и непустой список")
    @Sql(scripts = "classpath:fill_users_for_update_test.sql", executionPhase = BEFORE_TEST_METHOD)
    void getAllTasks_whenTasksExist_thenReturns200AndNonEmptyList() throws Exception {
        var mockRequest = get("/tasks");

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("title_1"))
                .andExpect(jsonPath("$[0].description").value("desc_1"))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("title_2"))
                .andExpect(jsonPath("$[1].description").value("desc_2"))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].status").value("UPDATING"));
    }

    @Test
    @DisplayName("GET /tasks — при отсутствии задач возвращает 200 и пустой список")
    @Sql(scripts = "classpath:cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
    void getAllTasks_whenNoTasks_thenReturns200AndEmptyList() throws Exception {
        var mockRequest = get("/tasks");

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
