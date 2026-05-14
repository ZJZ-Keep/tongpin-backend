
---

## 🔧 核心功能详解

### 1. 智能体（Agent）系统

项目实现了多种智能体模式：

- **BaseAgent**: 基础的流式对话智能体，支持 SSE 实时推送
- **ReActAgent**: 基于 ReAct（Reasoning + Acting）模式的推理智能体
- **ToolCallAgent**: 支持工具调用的智能体
- **ZjzManus**: 高级智能体，集成多种能力

### 2. RAG（检索增强生成）

基于 PostgreSQL + pgvector 实现的 RAG 系统：

- 文档加载和分块
- 向量嵌入和存储
- 上下文查询增强
- 自定义 RAG 顾问

### 3. 工具系统

内置多种实用工具：

| 工具 | 功能 |
|------|------|
| FileOperationTool | 文件读写、删除操作 |
| PDFGenerationTool | PDF 文件生成 |
| QQEmailSendTool | QQ 邮箱邮件发送 |
| WebSearchTool | 网络搜索 |
| WebScrapingTool | 网页爬取 |
| ResourceDownloadTool | 资源下载 |
| TerminalOperationTool | 终端命令执行 |

### 4. MCP 服务

支持 MCP（Model Context Protocol）协议：

- 工具注册和发现
- 图像搜索工具
- 支持 SSE 和 stdio 传输模式

---

## 🐳 Docker 部署

### 使用 Docker Compose（推荐）

```bash
# 一键启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 手动部署

```bash
# 1. 创建 Docker 网络
docker network create zjz-network

# 2. 启动数据库
docker run -d --name mariadb -p 3306:3306 -e MYSQL_ROOT_PASSWORD=your_password mariadb:latest
docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=your_password postgres:latest

# 3. 启动后端
docker build -t zjz-ai-agent-backend .
docker run -d --network zjz-network -p 8081:8081 --env-file .env zjz-ai-agent-backend

# 4. 启动前端
docker build -t zjz-ai-agent-frontend zjz-ai-agent-frontend/
docker run -d --network zjz-network -p 81:80 zjz-ai-agent-frontend
```

---

## 📊 API 接口

### 主要接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/ai/plan_app/chat/Sse` | GET | 行程规划流式对话 |
| `/api/ai/zjz_manus/chat` | POST | 通用智能体对话 |
| `/api/health` | GET | 健康检查 |
| `/api/logger` | GET/POST | 日志管理 |

### 调用示例

```bash
# 行程规划对话（SSE）
curl "http://localhost:8081/api/ai/plan_app/chat/Sse?message=帮我规划一次北京旅行&chatId=chat_123"

# 通用智能体对话
curl -X POST http://localhost:8081/api/ai/zjz_manus/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好", "chatId": "chat_123"}'
```

---

## 🧪 测试

```bash
# 后端测试
mvn test

# 前端测试
npm run test

# 集成测试
docker-compose -f docker-compose.test.yml up
```

---

## 📝 文档

- [API 文档](http://localhost:8081/swagger-ui.html) - Swagger UI
- [开发指南](docs/DEVELOPMENT.md) - 开发环境配置
- [部署指南](docs/DEPLOYMENT.md) - 生产环境部署
- [工具开发](docs/TOOLS.md) - 如何开发新工具

---

## 🤝 贡献

欢迎贡献代码、报告问题或提出建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 开源协议

本项目采用 MIT 协议开源 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 👨‍💻 作者

- **郑少** - 主要开发者
- 邮箱：2302181496@qq.com

---

## 🙏 致谢

感谢以下开源项目：

- [Spring AI](https://spring.io/projects/spring-ai)
- [Vue.js](https://vuejs.org/)
- [pgvector](https://github.com/pgvector/pgvector)
- [Alibaba DashScope](https://dashscope.aliyuncs.com/)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star 支持！**

Made with ❤️ by ZJZ AI Agent Team

</div>
