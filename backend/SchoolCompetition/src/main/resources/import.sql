-- 插入一个管理员账号 (账号: admin, 密码: 123456 的 SHA-256 加密版)
INSERT INTO users (username, password, role, name) VALUES ('admin', 'jZkw3uS9LRU9F8U+XW0+7W+N1/FjV1jD7S/X+6Yy7rM=', 'admin', '教务处老师');

-- 插入一个学生账号 (账号: stu1, 密码: 123456 的 SHA-256 加密版)
INSERT INTO users (username, password, role, name) VALUES ('stu1', 'jZkw3uS9LRU9F8U+XW0+7W+N1/FjV1jD7S/X+6Yy7rM=', 'student', '张三同学');
