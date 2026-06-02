
基于 Spring Boot 的同频匹配后端服务，用于实现用户匹配和团队管理功能。

## 技术栈

- **语言**: Java 21
- **框架**: Spring Boot 3.2.x
- **数据库**: MySQL 8.0+ + Redis 7.0+
- **ORM**: MyBatis Plus
- **分布式锁**: Redisson
- **定时任务**: Spring Scheduler
- **构建工具**: Maven

## 功能特性

### 用户模块
- 用户注册与登录
- 用户信息管理
- 密码加密存储

### 团队模块
- 团队创建与管理
- 团队成员管理
- 团队搜索与筛选
- 加入/退出团队

### 匹配算法
- 基于标签相似度的用户匹配
- 余弦相似度计算

### 缓存优化
- Redis 热点数据缓存
- 定时任务预热缓存

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+

### 配置说明

1. 创建数据库
```sql
CREATE DATABASE tongpin_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本
```bash
mysql -u username -p tongpin_db < sql/create_table.sql
```

3. 修改配置文件 `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tongpin_db
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 运行项目

**开发环境:**
```bash
mvn spring-boot:run
```

**生产环境:**
```bash
mvn clean package
java -jar target/tongpin-backend-1.0.0.jar
```

## 项目结构

```
├── src/main/java/com/zjz/tongpin/
│   ├── common/          # 通用工具类
│   ├── config/          # 配置类
│   ├── controller/      # 控制层
│   ├── service/         # 服务层
│   ├── mapper/          # 数据访问层
│   ├── model/           # 数据模型
│   ├── exception/       # 异常处理
│   ├── job/             # 定时任务
│   └── utils/           # 工具函数
├── src/main/resources/
│   ├── mapper/          # MyBatis XML映射
│   └── application.yml  # 配置文件
├── sql/                 # 数据库脚本
└── Dockerfile           # Docker配置
```

## API 接口

### 用户接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/user/register` | POST | 用户注册 |
| `/api/user/login` | POST | 用户登录 |
| `/api/user/logout` | POST | 用户登出 |
| `/api/user/current` | GET | 获取当前用户 |
| `/api/user/search` | GET | 搜索用户 |

### 团队接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/team/list` | GET | 获取团队列表 |
| `/api/team/create` | POST | 创建团队 |
| `/api/team/update` | POST | 更新团队 |
| `/api/team/delete` | POST | 删除团队 |
| `/api/team/join` | POST | 加入团队 |
| `/api/team/quit` | POST | 退出团队 |

## 部署方式

### Docker 部署

```bash
docker build -t tongpin-backend .
docker run -p 8080:8080 tongpin-backend
```

### Docker Compose

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: tongpin_db
    ports:
      - "3306:3306"
  redis:
    image: redis:7.0
    ports:
      - "6379:6379"
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
```

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
