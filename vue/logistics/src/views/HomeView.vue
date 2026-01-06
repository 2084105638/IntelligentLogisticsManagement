<template>
  <div class="home">
    <div class="home-bg"></div>
    <div class="home-content">
      <header class="home-header">
        <div class="logo-text">
          <span class="logo-dot" />
          <span class="logo-title">智慧物流运输管理平台</span>
        </div>
        <span class="header-subtitle">统一调度 · 可视运输 · 安全高效</span>
      </header>

      <main class="home-main">
        <section class="hero">
          <h1 class="hero-title">让物流调度更智能，让运输执行更安心</h1>
          <p class="hero-subtitle">
            面向货主、调度员、司机和管理员的一体化运输管理平台，支持运单全流程跟踪、异常上报与智能分配。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="goToLogin('consignor')">
              立即登录 / 注册
            </el-button>
            <el-button size="large" plain @click="goToLogin('dispatcher')">
              进入调度工作台
            </el-button>
          </div>
        </section>

        <section class="roles-section">
          <div class="role-card" @click="goToLogin('consignor')">
            <h3>货主端</h3>
            <p>在线下单、查看运单进度，实时掌握货物运输状态。</p>
            <el-tag type="success" size="small">支持自助注册</el-tag>
          </div>
          <div class="role-card" @click="goToLogin('dispatcher')">
            <h3>调度员端</h3>
            <p>统一管理运单与车辆，智能分配司机，提高运输效率。</p>
            <el-tag type="warning" size="small">内部账号登录</el-tag>
          </div>
          <div class="role-card" @click="goToLogin('driver')">
            <h3>司机端</h3>
            <p>接收派单、回传定位、上报异常，简化司机操作流程。</p>
            <el-tag type="info" size="small">支持手机注册</el-tag>
          </div>
          <div class="role-card" @click="goToLogin('admin')">
            <h3>管理员端</h3>
            <p>全局监控平台运行情况与用户信息，保障系统安全稳定。</p>
            <el-tag type="danger" size="small">管理员专用</el-tag>
          </div>
        </section>
      </main>

      <footer class="home-footer">
        <span>© {{ new Date().getFullYear() }} 智慧物流运输管理系统</span>
        <span class="footer-divider">|</span>
        <span>推荐使用最新版 Chrome / Edge 浏览器访问</span>
      </footer>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'HomeView',
  setup() {
    const router = useRouter();
    
    const goToLogin = (role: string) => {
      router.push('/login');
    };
    
    return {
      goToLogin,
    };
  },
});
</script>

<style scoped>
.home {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  color: #1f2933;
  background: radial-gradient(circle at top left, #4f46e5 0, #0f172a 45%, #020617 100%);
}

.home-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 10% 20%, rgba(96, 165, 250, 0.28), transparent 55%),
    radial-gradient(circle at 90% 80%, rgba(129, 140, 248, 0.24), transparent 55%);
  opacity: 0.9;
  filter: blur(2px);
}

.home-content {
  position: relative;
  max-width: 1120px;
  margin: 0 auto;
  padding: 32px 20px 40px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  color: #e5e7eb;
}

.logo-text {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 18px;
}

.logo-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #22c55e, #4ade80);
  box-shadow: 0 0 12px rgba(22, 163, 74, 0.8);
}

.logo-title {
  letter-spacing: 0.08em;
}

.header-subtitle {
  font-size: 13px;
  opacity: 0.9;
}

.home-main {
  flex: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1.2fr);
  gap: 32px;
  align-items: center;
}

.hero {
  color: #f9fafb;
}

.hero-title {
  font-size: 32px;
  line-height: 1.3;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 14px;
  line-height: 1.7;
  color: #e5e7eb;
  opacity: 0.9;
  max-width: 480px;
}

.hero-actions {
  margin-top: 24px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.roles-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.role-card {
  padding: 16px 18px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.35);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.75);
  cursor: pointer;
  transition: all 0.18s ease-out;
  color: #e5e7eb;
}

.role-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.role-card p {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.6;
  color: #cbd5f5;
}

.role-card:hover {
  transform: translateY(-3px) translateZ(0);
  box-shadow: 0 22px 55px rgba(15, 23, 42, 0.9);
  border-color: rgba(129, 140, 248, 0.8);
  background: rgba(15, 23, 42, 0.98);
}

.home-footer {
  margin-top: 40px;
  font-size: 12px;
  color: #9ca3af;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.footer-divider {
  opacity: 0.6;
}

@media (max-width: 960px) {
  .home-content {
    padding-top: 24px;
    padding-bottom: 24px;
  }

  .home-main {
    grid-template-columns: minmax(0, 1fr);
    gap: 24px;
  }

  .hero-title {
    font-size: 26px;
  }

  .roles-section {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 640px) {
  .home-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .hero-actions {
    flex-direction: column;
  }
}
</style>
