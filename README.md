# SchoolCompetitionWeb

一个基于 Vue 3 和 Spring Boot 的校园竞赛管理平台。

## 项目重构说明 (2026-04)

项目近期完成了全面的架构重构，主要改进包括：

### 后端 (Spring Boot)
- **架构分层**：引入了 Service 层和 DTO 层，实现了 Controller - Service - Repository 的标准三层架构，有效分离业务逻辑与数据访问。
- **响应规范化**：统一定义了 `Result<T>` 响应包装类，所有 API 接口遵循 `{ code, msg, data }` 标准格式。
- **全局异常处理**：通过 `@RestControllerAdvice` 实现了统一的异常拦截与业务错误提示。
- **安全性增强**：废弃了明文密码存储，引入了基于 SHA-256 的密码哈希加密逻辑。
- **事务管理**：对涉及多表操作（如竞赛及其报名信息的级联删除）增加了 `@Transactional` 事务支持。

### 前端 (Vue 3)
- **请求层优化**：重构了 `src/utils/request.js`，自动解析后端标准响应格式，简化了业务代码中的错误处理。
- **组件化重构**：将原本臃肿的 `AdminView.vue` 拆分为 `CompetitionList`、`UserManagement` 和 `AccountSettings` 等独立子组件，大幅提升了代码的可读性和可维护性。
- **状态管理**：优化了 Pinia Store 的用户信息存储逻辑。

## 技术栈

### 前端
- Vue 3 (Composition API)
- Vite
- Element Plus
- Pinia (PersistedState)
- Vue Router
- Axios
- Lucide Icons / Iconify

### 后端
- Spring Boot 4.0.1
- Spring Data JPA
- MySQL
- Lombok
- EasyExcel (Alibaba)
- Maven

## 开发与运行

### 后端
1. 确保已安装 JDK 17+ 和 MySQL。
2. 创建数据库 `school_competition_db`。
3. 修改 `backend/SchoolCompetition/src/main/resources/application.yml` 中的数据库配置。
4. 运行 `SchoolCompetitionApplication`。

### 前端
1. 进入项目根目录。
2. 执行 `npm install` 安装依赖。
3. 执行 `npm run dev` 启动开发服务器。

## 注意事项
由于引入了密码加密，重构前数据库中的明文密码将无法登录。请通过注册接口重新创建用户或手动更新数据库中的密码字段。
