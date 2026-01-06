// 运单状态工具

export const WAYBILL_STATUS = {
  PENDING_ALLOCATION: 0, // 待分配
  ALLOCATED: 1, // 已分配
  IN_TRANSIT: 2, // 运输中
  COMPLETED: 3, // 已完成
};

export const getStatusDesc = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '待分配',
    1: '已分配',
    2: '运输中',
    3: '已完成',
  };
  return statusMap[status] || '未知';
};

export const getStatusType = (status: number): string => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success',
  };
  return typeMap[status] || '';
};

// 车辆状态
export const CAR_STATUS = {
  AVAILABLE: 0, // 可用
  ASSIGNED: 1, // 已分配
  MAINTENANCE: 2, // 维修中
};

export const getCarStatusDesc = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '可用',
    1: '已分配',
    2: '维修中',
  };
  return statusMap[status] || '未知';
};

export const getCarStatusType = (status: number): string => {
  const typeMap: Record<number, string> = {
    0: 'success',
    1: 'warning',
    2: 'danger',
  };
  return typeMap[status] || '';
};

