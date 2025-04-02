# 📌 Task Manager - CRUD приложение с AOP-логированием и обработкой исключений

## 🚀 Возможности

- **Полноценное управление задачами** через REST API (CRUD операции)
- **Сквозное логирование** HTTP-запросов, времени выполнения методов и ошибок
- **Автоматическая обработка исключений** с конвертацией в структурированные JSON-ответы

## 📖 Описание

RESTful сервис для управления задачами с расширенной системой мониторинга.  
Базовая сущность:

```java

@Data
@Accessors(chain = true)
public class Task {
    private Long id;
    private String title;
    private String description;
    private Long userId;
}
```

## 🛠️ Технологический стек

- **Java 17** + **Spring Boot 3**
- **Spring AOP** для аспектно-ориентированного программирования
- **Lombok** для сокращения boilerplate-кода
- **SLF4J** с реализацией Logback для логирования

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

| Аспект                        | Аннотации                               | Функционал                                |
|-------------------------------|-----------------------------------------|-------------------------------------------|
| `LoggingAspect`               | `@Before`, `@AfterReturning`, `@Around` | Логирование запросов, времени выполнения  |
| `TaskExceptionHandlingAspect` | `@Around`, `@AfterThrowing`             | Обработка исключений и логирование ошибок |

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
- **Единая точка обработки ошибок:** аспект преобразует исключения в HTTP-ответы

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