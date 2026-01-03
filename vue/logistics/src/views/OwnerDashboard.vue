<template>
  <div class="dashboard">
    <h1>货主工作台</h1>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="发布运单" name="create">
        <el-form :model="orderForm" label-width="120px">
          <el-form-item label="货物信息">
            <el-input v-model="orderForm.goods" placeholder="货物描述"></el-input>
          </el-form-item>
          <el-form-item label="发货地址">
            <el-input v-model="orderForm.origin" placeholder="提货地址"></el-input>
          </el-form-item>
          <el-form-item label="收货地址">
            <el-input v-model="orderForm.destination" placeholder="送货地址"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="createOrder">提交订单</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="运单追踪" name="track">
        <el-table :data="orders" style="width: 100%">
          <el-table-column prop="id" label="运单号" width="180" />
          <el-table-column prop="goods" label="货物" width="180" />
          <el-table-column prop="status" label="状态" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" @click="viewDetails(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';

export default defineComponent({
  name: 'OwnerDashboard',
  setup() {
    const activeTab = ref('create');
    const orderForm = reactive({
      goods: '',
      origin: '',
      destination: '',
    });
    const orders = ref([
      { id: 'ORD001', goods: '电子产品', status: '运输中' },
      { id: 'ORD002', goods: '家具', status: '待处理' },
    ]);

    const createOrder = () => {
      console.log('Order created:', orderForm);
      alert('订单已提交 (模拟)');
    };

    const viewDetails = (row: any) => {
      console.log('View details:', row);
    };

    return {
      activeTab,
      orderForm,
      orders,
      createOrder,
      viewDetails,
    };
  },
});
</script>
