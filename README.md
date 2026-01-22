# 🏆 学校学科竞赛报名管理系统 (School Competition Management System)

> 基于 **Spring Boot + Vue 3** 的前后端分离学科竞赛管理平台。实现了从竞赛发布、学生报名到名单管理的完整业务闭环。

## 📖 项目介绍

本项目旨在解决学校竞赛报名流程繁琐、数据统计困难的问题。系统采用 B/S 架构，分为**学生端**和**管理端**。通过严格的权限控制和业务逻辑校验，确保报名数据的准确性与安全性。

### 核心特性

- **👥 角色权限管理 (RBAC)**：区分管理员与学生角色，通过路由守卫拦截未授权访问。
- **🔒 安全报名机制**：
- **防代报**：学生报名时自动读取登录账号信息（姓名/学号），且无法修改，杜绝替考/代报现象。
- **防重报**：后端逻辑校验，同一学生无法重复报名同一场比赛。

- **📝 竞赛全生命周期管理**：管理员可发布、查看、删除竞赛，支持富文本描述和时间范围设置。
- **📊 名单实时管理**：管理员可实时查看报名名单，并具备“取消学生参赛资格”的权限。
- **🚀 单体打包部署**：前端构建产物嵌入后端资源目录，支持一键打包为 JAR 文件运行。

## 🛠️ 技术栈

### 后端 (Backend)

- **核心框架**: Spring Boot 3.x
- **ORM 框架**: Spring Data JPA (Hibernate)
- **数据库**: MySQL 8.0
- **工具库**: Lombok (简化代码), Maven (构建工具)

### 前端 (Frontend)

- **核心框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **路由管理**: Vue Router (Hash 模式)
- **网络请求**: Axios (封装了全局拦截器)
- **样式**: CSS3 (Flex/Grid 布局)

## 📂 数据库设计

系统包含三张核心表（自动建表）：

1. **Users (用户表)**: 存储账号、密码、角色(admin/student)、真实姓名。
2. **Competitions (竞赛表)**: 存储竞赛标题、描述、起止时间、状态。
3. **Registrations (报名表)**: 关联竞赛ID和学生信息，存储报名记录。

## ⚡️ 快速开始

### 环境准备

- JDK 17+
- Node.js 16+
- MySQL 8.0

### 1. 数据库配置

创建一个名为 `school_competition_db` 的数据库，并在 `application.yml` 中修改你的数据库账号密码。

```sql
CREATE DATABASE school_competition_db CHARACTER SET utf8mb4;

```

### 2. 初始化数据

项目启动会自动建表。建议首次运行后手动插入测试账号：

```sql
-- 管理员 (密码: 123456)
INSERT INTO users (username, password, role, name) VALUES ('admin', '123456', 'admin', '教务处老师');
-- 学生 (密码: 123456)
INSERT INTO users (username, password, role, name) VALUES ('stu1', '123456', 'student', '张三同学');

```

### 3. 启动开发环境

**后端 (Spring Boot):**

```bash
mvn spring-boot:run

```

后端服务运行在 `http://localhost:8080`

**前端 (Vue):**

```bash
cd school-competition-web
npm install
npm run dev

```

前端页面运行在 `http://localhost:5173`

## 📦 生产环境打包部署

本项目支持前后端合并打包，无需独立部署 Nginx。

1. **构建前端**:

```bash
cd school-competition-web
npm run build

```

生成的 `dist` 目录内容会自动复制到后端的 `src/main/resources/static` 目录下（需手动复制或配置脚本）。2. **打包后端**:

```bash
mvn clean package

```

3. **运行**:
   在 `target` 目录下找到生成的 JAR 包：

```bash
java -jar school-competition-0.0.1-SNAPSHOT.jar

```

访问 `http://localhost:8080` 即可使用完整系统。

## 🔗 API 接口说明

| 方法   | 路径                                    | 描述               |
| ------ | --------------------------------------- | ------------------ |
| POST   | `/api/auth/login`                       | 用户登录           |
| GET    | `/api/competitions`                     | 获取竞赛列表       |
| POST   | `/api/competitions`                     | 发布新竞赛         |
| DELETE | `/api/competitions/{id}`                | 删除竞赛           |
| POST   | `/api/registrations`                    | 学生报名           |
| GET    | `/api/registrations?competitionId={id}` | 获取某比赛报名名单 |
| DELETE | `/api/registrations/{id}`               | 取消报名资格       |

## 👨‍💻 作者

- **Developer**: [tfgkk]
- **Project**: 个人全栈练习项目 / 课程设计

---

**✨ 如果觉得这个项目不错，欢迎给个 Star!**
