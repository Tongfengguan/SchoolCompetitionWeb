-- 插入一个管理员账号 (账号: admin, 密码: 123456)
INSERT INTO users (username, password, role, name) VALUES ('admin', '123456', 'admin', '教务处老师');

-- 插入一个学生账号 (账号: stu1, 密码: 123456)
INSERT INTO users (username, password, role, name) VALUES ('stu1', '123456', 'student', '张三同学');