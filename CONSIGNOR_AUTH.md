# 货主登录注册功能说明

## 功能概述

实现了货主（Consignor）的完整登录注册功能，包括：
- 货主注册
- 货主登录
- 货主登出
- 获取当前登录货主信息
- 基于 Redis 的 Token 管理和用户信息缓存

## 技术实现

### 1. Redis 缓存架构

#### RedisUtil (基础操作类)
位置：`src/main/java/com/sylphy/common/RedisUtil.java`

封装了 Redis 的基本操作：
- String 操作：`get()`, `set()`, `incr()`, `decr()`
- Hash 操作：`hget()`, `hset()`, `hmget()`, `hmset()`
- Set 操作：`sGet()`, `sSet()`
- List 操作：`lGet()`, `lSet()`
- 通用操作：`expire()`, `hasKey()`, `del()`

#### RedisCache (业务操作类)
位置：`src/main/java/com/sylphy/common/RedisCache.java`

封装了业务相关的 Redis 操作：
- Token 管理：`saveToken()`, `getConsignorIdByToken()`, `deleteToken()`, `refreshToken()`
- 货主信息缓存：`saveConsignorInfo()`, `getConsignorInfo()`, `deleteConsignorInfo()`, `refreshConsignorInfo()`

#### RedisConstants (常量类)
位置：`src/main/java/com/sylphy/common/RedisConstants.java`

定义了 Redis Key 的前缀和过期时间：
- `TOKEN_PREFIX`: Token 键前缀
- `CONSIGNOR_INFO_PREFIX`: 货主信息键前缀
- `TOKEN_EXPIRE_TIME`: Token 过期时间（7天）
- `USER_INFO_EXPIRE_TIME`: 用户信息缓存过期时间（30分钟）

### 2. Token 生成
位置：`src/main/java/com/sylphy/common/TokenUtil.java`

使用 Hutool 的 `IdUtil.simpleUUID()` 生成唯一的 Token 字符串。

### 3. 数据库设计

涉及两张表：
- `sys_user`: 用户表（存储用户类型：0-管理员，1-货主，2-调度员，3-司机）
- `sys_consignor`: 货主表（存储货主详细信息，通过 user_id 关联用户表）

## API 接口

### 1. 货主注册
```
POST /api/consignor/register
Content-Type: application/json

请求体:
{
  "email": "test@example.com",
  "phone": "13800138000",
  "password": "123456"
}

响应:
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

### 2. 货主登录
```
POST /api/consignor/login
Content-Type: application/json

请求体:
{
  "account": "13800138000",  // 手机号或邮箱
  "password": "123456"
}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "abc123...",
    "consignorId": 1,
    "userId": 1,
    "email": "test@example.com",
    "phone": "13800138000"
  }
}
```

### 3. 货主登出
```
POST /api/consignor/logout
Authorization: {token}

响应:
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

### 4. 获取当前登录货主信息
```
GET /api/consignor/info
Authorization: {token}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "consignorId": 1,
    "userId": 1,
    "email": "test@example.com",
    "phone": "13800138000",
    "password": null  // 密码字段已清空
  }
}
```

## 安全特性

1. **密码加密**：使用 MD5 加密存储密码
2. **Token 机制**：登录后生成唯一 Token，存储在 Redis 中
3. **Token 自动刷新**：每次访问接口时自动刷新 Token 和用户信息缓存的过期时间
4. **密码不返回**：获取用户信息时清空密码字段
5. **重复注册检测**：注册时检查手机号和邮箱是否已存在

## 缓存策略

1. **Token 缓存**：
   - Key: `token:{token}`
   - Value: `consignorId`
   - 过期时间：7天
   - 每次访问自动刷新

2. **用户信息缓存**：
   - Key: `consignor:info:{consignorId}`
   - Value: `Consignor对象`
   - 过期时间：30分钟
   - 每次访问自动刷新

3. **缓存穿透保护**：
   - 优先从 Redis 获取
   - Redis 不存在时从数据库查询
   - 查询后更新 Redis 缓存

## 项目结构

```
src/main/java/com/sylphy/
├── common/
│   ├── RedisUtil.java          # Redis 基础操作工具类
│   ├── RedisCache.java         # Redis 业务操作类
│   ├── RedisConstants.java     # Redis 常量
│   └── TokenUtil.java          # Token 工具类
├── controller/
│   └── ConsignorController.java  # 货主控制器
├── service/
│   ├── ConsignorService.java     # 货主服务接口
│   └── impl/
│       └── ConsignorServiceImpl.java  # 货主服务实现
├── mapper/
│   ├── ConsignorDao.java         # 货主 DAO
│   └── UserDao.java              # 用户 DAO
├── entity/model/
│   ├── Consignor.java            # 货主实体
│   └── User.java                 # 用户实体
├── dto/
│   ├── ConsignorLoginDTO.java    # 登录请求 DTO
│   └── ConsignorRegisterDTO.java # 注册请求 DTO
└── vo/
    └── ConsignorLoginVO.java     # 登录响应 VO

src/main/resources/mapper/
├── ConsignorDao.xml              # 货主 Mapper XML
└── UserDao.xml                   # 用户 Mapper XML
```

## 使用示例

### 前端调用示例（JavaScript）

```javascript
// 注册
async function register() {
  const response = await fetch('http://localhost:8080/api/consignor/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: 'test@example.com',
      phone: '13800138000',
      password: '123456'
    })
  });
  const data = await response.json();
  console.log(data);
}

// 登录
async function login() {
  const response = await fetch('http://localhost:8080/api/consignor/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      account: '13800138000',
      password: '123456'
    })
  });
  const data = await response.json();
  // 保存 token 到 localStorage
  localStorage.setItem('token', data.data.token);
  return data;
}

// 获取当前用户信息
async function getCurrentUser() {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8080/api/consignor/info', {
    method: 'GET',
    headers: {
      'Authorization': token
    }
  });
  const data = await response.json();
  return data;
}

// 登出
async function logout() {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8080/api/consignor/logout', {
    method: 'POST',
    headers: {
      'Authorization': token
    }
  });
  localStorage.removeItem('token');
  return await response.json();
}
```

## 数据库表结构

### sys_user 表
```sql
CREATE TABLE `sys_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键id',
  `type` BOOLEAN COMMENT '用户类型 0:管理员 1:货主 2:调度员 3:司机',
  PRIMARY KEY (`user_id`)
);
```

### sys_consignor 表
```sql
CREATE TABLE `sys_consignor` (
  `consignor_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '货主主键 id',
  `user_id` BIGINT COMMENT '关联的用户 id',
  `email` VARCHAR(255) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `password` VARCHAR(255) COMMENT '密码',
  PRIMARY KEY (`consignor_id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_user_id` (`user_id`)
);
```

## 注意事项

1. 登录时可以使用手机号或邮箱作为账号
2. Token 需要放在请求头的 `Authorization` 字段中
3. Token 有效期为 7 天，每次访问会自动刷新
4. 用户信息缓存 30 分钟，每次访问会自动刷新
5. 密码使用 MD5 加密，实际生产环境建议使用 BCrypt 等更安全的加密方式
