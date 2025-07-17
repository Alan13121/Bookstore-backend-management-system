INSERT INTO roles (id, name) VALUES (1, 'ADMIN');
INSERT INTO users (id, username, password, enabled, full_name, email, phone) 
VALUES (1, 'admin', '$2a$12$utRQSqVDCU1HFpNirGPDYOHjZTImQJ8Es9e5hKgvUFWtDY.LPtPau', true, 'Admin User', 'admin@example.com', '1234567890');

INSERT INTO user_roles (user_id, roles_id) VALUES (1, 1);
