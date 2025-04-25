package ru.t1.ismailov.taskmanager.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import ru.t1.ismailov.taskmanager.dto.TaskRequestDto;
import ru.t1.ismailov.taskmanager.dto.TaskResponseDto;
import ru.t1.ismailov.taskmanager.model.TaskStatus;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TaskControllerITest {

    @LocalServerPort
    int port;

    RestTemplate rest = new RestTemplate();

    @Test
    @DisplayName("POST /tasks создаёт задачу, а GET /tasks/{id} её возвращает (сквозной happy-path)")
    void createAndFetch() {

        var req = new TaskRequestDto("title_IT", "desc_IT", 22, null);
        ResponseEntity<TaskResponseDto> createResp = rest.postForEntity(url("/tasks"), req, TaskResponseDto.class);

        Assertions.assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer id = createResp.getBody().id();


        TaskResponseDto fetched = rest.getForObject(url("/tasks/{id}"), TaskResponseDto.class, id);

        Assertions.assertThat(fetched)
                .usingRecursiveComparison()
                .ignoringFields("status")
                .isEqualTo(createResp.getBody());

        Assertions.assertThat(fetched.status()).isEqualTo(TaskStatus.NEW);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
