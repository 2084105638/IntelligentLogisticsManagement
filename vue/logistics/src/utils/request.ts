import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API地址
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = token;
    }
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<any>) => {
    const res = response.data;
    
    // 如果返回的状态码不是200，则认为是错误
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败');
      
      // 401: 未授权，跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        router.push('/login');
      }
      
      return Promise.reject(new Error(res.msg || '请求失败'));
    } else {
      return res;
    }
  },
  (error) => {
    console.error('响应错误:', error);
    ElMessage.error(error.message || '网络错误');
    return Promise.reject(error);
  }
);

export default service;

