<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>智慧物流管理系统</h2>
          <el-tabs v-model="activeRole" @tab-change="handleRoleChange">
            <el-tab-pane label="货主" name="consignor"></el-tab-pane>
            <el-tab-pane label="调度员" name="dispatcher"></el-tab-pane>
            <el-tab-pane label="司机" name="driver"></el-tab-pane>
            <el-tab-pane label="管理员" name="admin"></el-tab-pane>
          </el-tabs>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        v-if="!showRegister"
      >
        <el-form-item :label="activeRole === 'admin' ? '用户名' : '手机号'" prop="phone">
          <el-input 
            v-model="loginForm.phone" 
            :placeholder="activeRole === 'admin' ? '请输入用户名' : '请输入手机号'" 
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <el-form-item v-if="activeRole === 'consignor' || activeRole === 'driver'">
          <el-link type="primary" @click="showRegister = true">还没有账号？立即注册</el-link>
        </el-form-item>
      </el-form>

      <!-- 注册表单 -->
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
        v-else
      >
        <el-form-item label="手机号" prop="phone" v-if="activeRole === 'driver'">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email" v-if="activeRole === 'consignor'">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" v-if="activeRole === 'consignor'">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name" v-if="activeRole === 'driver'">
          <el-input v-model="registerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="驾驶证号" prop="licenseNumber" v-if="activeRole === 'driver'">
          <el-input v-model="registerForm.licenseNumber" placeholder="请输入驾驶证号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="showRegister = false">已有账号？立即登录</el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  consignorLogin,
  consignorRegister,
  dispatcherLogin,
  driverLogin,
  driverRegister,
  adminLogin,
  ConsignorRegisterDTO,
  DriverRegisterDTO,
  AdminLoginDTO,
} from '../api';
import { setToken, setUserInfo } from '../utils/auth';

export default defineComponent({
  name: 'LoginView',
  setup() {
    const router = useRouter();
    const activeRole = ref('consignor');
    const showRegister = ref(false);
    const loading = ref(false);
    const loginFormRef = ref();
    const registerFormRef = ref();

    const loginForm = reactive({
      phone: '',
      password: '',
      username: '', // 调度员使用
    });

    const registerForm = reactive({
      email: '',
      phone: '',
      password: '',
      name: '',
      licenseNumber: '',
    });

    const loginRules = {
      phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    };

    const registerRules = {
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
      ],
      phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
      licenseNumber: [{ required: true, message: '请输入驾驶证号', trigger: 'blur' }],
    };

    const handleRoleChange = () => {
      showRegister.value = false;
      // 清空表单
      Object.keys(loginForm).forEach((key) => {
        (loginForm as any)[key] = '';
      });
      Object.keys(registerForm).forEach((key) => {
        (registerForm as any)[key] = '';
      });
    };

    const handleLogin = async () => {
      if (!loginFormRef.value) return;
      await loginFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          loading.value = true;
          try {
            let result: any;
            if (activeRole.value === 'consignor') {
              result = await consignorLogin({
                phone: loginForm.phone,
                password: loginForm.password,
              });
            } else if (activeRole.value === 'dispatcher') {
              result = await dispatcherLogin({
                username: loginForm.phone, // 调度员可能用手机号作为用户名
                password: loginForm.password,
              });
            } else if (activeRole.value === 'driver') {
              result = await driverLogin({
                phone: loginForm.phone,
                password: loginForm.password,
              });
            } else if (activeRole.value === 'admin') {
              result = await adminLogin({
                username: loginForm.phone, // 管理员使用用户名登录
                password: loginForm.password,
              });
            }

            if (result.data) {
              setToken(result.data.token);
              setUserInfo({ ...result.data, role: activeRole.value });
              ElMessage.success('登录成功');
              // 跳转到对应的工作台
              if (activeRole.value === 'consignor') {
                router.push('/owner');
              } else if (activeRole.value === 'dispatcher') {
                router.push('/dispatcher');
              } else if (activeRole.value === 'driver') {
                router.push('/driver');
              } else if (activeRole.value === 'admin') {
                router.push('/admin');
              }
            }
          } catch (error: any) {
            ElMessage.error(error.message || '登录失败');
          } finally {
            loading.value = false;
          }
        }
      });
    };

    const handleRegister = async () => {
      if (!registerFormRef.value) return;
      await registerFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          loading.value = true;
          try {
            if (activeRole.value === 'consignor') {
              const data: ConsignorRegisterDTO = {
                email: registerForm.email,
                phone: registerForm.phone,
                password: registerForm.password,
              };
              await consignorRegister(data);
              ElMessage.success('注册成功，请登录');
              showRegister.value = false;
            } else if (activeRole.value === 'driver') {
              const data: DriverRegisterDTO = {
                phone: registerForm.phone,
                password: registerForm.password,
                name: registerForm.name,
                licenseNumber: registerForm.licenseNumber,
              };
              await driverRegister(data);
              ElMessage.success('注册成功，请登录');
              showRegister.value = false;
            }
          } catch (error: any) {
            ElMessage.error(error.message || '注册失败');
          } finally {
            loading.value = false;
          }
        }
      });
    };

    return {
      activeRole,
      showRegister,
      loading,
      loginForm,
      registerForm,
      loginRules,
      registerRules,
      loginFormRef,
      registerFormRef,
      handleRoleChange,
      handleLogin,
      handleRegister,
    };
  },
});
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 450px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 20px 0;
  color: #333;
}
</style>

