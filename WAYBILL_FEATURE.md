# 货主运单功能说明

## 功能概述

实现了货主运单的完整管理功能，包括：
- 发布运单：在线填写货物信息、起止地址、期望时效等，创建运输订单
- 历史运单查询：查看所有历史订单的详细信息，支持分页和条件筛选
- 运单详情查询：查看单个运单的详细信息
- 取消运单：取消待分配状态的运单

## 技术实现

### 1. MyBatis Plus 集成

使用 MyBatis Plus 作为 ORM 框架，简化 CRUD 操作：
- `WaybillMapper` 继承 `BaseMapper<WaybillEntity>`，自动获得基础 CRUD 方法
- `WaybillService` 继承 `IService<WaybillEntity>`，提供丰富的业务方法
- `WaybillServiceImpl` 继承 `ServiceImpl<WaybillMapper, WaybillEntity>`

### 2. 运单状态管理

#### WaybillStatus 枚举
位置：`src/main/java/com/sylphy/common/WaybillStatus.java`

定义了运单的四种状态：
- `PENDING_ALLOCATION(0)`: 待分配
- `ALLOCATED(1)`: 已分配
- `IN_TRANSIT(2)`: 运输中
- `COMPLETED(3)`: 已完成

### 3. 实体设计

#### WaybillEntity
位置：`src/main/java/com/sylphy/entity/model/WaybillEntity.java`

使用 MyBatis Plus 注解：
- `@TableName("sys_waybill")`: 指定表名
- `@TableId(type = IdType.AUTO)`: 主键自增
- `@TableField`: 字段映射

主要字段：
- `waybillId`: 运单主键
- `consignorId`: 货主ID
- `goodsInformation`: 货物信息
- `startAddress`: 起始地址
- `endAddress`: 结束地址
- `expectedTimeLimit`: 期望时效
- `cost`: 费用
- `status`: 运单状态
- `createTime`: 创建时间

## API 接口

### 1. 发布运单

```
POST /api/waybill/create
Authorization: {token}
Content-Type: application/json

请求体:
{
  "goodsInformation": "电子产品，易碎",
  "startAddress": "北京市朝阳区xxx",
  "endAddress": "上海市浦东新区xxx",
  "expectedTimeLimit": "2026-01-10 18:00:00",
  "cost": 500.00
}

响应:
{
  "code": 200,
  "message": "运单创建成功",
  "data": 1  // 运单ID
}
```

**校验规则：**
- 所有字段不能为空
- 费用必须大于0
- 期望时效不能早于当前时间

### 2. 查询运单详情

```
GET /api/waybill/{waybillId}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "waybillId": 1,
    "consignorId": 1,
    "goodsInformation": "电子产品，易碎",
    "startAddress": "北京市朝阳区xxx",
    "endAddress": "上海市浦东新区xxx",
    "createTime": "2026-01-04 10:30:00",
    "expectedTimeLimit": "2026-01-10 18:00:00",
    "cost": 500.00,
    "status": 0,
    "statusDesc": "待分配"
  }
}
```

### 3. 分页查询历史运单

```
POST /api/waybill/list
Authorization: {token}
Content-Type: application/json

请求体:
{
  "status": 0,              // 可选，运单状态筛选
  "startAddress": "北京",   // 可选，起始地址模糊查询
  "endAddress": "上海",     // 可选，结束地址模糊查询
  "current": 1,             // 页码，默认1
  "size": 10                // 每页大小，默认10
}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,           // 总记录数
    "current": 1,          // 当前页码
    "size": 10,            // 每页大小
    "records": [           // 运单列表
      {
        "waybillId": 1,
        "consignorId": 1,
        "goodsInformation": "电子产品，易碎",
        "startAddress": "北京市朝阳区xxx",
        "endAddress": "上海市浦东新区xxx",
        "createTime": "2026-01-04 10:30:00",
        "expectedTimeLimit": "2026-01-10 18:00:00",
        "cost": 500.00,
        "status": 0,
        "statusDesc": "待分配"
      }
      // ... 更多运单
    ]
  }
}
```

**查询条件说明：**
- 自动关联当前登录货主的运单
- 支持按状态精确筛选
- 支持起始地址和结束地址模糊查询
- 按创建时间倒序排列

### 4. 取消运单

```
DELETE /api/waybill/{waybillId}
Authorization: {token}

响应:
{
  "code": 200,
  "message": "运单取消成功",
  "data": null
}
```

**取消规则：**
- 只有运单所属货主可以取消
- 只有"待分配"状态的运单可以取消
- 其他状态的运单不能取消

## 业务规则

### 1. 权限控制
- 所有运单操作需要 Token 认证
- 货主只能查看和操作自己的运单
- 通过 Token 自动获取货主ID，无需前端传递

### 2. 状态流转
```
待分配(0) → 已分配(1) → 运输中(2) → 已完成(3)
    ↓
  可取消
```

### 3. 数据校验
- 期望时效必须晚于当前时间
- 费用必须大于0
- 所有必填字段不能为空
- 手机号格式验证

## 使用示例

### 前端调用示例（JavaScript）

```javascript
// 获取 Token
const token = localStorage.getItem('token');

// 1. 发布运单
async function createWaybill() {
  const response = await fetch('http://localhost:8080/api/waybill/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token
    },
    body: JSON.stringify({
      goodsInformation: '电子产品，易碎',
      startAddress: '北京市朝阳区xxx',
      endAddress: '上海市浦东新区xxx',
      expectedTimeLimit: '2026-01-10 18:00:00',
      cost: 500.00
    })
  });
  const data = await response.json();
  console.log('运单ID:', data.data);
  return data;
}

// 2. 查询历史运单
async function queryWaybills(status, page = 1, size = 10) {
  const response = await fetch('http://localhost:8080/api/waybill/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token
    },
    body: JSON.stringify({
      status: status,
      current: page,
      size: size
    })
  });
  const data = await response.json();
  return data.data; // 返回分页结果
}

// 3. 查询运单详情
async function getWaybillDetail(waybillId) {
  const response = await fetch(`http://localhost:8080/api/waybill/${waybillId}`, {
    method: 'GET',
    headers: {
      'Authorization': token
    }
  });
  const data = await response.json();
  return data.data;
}

// 4. 取消运单
async function cancelWaybill(waybillId) {
  const response = await fetch(`http://localhost:8080/api/waybill/${waybillId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': token
    }
  });
  const data = await response.json();
  return data;
}

// 使用示例
(async () => {
  // 发布运单
  const createResult = await createWaybill();
  console.log('创建结果:', createResult);

  // 查询所有待分配的运单
  const pendingWaybills = await queryWaybills(0);
  console.log('待分配运单:', pendingWaybills);

  // 查询运单详情
  const detail = await getWaybillDetail(1);
  console.log('运单详情:', detail);

  // 取消运单
  const cancelResult = await cancelWaybill(1);
  console.log('取消结果:', cancelResult);
})();
```

## 数据库表结构

### sys_waybill 表

```sql
CREATE TABLE `sys_waybill` (
  `waybill_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '运单主键 id',
  `consignor_id` BIGINT NOT NULL COMMENT '货主 ID',
  `goods_information` VARCHAR(500) COMMENT '货物信息',
  `start_address` VARCHAR(255) NOT NULL COMMENT '起始地址',
  `end_address` VARCHAR(255) NOT NULL COMMENT '结束地址',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expected_time_limit` DATETIME NOT NULL COMMENT '期望时效',
  `cost` DECIMAL(10,2) NOT NULL COMMENT '费用',
  `status` INT DEFAULT 0 COMMENT '状态 待分配:0 已分配:1 运输中:2 已完成:3',
  PRIMARY KEY (`waybill_id`),
  KEY `idx_consignor_id` (`consignor_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运单表';
```

**索引说明：**
- `idx_consignor_id`: 加速按货主查询
- `idx_status`: 加速按状态筛选
- `idx_create_time`: 加速按时间排序

## 项目结构

```
src/main/java/com/sylphy/
├── common/
│   └── WaybillStatus.java          # 运单状态枚举
├── controller/
│   └── WaybillController.java      # 运单控制器
├── service/
│   ├── WaybillService.java         # 运单服务接口
│   └── impl/
│       └── WaybillServiceImpl.java # 运单服务实现
├── mapper/
│   └── WaybillMapper.java          # 运单 Mapper (MyBatis Plus)
├── entity/model/
│   └── WaybillEntity.java          # 运单实体
├── dto/
│   ├── WaybillCreateDTO.java       # 创建运单请求 DTO
│   └── WaybillQueryDTO.java        # 查询运单请求 DTO
└── vo/
    └── WaybillVO.java              # 运单响应 VO
```

## MyBatis Plus 特性

### 1. 自动 CRUD
```java
// 保存
waybillService.save(waybill);

// 根据ID查询
waybillService.getById(waybillId);

// 删除
waybillService.removeById(waybillId);

// 分页查询
Page<WaybillEntity> page = new Page<>(current, size);
waybillService.page(page, queryWrapper);
```

### 2. LambdaQueryWrapper
```java
LambdaQueryWrapper<WaybillEntity> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper
    .eq(WaybillEntity::getConsignorId, consignorId)
    .eq(queryDTO.getStatus() != null, WaybillEntity::getStatus, queryDTO.getStatus())
    .like(StringUtils.hasText(queryDTO.getStartAddress()),
          WaybillEntity::getStartAddress, queryDTO.getStartAddress())
    .orderByDesc(WaybillEntity::getCreateTime);
```

### 3. 条件构造器
- `eq`: 等于
- `like`: 模糊查询
- `orderByDesc`: 降序排列
- 第一个参数为条件判断，true 时才应用该条件

## 注意事项

1. **Token 认证**：所有接口都需要在请求头中携带 Token
2. **权限隔离**：货主只能操作自己的运单
3. **状态限制**：只有待分配状态的运单可以取消
4. **时间校验**：期望时效必须晚于当前时间
5. **分页查询**：默认按创建时间倒序排列
6. **模糊搜索**：地址字段支持模糊查询
7. **数据完整性**：所有必填字段都有校验

## 扩展功能建议

1. **运单追踪**：实时查看运单物流状态
2. **费用计算**：根据距离和货物重量自动计算费用
3. **评价功能**：运单完成后可以对司机进行评价
4. **消息推送**：运单状态变更时推送消息给货主
5. **统计报表**：运单数量、费用统计等
6. **批量导出**：支持批量导出运单数据
