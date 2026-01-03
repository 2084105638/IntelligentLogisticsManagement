# Intelligent Logistics Management System

智能物流管理系统 - 基于 Spring Boot 3 + MyBatis Plus + MySQL + Redis

## 技术栈

- **Java**: 21
- **Spring Boot**: 3.2.1
- **MyBatis Plus**: 3.5.5
- **MySQL**: 8.0.33
- **Redis**: Spring Data Redis
- **Lombok**: 1.18.30
- **Hutool**: 5.8.24

## 项目结构

```
src/main/java/com/sylphy/
├── LogisticsApplication.java    # Spring Boot 主启动类
├── common/                      # 通用类
│   ├── Result.java             # 统一响应结果
│   └── PageResult.java         # 分页结果
├── config/                      # 配置类
│   ├── MyBatisPlusConfig.java  # MyBatis Plus 配置
│   ├── MyMetaObjectHandler.java # 字段自动填充
│   ├── RedisConfig.java        # Redis 配置
│   └── CorsConfig.java         # 跨域配置
├── controller/                  # 控制层
│   └── HealthController.java   # 健康检查
├── service/                     # 服务层
├── mapper/                      # 数据访问层
├── entity/                      # 实体类
│   └── BaseEntity.java         # 基础实体类
├── dto/                        # 数据传输对象
├── vo/                         # 视图对象
└── exception/                   # 异常处理
    ├── BusinessException.java  # 业务异常
    └── GlobalExceptionHandler.java # 全局异常处理器
```

## 配置说明

### 数据库配置

在 `application.yml` 中修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/logistics?...
    username: root
    password: your_password
```

### Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
```

## 数据库初始化

请先创建数据库：

```sql
CREATE DATABASE logistics CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

## 运行项目

1. 确保 MySQL 和 Redis 已启动
2. 修改 `application.yml` 中的数据库和 Redis 配置
3. 运行主类 `LogisticsApplication`
4. 访问 http://localhost:8080/api/health 检查服务状态

## 主要功能特性

- ✅ MyBatis Plus 分页插件
- ✅ MyBatis Plus 乐观锁
- ✅ MyBatis Plus 防止全表更新删除
- ✅ 字段自动填充（创建时间、更新时间、逻辑删除）
- ✅ Redis 序列化配置
- ✅ 统一响应结果封装
- ✅ 全局异常处理
- ✅ 跨域配置
- ✅ 参数校验

## 开发规范

1. 实体类继承 `BaseEntity` 获取基础字段
2. 使用 `Result<T>` 包装响应结果
3. 使用 `BusinessException` 抛出业务异常
4. 使用 `@Validated` 进行参数校验

## 作者

- **Author**: apple
- **Date**: 2026/1/3
