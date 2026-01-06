<template>
  <div class="home">
    <h1>欢迎使用智慧物流运输管理系统</h1>
    <div class="role-selection">
      <el-button type="primary" @click="goToLogin('consignor')">货主端</el-button>
      <el-button type="success" @click="goToLogin('dispatcher')">调度员端</el-button>
      <el-button type="warning" @click="goToLogin('driver')">司机端</el-button>
      <el-button type="danger" @click="goToLogin('admin')">管理员端</el-button>
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
  text-align: center;
  padding: 50px;
}
.role-selection {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>
