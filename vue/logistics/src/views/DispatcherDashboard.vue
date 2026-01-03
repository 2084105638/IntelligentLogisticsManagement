<template>
  <div class="dashboard">
    <h1>调度员工作台</h1>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待分配运单</span>
            </div>
          </template>
          <el-table :data="pendingOrders" style="width: 100%">
            <el-table-column prop="id" label="运单号" width="100" />
            <el-table-column prop="goods" label="货物" />
            <el-table-column label="操作">
              <template #default="scope">
                <el-button type="primary" size="small" @click="assignDriver(scope.row)">派单</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>活跃车辆</span>
            </div>
          </template>
          <!-- Map placeholder -->
          <div style="height: 300px; background-color: #eee; display: flex; align-items: center; justify-content: center;">
            地图可视化占位符
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  name: 'DispatcherDashboard',
  setup() {
    const pendingOrders = ref([
      { id: 'ORD003', goods: '纺织品' },
      { id: 'ORD004', goods: '机械设备' },
    ]);

    const assignDriver = (order: any) => {
      console.log('Assigning driver for:', order);
      alert('智能调度系统已触发 (模拟)');
    };

    return {
      pendingOrders,
      assignDriver,
    };
  },
});
</script>
