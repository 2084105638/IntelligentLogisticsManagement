import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import JSONBig from 'json-bigint';
import { ElMessage } from 'element-plus';
import router from '../router';

// 创建axios实例
const JSONbig = JSONBig({ storeAsString: true });

const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API地址
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
  // 处理 Long 精度问题，确保超出 JS 安全整数的 ID 以字符串保留
  transformResponse: [
    (data) => {
      try {
        return JSONbig.parse(data);
      } catch (err) {
        return data;
      }
    },
  ],
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 获取 token，后端不需要 Bearer 前缀，保持原样
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
      // 401: 未授权，跳转到登录页（需要显示错误）
      if (res.code === 401) {
        ElMessage.error(res.msg || '登录已过期，请重新登录');
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        router.push('/login');
        return Promise.reject(new Error(res.msg || '登录已过期'));
      }
      
      // 其他错误不在这里统一显示，由业务代码决定是否显示
      // 只记录到控制台
      console.warn('API返回非200状态码:', res.code, res.msg || '请求失败');
      
      return Promise.reject(new Error(res.msg || '请求失败'));
    } else {
      return res;
    }
  },
  (error) => {
    console.error('响应错误:', error);
    
    // 网络错误或超时错误，只在真正严重的情况下显示
    // 如果错误已经被业务代码处理，这里就不显示了
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      // 超时错误，静默处理，由业务代码决定是否提示
      console.warn('请求超时:', error.config?.url);
    } else if (error.response?.status >= 500) {
      // 服务器错误（5xx），显示错误
      ElMessage.error('服务器错误，请稍后再试');
    } else if (error.response?.status === 404) {
      // 404错误，静默处理
      console.warn('请求的资源不存在:', error.config?.url);
    } else {
      // 其他网络错误，静默处理，由业务代码决定是否提示
      console.warn('网络请求异常:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default service;

