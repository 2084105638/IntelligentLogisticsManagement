// Token管理工具

export const setToken = (token: string) => {
  localStorage.setItem('token', token);
};

export const getToken = (): string | null => {
  return localStorage.getItem('token');
};

export const removeToken = () => {
  localStorage.removeItem('token');
};

export const setUserInfo = (userInfo: any) => {
  localStorage.setItem('userInfo', JSON.stringify(userInfo));
};

export const getUserInfo = (): any => {
  const userInfo = localStorage.getItem('userInfo');
  return userInfo ? JSON.parse(userInfo) : null;
};

export const removeUserInfo = () => {
  localStorage.removeItem('userInfo');
};

export const clearAuth = () => {
  removeToken();
  removeUserInfo();
};

// 获取用户角色
export const getUserRole = (): string | null => {
  const userInfo = getUserInfo();
  return userInfo?.role || null;
};

