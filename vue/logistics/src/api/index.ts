import request from '../utils/request';

// 通用响应类型
export interface Result<T = any> {
  code: number;
  msg: string;
  data: T;
}

// 分页结果
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

// ============ 货主相关API ============
export interface ConsignorRegisterDTO {
  email: string;
  phone: string;
  password: string;
}

export interface ConsignorLoginDTO {
  // 与后端 ConsignorLoginDTO.account 对应，支持手机号或邮箱
  account: string;
  password: string;
}

export interface ConsignorLoginVO {
  token: string;
  consignorId: number;
  phone: string;
  email: string;
}

// 货主注册
export const consignorRegister = (data: ConsignorRegisterDTO) => {
  return request.post<Result>('/consignor/register', data);
};

// 货主登录
export const consignorLogin = (data: ConsignorLoginDTO) => {
  return request.post<Result<ConsignorLoginVO>>('/consignor/login', data);
};

// 货主登出
export const consignorLogout = () => {
  return request.post<Result>('/consignor/logout');
};

// 获取当前货主信息
export const getConsignorInfo = () => {
  return request.get<Result>('/consignor/info');
};

// 更新货主信息（对应后端 ConsignorChangeInfoDTO）
export interface ConsignorChangeInfoDTO {
  email?: string;
  oldPassword?: string;
  newPassword?: string;
}

// 修改货主个人信息（/consignor/changeInfo）
export const changeConsignorInfo = (data: ConsignorChangeInfoDTO) => {
  return request.post<Result>('/consignor/changeInfo', data);
};

// ============ 运单相关API ============
export interface WaybillCreateDTO {
  receivingConsignorId: number;
  goodsInformation: string;
  startAddress: string;
  endAddress: string;
  expectedTimeLimit: string; // 格式: yyyy-MM-dd HH:mm:ss
  cost: number;
}

export interface WaybillQueryDTO {
  status?: number;
  startTime?: string;
  endTime?: string;
  current?: number;
  size?: number;
  keyword?: string;
}

export interface WaybillVO {
  waybillId: string; // 使用字符串避免 Long 精度丢失（用于API调用）
  waybillIdentification: string; // 运单业务标识（用于前端显示，唯一标识）
  consignorId: number;
  goodsInformation: string;
  startAddress: string;
  endAddress: string;
  createTime: string;
  expectedTimeLimit: string;
  cost: number;
  status: number;
  statusDesc: string;
  changed?: number; // 0 表示当前有效运单，1 表示已被替换的旧运单
}

// 创建运单
export const createWaybill = (data: WaybillCreateDTO) => {
  return request.post<Result<string>>('/waybill/create', data);
};

// 获取运单详情
export const getWaybillDetail = (waybillId: string) => {
  return request.get<Result<WaybillVO>>(`/waybill/${waybillId}`);
};

// 查询运单列表
export const queryWaybills = (data: WaybillQueryDTO) => {
  return request.post<Result<PageResult<WaybillVO>>>('/waybill/list', data);
};

// 根据状态查询运单
export const queryWaybillsByStatus = (status?: number, current = 1, size = 10) => {
  return request.get<Result<PageResult<WaybillVO>>>('/waybill/listByStatus', {
    params: { status, current, size },
  });
};

// 根据时间查询运单
export const queryWaybillsByTime = (startTime?: string, endTime?: string, current = 1, size = 10) => {
  return request.get<Result<PageResult<WaybillVO>>>('/waybill/listByTime', {
    params: { startTime, endTime, current, size },
  });
};

// 取消运单
export const cancelWaybill = (waybillId: string) => {
  return request.delete<Result>(`/waybill/${waybillId}`);
};

// ============ 调度员相关API ============
export interface DispatcherLoginDTO {
  username: string;
  password: string;
}

export interface DispatcherLoginVO {
  token: string;
  dispatcherId: number;
  username: string;
}

// 调度员登录
export const dispatcherLogin = (data: DispatcherLoginDTO) => {
  return request.post<Result<DispatcherLoginVO>>('/dispatcher/login', data);
};

// 调度员登出
export const dispatcherLogout = () => {
  return request.post<Result>('/dispatcher/logout');
};

// 司机登出（如果后端有提供）
export const driverLogout = () => {
  return request.post<Result>('/driver/logout');
};

// 获取当前调度员信息
export const getDispatcherInfo = () => {
  return request.get<Result>('/dispatcher/me');
};

// 调度员查询运单列表
export interface DispatcherWaybillQueryDTO {
  status?: number;
  startAddress?: string;
  endAddress?: string;
  keyword?: string;
  current?: number;
  size?: number;
}

export const dispatcherQueryWaybills = (params: DispatcherWaybillQueryDTO) => {
  return request.get<Result<PageResult<WaybillVO>>>('/dispatcher/waybills', { params });
};

// 获取运单详情（调度员视角）
export const dispatcherGetWaybill = (waybillId: string) => {
  return request.get<Result<WaybillVO>>(`/dispatcher/waybills/${waybillId}`);
};

// ============ 车辆相关API ============
export interface CarCreateDTO {
  driverId: number;
  // 车辆类型，对应后端枚举 CarType（0: 微型, 1: 轻型, 2: 中型, 3: 重型）
  type: number;
  location?: string;
  status?: number;
}

export interface CarUpdateDTO {
  carId: number;
  driverId?: number;
  // 车辆类型，可选；后端若暂不支持会自动忽略
  type?: number;
  location?: string;
  status?: number;
}

export interface CarQueryDTO {
  status?: number;
  driverId?: number;
  location?: string;
  current?: number;
  size?: number;
}

export interface CarVO {
  carId: number;
  driverId: number;
  driverName?: string;
  // 车辆类型编码，对应后端枚举 CarType（0: 微型, 1: 轻型, 2: 中型, 3: 重型）
  type?: number;
  // 兼容旧字段：如果后端返回了车牌、车型、载重，这里保留但不强依赖
  licensePlate?: string;
  carType?: string;
  loadCapacity?: number;
  location: string;
  status: number;
  statusDesc?: string;
}

// 创建车辆
export const createCar = (data: CarCreateDTO) => {
  return request.post<Result<number>>('/dispatcher/cars', data);
};

// 更新车辆
export const updateCar = (carId: number, data: CarUpdateDTO) => {
  return request.put<Result>(`/dispatcher/cars/${carId}`, data);
};

// 删除车辆
export const deleteCar = (carId: number) => {
  return request.delete<Result>(`/dispatcher/cars/${carId}`);
};

// 查询车辆列表
export const queryCars = (params: CarQueryDTO) => {
  return request.get<Result<PageResult<CarVO>>>('/dispatcher/cars', { params });
};

// 获取车辆状态
export const getCarStatus = (carId: number) => {
  return request.get<Result<number>>(`/dispatcher/cars/${carId}/status`);
};

// 更新车辆状态
export const updateCarStatus = (carId: number, status: number) => {
  return request.put<Result>(`/dispatcher/cars/${carId}/status`, null, {
    params: { status },
  });
};

// 获取车辆位置
export const getCarLocation = (carId: number) => {
  return request.get<Result<string>>(`/dispatcher/cars/${carId}/location`);
};

// 更新车辆位置
export const updateCarLocation = (carId: number, location: string) => {
  return request.put<Result>(`/dispatcher/cars/${carId}/location`, null, {
    params: { location },
  });
};

// ============ 司机查询（用于调度员选择司机） ============
export interface DriverOption {
  driverId: number;
  phone?: string;
  email?: string;
  name?: string;
}

// 按关键字查询司机（支持手机号/邮箱/姓名），需后端提供对应接口
export const searchDrivers = (keyword: string) => {
  return request.get<Result<DriverOption[]>>('/dispatcher/drivers', {
    params: { keyword },
  });
};

// ============ 分配相关API ============
// 手动分配车辆
export const assignVehicle = (oldWaybillId: string, carId: number) => {
  return request.post<Result<number>>('/dispatcher/assign', null, {
    params: { oldWaybillId, carId },
  });
};

// 自动分配车辆
export const autoAssignVehicle = (oldWaybillId: string) => {
  return request.post<Result<number>>('/dispatcher/assign/auto', null, {
    params: { oldWaybillId },
  });
};

// 重新分配车辆
export const reassignVehicle = (oldWaybillId: string, newCarId: number) => {
  return request.post<Result<number>>('/dispatcher/assign/reassign', null, {
    params: { oldWaybillId, newCarId },
  });
};

// 分配历史记录
export interface WaybillAssignmentVO {
  assignmentId: number;
  oldWaybillId: string;
  newWaybillId: string;
  operatorId: number;
  operatorName?: string;
  createTime: string;
}

export const getAssignmentHistory = (params: {
  oldWaybillId?: number;
  newWaybillId?: number;
  operatorId?: number;
  current?: number;
  size?: number;
}) => {
  return request.get<Result<PageResult<WaybillAssignmentVO>>>('/dispatcher/assign/history', { params });
};

// ============ 司机相关API ============
export interface DriverRegisterDTO {
  phone: string;
  email: string;
  password: string;
}

export interface DriverLoginDTO {
  // 后端 DriverLoginDTO 使用 account 字段（可为账号/手机号）
  account: string;
  password: string;
}

export interface DriverLoginVO {
  token: string;
  driverId: number;
  phone: string;
  name: string;
}

// 司机查询 DTO（调度员侧查询司机列表）
export interface DriverQueryDTO {
  keyword?: string; // 可用于按手机号/姓名/账号模糊搜索
  phone?: string;
  name?: string;
  current?: number;
  size?: number;
}

// 司机信息 VO（用于下拉选择）
export interface DriverVO {
  driverId: number;
  userId?: number;
  username?: string;
  name?: string;
  phone?: string;
  email?: string;
  status?: number;
}

// 运单异常上报 DTO（与后端 WaybillExceptionReportDTO 对齐）
export interface WaybillExceptionReportDTO {
  waybillIdentification: string; // 使用 waybillIdentification 作为业务标识
  description: string;
  exceptionDate: string; // yyyy-MM-dd HH:mm:ss
}

// 司机注册
export const driverRegister = (data: DriverRegisterDTO) => {
  return request.post<Result>('/driver/register', data);
};

// 司机登录
export const driverLogin = (data: DriverLoginDTO) => {
  return request.post<Result<DriverLoginVO>>('/driver/login', data);
};

// 调度员查询司机列表
export const queryDriversForDispatcher = (data: DriverQueryDTO) => {
  return request.post<Result<PageResult<DriverVO>>>('/dispatcher/drivers', data);
};

// 上报运单异常
export const reportWaybillException = (data: WaybillExceptionReportDTO) => {
  return request.post<Result>('/driver/exception', data);
};

// 查询司机运单列表
export const driverQueryWaybills = (data: WaybillQueryDTO) => {
  return request.post<Result<PageResult<WaybillVO>>>('/driver/waybills', data);
};

// 司机开始运输
export const driverStartWaybill = (waybillId: string) => {
  return request.post<Result>(`/driver/start/${waybillId}`);
};

// 司机完成运输
export const driverCompleteWaybill = (waybillId: string) => {
  return request.post<Result>(`/driver/complete/${waybillId}`);
};

// ============ 管理员相关API ============
export interface AdminLoginDTO {
  username: string;
  password: string;
}

export interface AdminLoginVO {
  token: string;
  userId: number;
  username: string;
}

// 用户查询DTO
export interface UserQueryDTO {
  type?: number; // 用户类型 0:管理员 1:货主 2:调度员 3:司机
  username?: string; // 用户名（模糊查询）
  status?: number; // 账户状态 0:禁用 1:启用
  current?: number; // 当前页码
  size?: number; // 每页大小
}

// 用户创建DTO
export interface UserCreateDTO {
  type: number; // 用户类型 1:货主 2:调度员 3:司机
  username: string; // 用户名
  password: string; // 密码
  email?: string; // 邮箱（货主、司机必填）
  phone?: string; // 手机号（货主、司机必填，调度员可选）
}

// 用户更新DTO
export interface UserUpdateDTO {
  username?: string; // 用户名
  password?: string; // 密码（为空则不修改）
  email?: string; // 邮箱
  phone?: string; // 手机号
}

// 用户信息VO
export interface UserVO {
  userId: number; // 用户ID
  type: number; // 用户类型 0:管理员 1:货主 2:调度员 3:司机
  typeDesc: string; // 用户类型描述
  username: string; // 用户名
  status: number; // 账户状态 0:禁用 1:启用
  statusDesc: string; // 状态描述
  email?: string; // 邮箱
  phone?: string; // 手机号
  roleId?: number; // 角色ID（consignorId/dispatcherId/driverId）
}

// 管理员登录
export const adminLogin = (data: AdminLoginDTO) => {
  return request.post<Result<AdminLoginVO>>('/admin/login', data);
};

// 管理员登出
export const adminLogout = () => {
  return request.post<Result>('/admin/logout');
};

// 查询用户列表（分页）
export const queryUsers = (data: UserQueryDTO) => {
  return request.post<Result<PageResult<UserVO>>>('/admin/users/query', data);
};

// 创建用户（货主、调度员、司机）
export const createUser = (data: UserCreateDTO) => {
  return request.post<Result<number>>('/admin/users/create', data);
};

// 更新用户信息
export const updateUser = (userId: number, data: UserUpdateDTO) => {
  return request.put<Result>(`/admin/users/${userId}`, data);
};

// 删除用户
export const deleteUser = (userId: number) => {
  return request.delete<Result>(`/admin/users/${userId}`);
};

// 禁用/启用用户
export const updateUserStatus = (userId: number, status: number) => {
  return request.put<Result>(`/admin/users/${userId}/status?status=${status}`);
};

