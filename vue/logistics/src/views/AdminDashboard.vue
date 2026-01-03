<template>
  <div class="dashboard">
    <h1>管理员工作台</h1>
    <el-tabs type="border-card">
      <el-tab-pane label="用户管理">用户管理内容</el-tab-pane>
      <el-tab-pane label="基础数据管理">基础数据管理内容</el-tab-pane>
      <el-tab-pane label="系统监控">系统监控内容</el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'AdminDashboard',
});
</script>
