<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-shell">
      <div class="login-left">
        <div class="login-logo">
          <span class="logo-dot" />
          <div>
            <div class="logo-title">智慧物流运输管理平台</div>
            <div class="logo-subtitle">一站式运单调度与执行管理</div>
          </div>
        </div>
        <ul class="login-highlights">
          <li>
            <span class="dot dot-green"></span>
            货主实时查看运输进度，支持自助下单与查询
          </li>
          <li>
            <span class="dot dot-blue"></span>
            调度员统一管理运单与车辆，减少沟通成本
          </li>
          <li>
            <span class="dot dot-yellow"></span>
            司机移动端接单、上报异常，流程清晰可追溯
          </li>
          <li>
            <span class="dot dot-red"></span>
            管理员全局监控运行状态，提升安全与合规性
          </li>
        </ul>
        <div class="login-tip">
          <span>提示：</span>
          货主 / 司机支持在线注册，调度员 / 管理员由系统管理员分配账号。
        </div>
      </div>

      <div class="login-right">
        <el-card class="login-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-title-group">
                <h2>欢迎登录</h2>
                <p>请选择您的登录角色并输入账号信息</p>
              </div>
              <el-tabs v-model="activeRole" @tab-change="handleRoleChange" class="role-tabs">
                <el-tab-pane label="货主" name="consignor" />
                <el-tab-pane label="调度员" name="dispatcher" />
                <el-tab-pane label="司机" name="driver" />
                <el-tab-pane label="管理员" name="admin" />
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
            <el-form-item
              :label="activeRole === 'admin' || activeRole === 'dispatcher' ? '用户名' : activeRole === 'driver' ? '账号' : '手机号'"
              prop="phone"
            >
              <el-input
                v-model="loginForm.phone"
                :placeholder="activeRole === 'admin' || activeRole === 'dispatcher'
                  ? '请输入用户名'
                  : activeRole === 'driver'
                    ? '请输入司机账号（可为手机号）'
                    : '请输入手机号'"
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
              <el-button
                type="primary"
                @click="handleLogin"
                :loading="loading"
                class="full-btn"
              >
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
            <el-form-item label="邮箱" prop="email" v-if="activeRole === 'consignor' || activeRole === 'driver'">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone" v-if="activeRole === 'consignor'">
              <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
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
              <el-button
                type="primary"
                @click="handleRegister"
                :loading="loading"
                class="full-btn"
              >
                注册
              </el-button>
            </el-form-item>
            <el-form-item>
              <el-link type="primary" @click="showRegister = false">已有账号？立即登录</el-link>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  consignorLogin,
  consignorRegister,
  dispatcherLogin,
  driverLogin,
  driverRegister,
  adminLogin,
  type ConsignorRegisterDTO,
  type DriverRegisterDTO,
} from '../api';
import { setToken, setUserInfo } from '../utils/auth';

const router = useRouter();
const activeRole = ref('consignor');
const showRegister = ref(false);
const loading = ref(false);
const loginFormRef = ref();
const registerFormRef = ref();

const loginForm = reactive({
  phone: '',
  password: '',
  username: '',
});

const registerForm = reactive({
  email: '',
  phone: '',
  password: '',
  name: '',
  licenseNumber: '',
});

const loginRules = {
  phone: [{ required: true, message: '请输入手机号或用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
};

const registerRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的11位手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
};

const handleRoleChange = () => {
  showRegister.value = false;
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
            account: loginForm.phone,
            password: loginForm.password,
          });
        } else if (activeRole.value === 'dispatcher') {
          result = await dispatcherLogin({
            username: loginForm.phone,
            password: loginForm.password,
          });
        } else if (activeRole.value === 'driver') {
          result = await driverLogin({
            account: loginForm.phone,
            password: loginForm.password,
          });
        } else if (activeRole.value === 'admin') {
          result = await adminLogin({
            username: loginForm.phone,
            password: loginForm.password,
          });
        }

        if (result.data) {
          setToken(result.data.token);
          setUserInfo({ ...result.data, role: activeRole.value });
          ElMessage.success('登录成功');
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
            email: registerForm.email,
            password: registerForm.password,
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
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: radial-gradient(circle at top left, #4f46e5 0, #0f172a 45%, #020617 100%);
}

.login-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 10% 20%, rgba(96, 165, 250, 0.25), transparent 55%),
    radial-gradient(circle at 90% 80%, rgba(129, 140, 248, 0.22), transparent 55%);
  opacity: 0.95;
  filter: blur(2px);
}

.login-shell {
  position: relative;
  max-width: 1120px;
  margin: 0 auto;
  min-height: 100vh;
  padding: 32px 20px;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr);
  gap: 32px;
  align-items: center;
  color: #e5e7eb;
}

.login-left {
  padding-right: 16px;
}

.login-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.logo-dot {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #22c55e, #4ade80);
  box-shadow: 0 0 14px rgba(22, 163, 74, 0.85);
}

.logo-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.06em;
}

.logo-subtitle {
  font-size: 13px;
  color: #cbd5f5;
  margin-top: 2px;
}

.login-highlights {
  list-style: none;
  padding: 0;
  margin: 0 0 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
}

.login-highlights li {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #d1d5db;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

.dot-green {
  background: #22c55e;
}

.dot-blue {
  background: #3b82f6;
}

.dot-yellow {
  background: #eab308;
}

.dot-red {
  background: #f97373;
}

.login-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #9ca3af;
}

.login-tip span {
  color: #e5e7eb;
}

.login-right {
  display: flex;
  justify-content: center;
}

.login-card {
  width: 420px;
  background: rgba(15, 23, 42, 0.97);
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.5);
  box-shadow: 0 20px 55px rgba(15, 23, 42, 0.9);
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-title-group h2 {
  margin: 0;
  font-size: 20px;
  color: #f9fafb;
}

.card-title-group p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.role-tabs :deep(.el-tabs__item) {
  color: #cbd5f5;
}

.role-tabs :deep(.el-tabs__item.is-active) {
  color: #60a5fa;
}

.role-tabs :deep(.el-tabs__active-bar) {
  background-color: #60a5fa;
}

.full-btn {
  width: 100%;
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: minmax(0, 1fr);
    padding-top: 24px;
    padding-bottom: 24px;
  }

  .login-left {
    padding-right: 0;
  }

  .login-right {
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .login-shell {
    padding-inline: 16px;
  }

  .login-card {
    width: 100%;
  }
}
</style>

