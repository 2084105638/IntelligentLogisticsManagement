<template>
  <div class="dashboard">
    <h1>司机工作台 (移动端视图)</h1>
    <el-card v-if="currentTask">
      <template #header>
        <div class="card-header">
          <span>当前任务: {{ currentTask.id }}</span>
          <el-tag type="warning">进行中</el-tag>
        </div>
      </template>
      <p><strong>提货点:</strong> {{ currentTask.origin }}</p>
      <p><strong>卸货点:</strong> {{ currentTask.destination }}</p>
      <div style="margin-top: 20px;">
        <el-button type="primary" @click="updateStatus('arrived_pickup')">到达提货点</el-button>
        <el-button type="success" @click="updateStatus('delivered')">确认送达</el-button>
      </div>
      <div style="margin-top: 20px;">
         <el-button type="danger" @click="reportIssue">异常上报</el-button>
      </div>
    </el-card>
    <div v-else>
      <el-empty description="暂无活跃任务"></el-empty>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  name: 'DriverDashboard',
  setup() {
    const currentTask = ref({
      id: 'ORD001',
      origin: '仓库 A',
      destination: '门店 B',
    });

    const updateStatus = (status: string) => {
      console.log('Updating status to:', status);
      alert(`状态已更新为: ${status}`);
    };

    const reportIssue = () => {
      alert('异常上报弹窗');
    };

    return {
      currentTask,
      updateStatus,
      reportIssue,
    };
  },
});
</script>
