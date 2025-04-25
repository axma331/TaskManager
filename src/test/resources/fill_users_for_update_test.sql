-- 1. Полная очистка таблицы и сброс sequence
TRUNCATE TABLE tasks RESTART IDENTITY CASCADE;

-- 2. Наполняем таблицу тестовыми задачами
INSERT INTO tasks (title, description, user_id, status)
VALUES ('title_1', 'desc_1', 1, 'NEW'),
       ('title_2', 'desc_1', 2, 'UPDATING');