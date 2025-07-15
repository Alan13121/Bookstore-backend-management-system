-- 初始化 users 表
INSERT INTO users (username, password, full_name, email, enabled)
VALUES 
('admin', '$2a$10$bCMAT38mF7e1VWwngii7JOHXjDM2WTk76kHZqhusnne/s7/QzFj2K', '6969', 'admin@example.com', 1),
('user', '$2a$10$bCMAT38mF7e1VWwngii7JOHXjDM2WTk76kHZqhusnne/s7/QzFj2K', '6969', 'user@example.com', 1);

-- 初始化 roles 表
INSERT INTO roles (id, name)
VALUES
(1, 'ADMIN'),
(2, 'USER');

-- 關聯 users 和 roles（假設是 user_roles 表）
INSERT INTO user_roles (user_id, role_id)
VALUES
(1, 1),
(1, 2),
(2, 2);

-- 初始化 books 表
INSERT INTO books (id, title, author, price, stock)
VALUES
(1, 'Spring Boot in Action', 'Craig Walls', 39.99, 100),
(2, 'Hibernate Tips', 'Thorben Janssen', 29.99, 50);
