# ZJZ AI Agent - 智能AI助手系统

## 📖 项目简介

ZJZ AI Agent 是一个基于 Spring Boot 3.5 和 Spring AI Alibaba 构建的智能AI助手系统，集成了多种AI能力，包括旅行规划、工具调用、RAG检索增强、MCP协议支持等。系统采用前后端分离架构，提供丰富的AI交互功能。

### 核心特性

- 🤖 **多Agent架构**：支持 PlanApp（旅行规划）和 ZjzManus（通用任务执行）两种AI助手
- 🔧 **丰富工具集**：文件操作、PDF生成、邮件发送、网络搜索、网页抓取、终端操作等
- 📚 **RAG检索增强**：支持向量数据库（PGVector）和云知识库检索
- 💬 **流式输出**：支持 SSE、Server-Sent Events、SseEmitter 多种流式响应方式
- 🧠 **会话记忆**：支持文件系统、MySQL数据库等多种会话记忆存储方式
- 🌐 **MCP协议**：集成 Model Context Protocol，支持远程工具服务
- 🔒 **安全机制**：敏感词过滤、日志记录、重读校验等多重Advisor保护
- 🌍 **跨域支持**：全局CORS配置，完美支持前后端分离开发

---

## 🛠️ 技术栈

### 后端技术
- **框架**：Spring Boot 3.5.13
- **Java版本**：Java 21
- **AI框架**：Spring AI Alibaba 1.1.2.0 / Spring AI 1.1.2
- **大模型**：阿里云 DashScope (qwen-plus)
- **数据库**：
  - MySQL / MariaDB（会话记忆、业务数据）
  - PostgreSQL + PGVector（向量数据库）
- **ORM框架**：MyBatis Plus 3.5.12
- **API文档**：Knife4j 4.4.0 (OpenAPI 3)
- **工具库**：Hutool 5.8.40、Lombok 1.18.36
- **序列化**：Kryo 5.6.2
- **HTML解析**：jsoup 1.19.1
- **PDF生成**：iText Core 9.1.0
- **邮件发送**：javax.mail 1.6.2
- **文档处理**：Spring AI Markdown Document Reader
- **MCP客户端**：Spring AI MCP Client
- **响应式编程**：Spring WebFlux

### 前端技术
- **框架**：Vue.js 3
- **构建工具**：Vite
- **路由**：Vue Router
- **部署**：Nginx

### DevOps
- **容器化**：Docker
- **编排**：Docker Compose
- **MCP服务器**：独立部署（端口 8127）

---

## 📁 项目结构

