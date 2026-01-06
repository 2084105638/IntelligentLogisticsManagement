<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>司机工作台</h2>
          <div class="user-info">
            <span>{{ userInfo?.name || userInfo?.phone || '司机' }}</span>
            <el-button type="text" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 当前任务 -->
          <el-tab-pane label="当前任务" name="current">
            <el-card v-if="currentTask" class="task-card">
      <template #header>
        <div class="card-header">
                  <span>运单号: {{ currentTask.waybillId }}</span>
                  <el-tag :type="getStatusType(currentTask.status)">
                    {{ getStatusDesc(currentTask.status) }}
                  </el-tag>
        </div>
      </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="货物信息">
                  {{ currentTask.goodsInformation }}
                </el-descriptions-item>
                <el-descriptions-item label="提货点">
                  {{ currentTask.startAddress }}
                </el-descriptions-item>
                <el-descriptions-item label="卸货点">
                  {{ currentTask.endAddress }}
                </el-descriptions-item>
                <el-descriptions-item label="期望时效">
                  {{ currentTask.expectedTimeLimit }}
                </el-descriptions-item>
                <el-descriptions-item label="费用">
                  {{ currentTask.cost?.toFixed(2) }} 元
                </el-descriptions-item>
              </el-descriptions>
              <div class="task-actions">
                <el-button
                  type="primary"
                  @click="updateStatus('arrived_pickup')"
                  v-if="currentTask.status === 1"
                >
                  到达提货点
                </el-button>
                <el-button
                  type="success"
                  @click="updateStatus('start_transit')"
                  v-if="currentTask.status === 1"
                >
                  开始运输
                </el-button>
                <el-button
                  type="warning"
                  @click="updateStatus('arrived_destination')"
                  v-if="currentTask.status === 2"
                >
                  到达目的地
                </el-button>
                <el-button
                  type="success"
                  @click="updateStatus('delivered')"
                  v-if="currentTask.status === 2"
                >
                  确认送达
                </el-button>
                <el-button type="danger" @click="showIssueDialog">异常上报</el-button>
              </div>
            </el-card>
            <el-empty v-else description="暂无活跃任务" />
          </el-tab-pane>

          <!-- 历史任务 -->
          <el-tab-pane label="历史任务" name="history">
            <el-card>
              <el-table :data="historyList" style="width: 100%" v-loading="loading">
                <el-table-column prop="waybillId" label="运单号" width="120" />
                <el-table-column prop="goodsInformation" label="货物信息" width="200" />
                <el-table-column prop="startAddress" label="提货点" width="150" />
                <el-table-column prop="endAddress" label="卸货点" width="150" />
                <el-table-column prop="cost" label="费用(元)" width="100">
                  <template #default="scope">
                    {{ scope.row.cost?.toFixed(2) }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">
                      {{ getStatusDesc(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" />
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button size="small" @click="viewTaskDetail(scope.row)">详情</el-button>
                    <el-button
                      size="small"
                      type="primary"
                      @click="uploadReceipt(scope.row)"
                      v-if="scope.row.status === 3"
                    >
                      上传回单
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-model:current-page="historyPage"
                v-model:page-size="historyPageSize"
                :page-sizes="[10, 20, 50]"
                :total="historyTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadHistory"
                @current-change="loadHistory"
                style="margin-top: 20px"
              />
            </el-card>
          </el-tab-pane>

          <!-- 收入统计 -->
          <el-tab-pane label="收入统计" name="income">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">¥{{ income.totalIncome.toFixed(2) }}</div>
                    <div class="stat-label">总收入</div>
      </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ income.completedTasks }}</div>
                    <div class="stat-label">已完成任务</div>
      </div>
    </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">¥{{ income.monthIncome.toFixed(2) }}</div>
                    <div class="stat-label">本月收入</div>
    </div>
                </el-card>
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <!-- 异常上报对话框 -->
    <el-dialog v-model="issueDialogVisible" title="异常上报" width="500px">
      <el-form :model="issueForm" label-width="100px">
        <el-form-item label="异常类型" required>
          <el-select v-model="issueForm.type" placeholder="请选择异常类型" style="width: 100%">
            <el-option label="堵车" value="traffic_jam" />
            <el-option label="等待" value="waiting" />
            <el-option label="事故" value="accident" />
            <el-option label="车辆故障" value="vehicle_breakdown" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常描述">
          <el-input
            v-model="issueForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述异常情况"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitIssue">提交</el-button>
          <el-button @click="issueDialogVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 上传回单对话框 -->
    <el-dialog v-model="receiptDialogVisible" title="上传电子回单" width="500px">
      <el-form :model="receiptForm" label-width="100px">
        <el-form-item label="运单号">
          <el-input v-model="receiptForm.waybillId" disabled />
        </el-form-item>
        <el-form-item label="签收单">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleReceiptChange"
            :file-list="receiptFileList"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png/pdf格式，最大10MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="货物照片">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleGoodsChange"
            :file-list="goodsFileList"
            multiple
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png格式，最多5张</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitReceipt">提交</el-button>
          <el-button @click="receiptDialogVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="任务详情" width="800px">
      <el-descriptions :column="2" border v-if="currentTaskDetail">
        <el-descriptions-item label="运单号">{{ currentTaskDetail.waybillId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTaskDetail.status)">
            {{ getStatusDesc(currentTaskDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="货物信息" :span="2">
          {{ currentTaskDetail.goodsInformation }}
        </el-descriptions-item>
        <el-descriptions-item label="提货点">{{ currentTaskDetail.startAddress }}</el-descriptions-item>
        <el-descriptions-item label="卸货点">{{ currentTaskDetail.endAddress }}</el-descriptions-item>
        <el-descriptions-item label="费用">{{ currentTaskDetail.cost?.toFixed(2) }} 元</el-descriptions-item>
        <el-descriptions-item label="期望时效">{{ currentTaskDetail.expectedTimeLimit }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTaskDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getUserInfo } from '../utils/auth';
import { getStatusDesc, getStatusType } from '../utils/waybillStatus';
import { WaybillVO } from '../api';

export default defineComponent({
  name: 'DriverDashboard',
  setup() {
    const router = useRouter();
    const activeTab = ref('current');
    const loading = ref(false);
    const currentTask = ref<WaybillVO | null>(null);
    const historyList = ref<WaybillVO[]>([]);
    const historyPage = ref(1);
    const historyPageSize = ref(10);
    const historyTotal = ref(0);
    const userInfo = ref<any>(null);
    const issueDialogVisible = ref(false);
    const receiptDialogVisible = ref(false);
    const detailDialogVisible = ref(false);
    const currentTaskDetail = ref<WaybillVO | null>(null);
    const receiptFileList = ref<any[]>([]);
    const goodsFileList = ref<any[]>([]);

    const issueForm = reactive({
      type: '',
      description: '',
    });

    const receiptForm = reactive({
      waybillId: 0,
    });

    const income = reactive({
      totalIncome: 0,
      completedTasks: 0,
      monthIncome: 0,
    });

    onMounted(() => {
      userInfo.value = getUserInfo();
      loadCurrentTask();
      loadHistory();
      loadIncome();
    });

    const handleTabChange = (tab: string) => {
      if (tab === 'history') {
        loadHistory();
      } else if (tab === 'income') {
        loadIncome();
      }
    };

    const loadCurrentTask = async () => {
      // 模拟加载当前任务 - 实际应该调用API获取司机当前分配的运单
      // 这里假设状态为1或2的运单是当前任务
      try {
        // TODO: 调用API获取当前任务
        // const result = await getDriverCurrentTask();
        // currentTask.value = result.data;
        
        // 模拟数据
        currentTask.value = null; // 暂时设为null，实际应该从API获取
      } catch (error) {
        console.error('加载当前任务失败:', error);
      }
    };

    const loadHistory = async () => {
      loading.value = true;
      try {
        // TODO: 调用API获取历史任务
        // const result = await getDriverHistoryTasks(historyPage.value, historyPageSize.value);
        // historyList.value = result.data.records || [];
        // historyTotal.value = result.data.total || 0;
        
        // 模拟数据
        historyList.value = [];
        historyTotal.value = 0;
      } catch (error: any) {
        ElMessage.error(error.message || '加载历史任务失败');
      } finally {
        loading.value = false;
      }
    };

    const loadIncome = () => {
      // 计算收入统计
      const completed = historyList.value.filter((task) => task.status === 3);
      income.completedTasks = completed.length;
      income.totalIncome = completed.reduce((sum, task) => sum + (task.cost || 0), 0);
      // 本月收入需要根据时间筛选
      income.monthIncome = income.totalIncome * 0.3; // 模拟数据
    };

    const updateStatus = async (status: string) => {
      if (!currentTask.value) return;
      
      try {
        // TODO: 调用API更新运单状态
        // await updateWaybillStatus(currentTask.value.waybillId, status);
        
        ElMessage.success('状态更新成功');
        loadCurrentTask();
      } catch (error: any) {
        ElMessage.error(error.message || '状态更新失败');
      }
    };

    const showIssueDialog = () => {
      issueForm.type = '';
      issueForm.description = '';
      issueDialogVisible.value = true;
    };

    const submitIssue = async () => {
      if (!issueForm.type) {
        ElMessage.warning('请选择异常类型');
        return;
      }
      
      try {
        // TODO: 调用API上报异常
        // await reportIssue(currentTask.value?.waybillId, issueForm);
        
        ElMessage.success('异常上报成功');
        issueDialogVisible.value = false;
      } catch (error: any) {
        ElMessage.error(error.message || '异常上报失败');
      }
    };

    const uploadReceipt = (row: WaybillVO) => {
      receiptForm.waybillId = row.waybillId;
      receiptFileList.value = [];
      goodsFileList.value = [];
      receiptDialogVisible.value = true;
    };

    const handleReceiptChange = (file: any) => {
      receiptFileList.value = [file];
    };

    const handleGoodsChange = (file: any, fileList: any[]) => {
      goodsFileList.value = fileList;
    };

    const submitReceipt = async () => {
      if (receiptFileList.value.length === 0) {
        ElMessage.warning('请上传签收单');
        return;
      }
      
      try {
        // TODO: 调用API上传回单
        // await uploadElectronicReceipt(receiptForm.waybillId, receiptFileList.value, goodsFileList.value);
        
        ElMessage.success('回单上传成功');
        receiptDialogVisible.value = false;
        loadHistory();
      } catch (error: any) {
        ElMessage.error(error.message || '回单上传失败');
      }
    };

    const viewTaskDetail = (row: WaybillVO) => {
      currentTaskDetail.value = row;
      detailDialogVisible.value = true;
    };

    const handleLogout = async () => {
      try {
        // TODO: 调用登出API（如果后端有提供）
        // await driverLogout();
      } catch (error) {
        // 忽略错误
      } finally {
        router.push('/login');
      }
    };

    return {
      activeTab,
      loading,
      currentTask,
      historyList,
      historyPage,
      historyPageSize,
      historyTotal,
      userInfo,
      issueDialogVisible,
      issueForm,
      receiptDialogVisible,
      receiptForm,
      detailDialogVisible,
      currentTaskDetail,
      receiptFileList,
      goodsFileList,
      income,
      handleTabChange,
      loadCurrentTask,
      loadHistory,
      loadIncome,
      updateStatus,
      showIssueDialog,
      submitIssue,
      uploadReceipt,
      handleReceiptChange,
      handleGoodsChange,
      submitReceipt,
      viewTaskDetail,
      handleLogout,
      getStatusDesc,
      getStatusType,
    };
  },
});
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.task-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}
</style>
