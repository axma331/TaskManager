# 📌 Task Manager - CRUD приложение с AOP-логированием и обработкой исключений

## 🚀 Возможности

- **Полноценное управление задачами** через REST API (CRUD операции)
- **Сквозное логирование** HTTP-запросов, времени выполнения методов и ошибок
- **Автоматическая обработка исключений** с конвертацией в структурированные JSON-ответы
- **Интеграция с Kafka** для отслеживания изменений статусов задач
- **Отправка email-уведомлений** при изменении статуса задачи

## 📖 Описание

## 📖 Описание
RESTful сервис для управления задачами с расширенной системой мониторинга и интеграцией с Kafka для отправки событий об изменениях статусов задач и последующей отправкой email-уведомлений об изменении статуса задачи.

Базовая сущность:

```java

import ru.t1.ismailov.taskmanager.model.TaskStatus;

@Data
@Accessors(chain = true)
public class Task {
   private Long id;
   private String title;
   private String description;
   private Long userId;
   private TaskStatus status;
}
```

## 🛠️ Технологический стек

- **Java 17** + **Spring Boot 3**
- **Spring AOP** для аспектно-ориентированного программирования
- **Lombok** для сокращения boilerplate-кода
- **SLF4J** с реализацией Logback для логирования
- **Spring Kafka** для работы с Kafka
- **Spring Mail** для отправки email-уведомлений

## 🌐 REST API Endpoints

| Метод  | Эндпоинт      | Описание              | Пример тела запроса        |
|--------|---------------|-----------------------|----------------------------|
| POST   | `/tasks`      | Создать задачу        | `{"title":"Fix bug", ...}` |
| GET    | `/tasks/{id}` | Получить задачу по ID | -                          |
| PUT    | `/tasks/{id}` | Обновить задачу       | `{"title":"Updated", ...}` |
| DELETE | `/tasks/{id}` | Удалить задачу        | -                          |
| GET    | `/tasks`      | Получить все задачи   | -                          |

**Пример ответа при ошибке:**

```json
{
  "error": "Not Found",
  "message": "Task not found with id: 999",
  "timestamp": "2023-12-20T15:30:45.123"
}
```

## 🔍 Система логирования (AOP)

### Реализованные аспекты

| Аспект                        | Аннотации                     | Функционал                                |
|-------------------------------|-------------------------------|-------------------------------------------|
| `LoggingTaskAspect`           | `@Before`, `@AfterReturning`, `@Around` | Логирование запросов, времени выполнения  |
| `TaskExceptionHandlingAspect` | `@AfterThrowing`             | Обработка исключений и логирование ошибок |
| `LoggingKafkaAspect`          | `@AfterReturning`, `@Around` | Логирование cобытий Kafka и NotificationService|

### Примеры логов

```log
# HTTP-запрос
INFO  [http-nio-8080-exec-1] LoggingAspect: Processing HTTP POST request in TaskController.createTask with args: [Task(...)]

# Успешное выполнение
INFO  [http-nio-8080-exec-1] LoggingAspect: Method TaskService.createTask executed successfully with result: Task(id=1, ...)

# Замер времени
INFO  [http-nio-8080-exec-2] LoggingAspect: TaskService.createTask executed after 24 ms

# Ошибка
ERROR [http-nio-8080-exec-3] TaskExceptionHandlingAspect: Task not found: Task not found with id: 999
```

## 🚦 Запуск приложения

1. Клонировать репозиторий:
```bash
  git clone https://github.com/axma331/TaskManager
```

2. Собрать проект:
```bash
  mvn clean package
```

3. Запустить:
```bash
  java -jar target/TaskManager-1.0.0.jar
```

## 📌 Особенности реализации

- **Кастомные аннотации:** `@MeasureExecutionTime`, `@LoggingRequest`, `@TaskExceptionHandler`
- **Fluent API:** цепочки сеттеров (`task.setTitle(...).setDescription(...)`)
- **Единая точка обработки исключений:** аспект преобразует исключения в HTTP-ответы
- **Интеграция с Kafka:** отслеживание изменения статусов задач с логированием метаданных
- **Отправка email-уведомлений:** при изменении статуса задачи отправляется email уведомление.

## 📡 Примеры HTTP-запросов

```http
POST http://localhost:8080/tasks
Content-Type: application/json

{
  "title": "Spring AOP",
  "description": "Реализовать использование аоп в приложении.",
  "userId": 1
}
```

```http
GET http://localhost:8080/tasks/1
```

```http
PUT http://localhost:8080/tasks/1
Content-Type: application/json

{
  "title": "Spring AOP",
  "description": "Реализовать использование аоп в приложении и добавить аспекты, советы, точки среза.",
  "userId": 1
}
```

```http
DELETE http://localhost:8080/tasks/1
```

```http
GET http://localhost:8080/tasks
```

```http
GET http://localhost:8080/tasks/999
```


## 🔧 Изменения в функционале

<details>
  <summary>📡 Интеграция с Kafka</summary>

- Настроены producer и consumer Kafka для отправки и получения событий изменения статусов задач.
- Создан топик `tasks-status-updates-topic` для отправки сообщений.
- Добавлены логи для успешной отправки сообщений в Kafka и обработки событий.
- Реализован класс `TaskStatusEventPublisher` для отправки событий изменения статуса задачи в Kafka.
- Обработчик `TaskStatusKafkaListener` для получения событий изменения статуса и отправки email-уведомлений.

</details>

<details>
  <summary>✉️ Email уведомления</summary>

- При изменении статуса задачи отправляется email уведомление.
- Используется `JavaMailSender` для отправки email сообщений.

</details>

<details>
  <summary>🚨 Обработка ошибок</summary>

- Все ошибки при отправке email сообщений или обработке событий Kafka логируются и выводятся в системный журнал.

</details>
