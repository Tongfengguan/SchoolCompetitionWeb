# SchoolCompetitionWeb

一个基于 Vue 3 和 Spring Boot 的现代化校园竞赛管理平台。

## 项目进阶重构说明 (2026-04)

项目已完成从“基础功能实现”向“生产级架构”的全面进化，核心增强包括：

### 1. 安全与鉴权体系 (Security & Auth)
- **Spring Security + JWT**：舍弃了传统的 Session 模式，集成了完整的 Spring Security 框架，实现了基于 JWT (JSON Web Token) 的无状态鉴权。
- **数据校验 (Validation)**：全量接入 `spring-boot-starter-validation` (JSR-303)，在 DTO 层通过注解（@NotBlank, @Pattern 等）确保入参合法性，并由全局异常处理器统一返回友好错误提示。
- **安全哈希**：密码采用 SHA-256 加密存储，配合 UTF-8 编码一致性处理，杜绝明文风险。

### 2. 性能与大数据量支持 (Performance)
- **全链路分页 (Pagination)**：后端基于 Spring Data JPA 的 `Pageable` 接口实现了数据库物理分页；前端适配了 Element Plus 分页组件，支持竞赛列表和用户列表的高效加载。
- **异步文件处理**：支持 Excel 批量导入学生账号（EasyExcel），自动跳过重复账号并静默生成初始密码。

### 3. 数据可视化与文档 (Visualization & Docs)
- **ECharts 数据看板**：管理员首页新增可视化控制台，通过饼图、柱状图直观展示竞赛报名分布、用户角色占比及系统核心指标。
- **Knife4j API 文档**：集成 Swagger/OpenAPI 3.0，通过 Knife4j 增强界面提供精美的在线 API 调试手册（访问地址：`/doc.html`）。

### 4. 业务功能深度扩展 (Features)
- **附件系统**：支持管理员为竞赛上传 PDF 规则文件，支持学生在线预览与下载。实现基于本地存储的 UUID 文件重命名逻辑。
- **前端交互重塑**：
    - **极简登录页**：采用现代分栏布局与沉稳配色，提升品牌专业感。
    - **竞赛详情窗**：学生端支持点击查看完整比赛描述、时间线及附件，报名前信息获取更充分。

## 技术栈

### 前端
- Vue 3 (Composition API) & Vite
- Element Plus & ECharts
- Pinia (PersistedState) & Vue Router
- Axios & Lucide Icons

### 后端
- Spring Boot 3.x (Jakarta EE)
- Spring Security & JJWT
- Spring Data JPA & MySQL
- Knife4j (Swagger) & EasyExcel
- Maven Wrapper

## 快速开始

### 运行环境
- JDK 17+
- MySQL 8.0+
- Node.js 20+

### 后端启动
1. 创建数据库：`CREATE DATABASE school_competition_db;`
2. 配置 `application.yml` 中的数据库账号密码。
3. 运行根目录下的 `./mvnw.cmd spring-boot:run` (或通过 IDE 运行)。
4. 访问接口文档：`http://localhost:8080/doc.html`

### 前端启动
1. `npm install`
2. `npm run dev`
3. 访问地址：`http://localhost:5173`

## 开发者提示
- **默认管理员**：`admin` / `123456`
- **默认学生**：`stu1` / `123456`
- **权限说明**：除登录注册及静态资源外，所有接口均需在 Header 中携带 `Authorization: Bearer <token>`。
