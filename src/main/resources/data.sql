-- 初始化 users 表
INSERT INTO users (username, password, full_name, email,phone, enabled)
VALUES 
('admin', '$2a$10$bCMAT38mF7e1VWwngii7JOHXjDM2WTk76kHZqhusnne/s7/QzFj2K', '6969', 'admin@example.com','0929777888', true),
('staff', '$2a$10$bCMAT38mF7e1VWwngii7JOHXjDM2WTk76kHZqhusnne/s7/QzFj2K', '6969', 'user@example.com','0966555444', true),
('worker', '$2a$10$bCMAT38mF7e1VWwngii7JOHXjDM2WTk76kHZqhusnne/s7/QzFj2K', '6969', 'worker@example.com','0944555666', true);

-- 初始化 roles 表
INSERT INTO roles (id, name)
VALUES
(1, 'ADMIN'),
(2, 'STAFF'),
(3, 'WORKER');

-- 關聯 users 和 roles
INSERT INTO user_roles (user_id, role_id)
VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 2),
(3, 3);

-- 初始化 books 表
INSERT INTO books ( title, author, description , list_price, sale_price)
VALUES
('Spring Boot in Action', 'Craig Walls','good book', 39.99, 100),
('Hibernate Tips', 'Thorben Janssen','tools book', 29.99, 50),
('kk duelist POV learning', 'After and S1mon','jue jue jue', 5.26, 20);

-- 初始化 url_role_mapping 表
INSERT INTO url_role_mapping (url_pattern, roles) VALUES
('/api/books/.*', 'ADMIN,STAFF'),
('/api/users/.*', 'ADMIN'),
('/api/roles/.*', 'ADMIN'),
('/book/.*', 'ADMIN,STAFF'),
('/user/.*', 'ADMIN');
