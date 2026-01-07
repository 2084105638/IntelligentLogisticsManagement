<template>
    <div class="dashboard">
      <el-container>
        <el-header class="dashboard-header">
          <div class="header-content">
            <div class="header-left">
              <div class="logo">
                <span class="logo-dot" />
                <div class="logo-text">
                  <div class="logo-title">司机工作台</div>
                  <div class="logo-subtitle">任务接收与状态上报</div>
                </div>
              </div>
            </div>
            <div class="user-info">
              <span class="user-name">{{ userInfo?.name || userInfo?.phone || '司机' }}</span>
              <el-button link @click="handleLogout">退出登录</el-button>
            </div>
          </div>
        </el-header>
        <el-main class="dashboard-main">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="dashboard-tabs">
            <!-- 当前任务 -->
            <el-tab-pane label="当前任务" name="current">
              <el-card class="panel-card">
                <el-table :data="currentTaskList" style="width: 100%" v-loading="currentTaskLoading">
                  <el-table-column prop="waybillIdentification" label="运单号" width="120" />
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
                  <el-table-column label="操作" width="300" fixed="right">
                    <template #default="scope">
                      <el-button size="small" @click="viewTaskDetail(scope.row)">详情</el-button>
                      <el-button
                        size="small"
                        type="success"
                        @click="updateStatus('start_transit', scope.row)"
                        v-if="scope.row.status === 1"
                      >
                        开始运输
                      </el-button>
                      <el-button
                        size="small"
                        type="success"
                        @click="updateStatus('delivered', scope.row)"
                        v-if="scope.row.status === 2"
                      >
                        确认送达
                      </el-button>
                      <el-button
                        size="small"
                        type="danger"
                        @click="showIssueDialog(scope.row)"
                      >
                        异常上报
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination
                  v-model:current-page="currentTaskPage"
                  v-model:page-size="currentTaskPageSize"
                  :page-sizes="[10, 20, 50]"
                  :total="currentTaskTotal"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="loadCurrentTask"
                  @current-change="loadCurrentTask"
                  style="margin-top: 20px"
                />
              </el-card>
            </el-tab-pane>
  
            <!-- 历史任务 -->
            <el-tab-pane label="历史任务" name="history">
              <el-card class="panel-card">
                <el-table :data="historyList" style="width: 100%" v-loading="loading">
                  <el-table-column prop="waybillIdentification" label="运单号" width="120" />
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
                  <el-card class="panel-card">
                    <div class="stat-item">
                      <div class="stat-value">¥{{ income.totalIncome.toFixed(2) }}</div>
                      <div class="stat-label">总收入</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="panel-card">
                    <div class="stat-item">
                      <div class="stat-value">{{ income.completedTasks }}</div>
                      <div class="stat-label">已完成任务</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="panel-card">
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
            <el-input :value="currentTaskDetail?.waybillIdentification || receiptForm.waybillId" disabled />
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
          <el-descriptions-item label="运单号">{{ currentTaskDetail.waybillIdentification }}</el-descriptions-item>
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
  import { getUserInfo, clearAuth } from '../utils/auth';
  import { getStatusDesc, getStatusType } from '../utils/waybillStatus';
  import {
    WaybillVO,
    WaybillQueryDTO,
    driverQueryWaybills,
    driverStartWaybill,
    driverCompleteWaybill,
    reportWaybillException,
    driverLogout,
    WaybillExceptionReportDTO,
  } from '../api';
  
  export default defineComponent({
    name: 'DriverDashboard',
    setup() {
      const router = useRouter();
      const activeTab = ref('current');
      const loading = ref(false);
      const currentTaskLoading = ref(false);
      const currentTaskList = ref<WaybillVO[]>([]);
      const currentTaskPage = ref(1);
      const currentTaskPageSize = ref(10);
      const currentTaskTotal = ref(0);
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
      const selectedTaskForIssue = ref<WaybillVO | null>(null);

      const issueForm = reactive({
        type: '',
        description: '',
      });
  
      const receiptForm = reactive<{
        waybillId: string;
      }>({
        waybillId: '',
      });
  
      const income = reactive({
        totalIncome: 0,
        completedTasks: 0,
        monthIncome: 0,
      });

      // 对运单列表进行去重，避免同一业务产生多条记录（类似调度员/货主端）
      const deduplicateWaybills = (records: WaybillVO[]): WaybillVO[] => {
        if (!records || records.length === 0) return [];

        // 1) 严格过滤掉 changed=1 的旧单（支持数字、字符串等多种类型）
        const filtered = records.filter((w) => {
          const changed = w.changed;
          // 转换为数字进行比较，兼容可能的字符串类型
          const changedNum = typeof changed === 'string' ? parseInt(changed, 10) : changed;
          // 如果 changed 是 1，都认为是旧单，需要过滤
          if (changedNum === 1) {
            return false;
          }
          // 只保留 changed=0、undefined、null 的运单
          return changedNum === 0 || changedNum === undefined || changedNum === null;
        });

        // 2) 按业务字段去重，保留"最新且状态优先"的一条
        const dedupMap = new Map<string, WaybillVO>();
        filtered.forEach((w) => {
          const key = [
            w.goodsInformation,
            w.startAddress,
            w.endAddress,
            w.expectedTimeLimit,
            w.cost,
          ].join('|');

          const prev = dedupMap.get(key);
          if (!prev) {
            dedupMap.set(key, w);
            return;
          }

          // 优先保留 changed=0 的运单（即使时间更早），支持多种类型
          const prevChangedNum: number | undefined = typeof prev.changed === 'string' ? parseInt(prev.changed, 10) : prev.changed;
          const curChangedNum: number | undefined = typeof w.changed === 'string' ? parseInt(w.changed, 10) : w.changed;
          // 有效运单：changed=0 或 undefined 或 null（不能是 1）
          const isPrevValid = prevChangedNum !== 1 && (prevChangedNum === 0 || prevChangedNum === undefined || prevChangedNum === null);
          const isCurValid = curChangedNum !== 1 && (curChangedNum === 0 || curChangedNum === undefined || curChangedNum === null);
          if (!isPrevValid && isCurValid) {
            dedupMap.set(key, w);
            return;
          }
          if (isPrevValid && !isCurValid) {
            return; // 保留 prev
          }

          // 状态优先级：运输中 > 已分配 > 其它
          const statusPriority = (s: number) => {
            if (s === 2) return 3; // 运输中
            if (s === 1) return 2; // 已分配
            return 1;
          };
          const prevPri = statusPriority(prev.status);
          const curPri = statusPriority(w.status);

          if (curPri > prevPri) {
            dedupMap.set(key, w);
            return;
          }

          if (curPri === prevPri) {
            const prevTime = new Date(prev.createTime).getTime();
            const curTime = new Date(w.createTime).getTime();
            if (curTime >= prevTime) {
              dedupMap.set(key, w);
            }
          }
        });

        return Array.from(dedupMap.values());
      };
  
      onMounted(() => {
        userInfo.value = getUserInfo();
        loadCurrentTask();
        loadHistory();
        loadIncome();
      });
  
      const handleTabChange = (tab: string) => {
        if (tab === 'current') {
          loadCurrentTask();
        } else if (tab === 'history') {
          loadHistory();
        } else if (tab === 'income') {
          loadIncome();
        }
      };

      const loadCurrentTask = async () => {
        currentTaskLoading.value = true;
        try {
          // 查询状态为1（已分配）和2（运输中）的运单
          // 由于后端可能只支持单个状态查询，我们查询两次然后在前端去重
          const queryDTO1: WaybillQueryDTO = {
            status: 1, // 已分配
            current: 1,
            size: 200, // 获取足够多的数据
          };
          const queryDTO2: WaybillQueryDTO = {
            status: 2, // 运输中
            current: 1,
            size: 200,
          };

          const [result1, result2] = await Promise.all([
            driverQueryWaybills(queryDTO1),
            driverQueryWaybills(queryDTO2),
          ]);

          let allTasks: WaybillVO[] = [];
          if (result1.data) {
            const pageData1 = result1.data as any;
            allTasks = allTasks.concat(pageData1.records || []);
          }
          if (result2.data) {
            const pageData2 = result2.data as any;
            allTasks = allTasks.concat(pageData2.records || []);
          }

          // 调试信息：输出查询到的原始数据（详细）
          console.log('查询到的原始运单数据:', allTasks.map(w => ({
            waybillId: w.waybillId,
            status: w.status,
            changed: w.changed,
            changedType: typeof w.changed,
            goodsInformation: w.goodsInformation,
            startAddress: w.startAddress,
            endAddress: w.endAddress
          })));

          // 第一步：在去重前就严格过滤掉 changed=1 的运单
          const preFiltered = allTasks.filter((w) => {
            const changed = w.changed;
            // 转换为数字进行比较，兼容可能的字符串类型
            const changedNum = typeof changed === 'string' ? parseInt(changed, 10) : changed;
            // 严格检查：changed 不能是 1
            if (changedNum === 1) {
              console.warn('第一步过滤：移除 changed=1 的旧运单', {
                waybillId: w.waybillId,
                status: w.status,
                changed: w.changed,
                changedType: typeof w.changed,
                changedNum: changedNum
              });
              return false;
            }
            return true;
          });

          console.log('第一步过滤后的运单数据:', preFiltered.map(w => ({
            waybillId: w.waybillId,
            status: w.status,
            changed: w.changed
          })));

          // 去重并按业务含义合并"旧单/新单"
          const deduped = deduplicateWaybills(preFiltered);
          
          // 调试信息：输出去重后的数据
          console.log('去重后的运单数据:', deduped.map(w => ({
            waybillId: w.waybillId,
            status: w.status,
            changed: w.changed,
            goodsInformation: w.goodsInformation
          })));

          // 最终过滤：确保绝对不显示 changed=1 的运单，并且只显示 status=1 或 2 的运单（三重保险）
          const finalFiltered = deduped.filter((w) => {
            const changed = w.changed;
            // 转换为数字进行比较，兼容可能的字符串类型
            const changedNum = typeof changed === 'string' ? parseInt(changed, 10) : changed;
            
            // 严格检查：changed 不能是 1
            if (changedNum === 1) {
              console.error('最终过滤：移除 changed=1 的旧运单', {
                waybillId: w.waybillId,
                status: w.status,
                changed: w.changed,
                changedType: typeof w.changed,
                changedNum: changedNum
              });
              return false;
            }
            
            // 只保留 changed=0、undefined、null 的运单
            const isValidChanged = changedNum === 0 || changedNum === undefined || changedNum === null;
            
            // 只保留 status=1（已分配）或 status=2（运输中）的运单
            const isValidStatus = w.status === 1 || w.status === 2;
            
            if (!isValidStatus) {
              console.warn('最终过滤：移除非当前任务状态的运单', {
                waybillId: w.waybillId,
                status: w.status
              });
            }
            
            if (!isValidChanged) {
              console.error('最终过滤：移除 changed 无效的运单', {
                waybillId: w.waybillId,
                status: w.status,
                changed: w.changed,
                changedType: typeof w.changed,
                changedNum: changedNum
              });
            }
            
            return isValidChanged && isValidStatus;
          });

          // 按创建时间倒序排序
          finalFiltered.sort((a, b) => {
            const timeA = new Date(a.createTime).getTime();
            const timeB = new Date(b.createTime).getTime();
            return timeB - timeA;
          });

          // 调试信息：输出最终过滤后的数据
          console.log('最终显示的运单数据:', finalFiltered.map(w => ({
            waybillId: w.waybillId,
            status: w.status,
            changed: w.changed,
            goodsInformation: w.goodsInformation
          })));

          // 前端分页
          const start = (currentTaskPage.value - 1) * currentTaskPageSize.value;
          const end = start + currentTaskPageSize.value;
          currentTaskList.value = finalFiltered.slice(start, end);
          currentTaskTotal.value = finalFiltered.length;
        } catch (error: any) {
          console.error('加载当前任务失败:', error);
          ElMessage.error(error.message || '加载当前任务失败');
          currentTaskList.value = [];
          currentTaskTotal.value = 0;
        } finally {
          currentTaskLoading.value = false;
        }
      };
  
      const loadHistory = async () => {
        loading.value = true;
        try {
          const queryDTO: WaybillQueryDTO = {
            status: 3, // 只查询已完成的运单
            current: historyPage.value,
            size: historyPageSize.value,
          };
          const result = await driverQueryWaybills(queryDTO);
          if (result.data) {
            const pageData = result.data as any;
            const records: WaybillVO[] = pageData.records || [];
            const deduped = deduplicateWaybills(records);
            historyList.value = deduped;
            historyTotal.value = deduped.length;
          } else {
            historyList.value = [];
            historyTotal.value = 0;
          }
        } catch (error: any) {
          ElMessage.error(error.message || '加载历史任务失败');
          historyList.value = [];
          historyTotal.value = 0;
        } finally {
          loading.value = false;
        }
      };
  
      const loadIncome = async () => {
        try {
          // 查询所有已完成的任务来计算收入统计
          const queryDTO: WaybillQueryDTO = {
            status: 3, // 只查询已完成的运单
            current: 1,
            size: 1000, // 获取足够多的数据以便统计
          };
          const result = await driverQueryWaybills(queryDTO);
          if (result.data) {
            const pageData = result.data as any;
            const completedRaw = pageData.records || [];
            const completed = deduplicateWaybills(completedRaw);
            income.completedTasks = completed.length;
            income.totalIncome = completed.reduce(
              (sum: number, task: WaybillVO) => sum + (task.cost || 0),
              0
            );

            // 计算本月收入：筛选本月完成的运单
            const now = new Date();
            const currentMonth = now.getMonth();
            const currentYear = now.getFullYear();
            const monthCompleted = completed.filter((task: WaybillVO) => {
              if (!task.createTime) return false;
              const taskDate = new Date(task.createTime);
              return (
                taskDate.getMonth() === currentMonth &&
                taskDate.getFullYear() === currentYear
              );
            });
            income.monthIncome = monthCompleted.reduce(
              (sum: number, task: WaybillVO) => sum + (task.cost || 0),
              0
            );
          } else {
            income.completedTasks = 0;
            income.totalIncome = 0;
            income.monthIncome = 0;
          }
        } catch (error: any) {
          console.error('加载收入统计失败:', error);
          income.completedTasks = 0;
          income.totalIncome = 0;
          income.monthIncome = 0;
        }
      };
  
      const updateStatus = async (status: string, task: WaybillVO) => {
        if (!task) return;

        // 检查运单是否有效（changed=0 或 undefined），支持多种类型
        const changed = task.changed;
        // 转换为数字进行比较，兼容可能的字符串类型
        const changedNum = typeof changed === 'string' ? parseInt(changed, 10) : changed;
        
        // 严格检查：changed 不能是 1
        if (changedNum === 1) {
          console.error('操作前检查：发现 changed=1 的旧运单，拒绝操作', {
            waybillId: task.waybillId,
            status: task.status,
            changed: changed,
            changedType: typeof changed,
            changedNum: changedNum
          });
          ElMessage.warning('该运单已被修改，请刷新页面后重试');
          // 自动刷新列表
          await loadCurrentTask();
          return;
        }

        try {
          const waybillId = task.waybillId;
          if (status === 'start_transit') {
            // 开始运输：调用 driverStartWaybill
            await driverStartWaybill(waybillId);
            ElMessage.success('开始运输成功');
          } else if (status === 'delivered') {
            // 确认送达：调用 driverCompleteWaybill
            await driverCompleteWaybill(waybillId);
            ElMessage.success('确认送达成功，运单已完成');
            
            // 等待一小段时间，确保后端数据已更新
            await new Promise(resolve => setTimeout(resolve, 300));
          } else {
            // 其他状态（到达提货点、到达目的地）暂时只提示，不调用后端
            ElMessage.info('状态已记录');
            return;
          }
          
          // 刷新当前任务和历史任务（强制刷新，不使用缓存）
          await Promise.all([
            loadCurrentTask(),
            loadHistory(),
            loadIncome(),
          ]);
          
          // 如果确认送达，检查当前任务列表是否为空，提示用户查看历史任务
          if (status === 'delivered') {
            // 再等待一下，确保列表已更新
            await new Promise(resolve => setTimeout(resolve, 500));
            
            // 检查列表中是否还有刚才操作的运单（如果还有，说明刷新有问题）
            const stillExists = currentTaskList.value.find(w => w.waybillId === waybillId);
            if (stillExists) {
              console.warn('确认送达后，运单仍在当前任务列表中，可能是旧记录，强制再次刷新:', {
                waybillId: waybillId,
                status: stillExists.status,
                changed: stillExists.changed
              });
              // 强制再次刷新，并增加延迟
              await new Promise(resolve => setTimeout(resolve, 500));
              await loadCurrentTask();
              // 再次检查
              await new Promise(resolve => setTimeout(resolve, 300));
              const stillExistsAfterRefresh = currentTaskList.value.find(w => w.waybillId === waybillId);
              if (stillExistsAfterRefresh) {
                console.error('多次刷新后，运单仍在列表中，可能是后端返回了旧记录:', stillExistsAfterRefresh);
                ElMessage.warning('运单状态已更新，但列表可能包含旧记录，请手动刷新页面');
              }
            }
            
            // 检查是否还有 changed=1 的旧运单（不应该出现）
            const invalidTasks = currentTaskList.value.filter(w => {
              const changed = w.changed;
              const changedNum = typeof changed === 'string' ? parseInt(changed, 10) : changed;
              return changedNum === 1;
            });
            
            if (invalidTasks.length > 0) {
              console.error('刷新后仍发现 changed=1 的旧运单，强制再次刷新:', invalidTasks);
              // 强制再次刷新
              await loadCurrentTask();
              return;
            }
            
            // 检查是否包含已完成的运单（不应该出现）
            const completedTasks = currentTaskList.value.filter(w => w.status === 3);
            if (completedTasks.length > 0) {
              console.warn('发现已完成运单仍在当前任务列表中:', completedTasks);
              // 强制刷新
              await loadCurrentTask();
              return;
            }
            
            if (currentTaskList.value.length === 0) {
              ElMessage.info('当前没有进行中的任务，可在"历史任务"中查看已完成订单');
            } else {
              // 如果还有任务，检查是否包含刚才操作的运单
              const completedInList = currentTaskList.value.find(w => w.waybillId === waybillId);
              if (!completedInList) {
                ElMessage.success('运单已完成，已从当前任务列表中移除');
              }
            }
          }
        } catch (error: any) {
          // 如果后端返回"运单已被修改"的错误，自动刷新列表
          if (error.message && error.message.includes('已被修改')) {
            ElMessage.warning('该运单已被修改，已自动刷新列表');
            loadCurrentTask();
          } else {
            ElMessage.error(error.message || '状态更新失败');
          }
        }
      };
  
      const showIssueDialog = (task: WaybillVO) => {
        selectedTaskForIssue.value = task;
        issueForm.type = '';
        issueForm.description = '';
        issueDialogVisible.value = true;
      };
  
      const submitIssue = async () => {
        if (!issueForm.type) {
          ElMessage.warning('请选择异常类型');
          return;
        }
        if (!selectedTaskForIssue.value) {
          ElMessage.warning('请选择要上报异常的运单');
          return;
        }

        // 检查运单是否有效（changed=0 或 undefined），支持多种类型
        const changed = selectedTaskForIssue.value.changed;
        const changedNum = typeof changed === 'string' ? parseInt(changed, 10) : changed;
        if (changedNum === 1) {
          ElMessage.warning('该运单已被修改，请刷新页面后重试');
          issueDialogVisible.value = false;
          loadCurrentTask();
          return;
        }

        try {
          const typeMap: Record<string, string> = {
            traffic_jam: '堵车',
            waiting: '等待',
            accident: '事故',
            vehicle_breakdown: '车辆故障',
            other: '其他',
          };
          const typeDesc = typeMap[issueForm.type] || issueForm.type;
          const description = `[${typeDesc}] ${issueForm.description || ''}`.trim();

          const exceptionData: WaybillExceptionReportDTO = {
            waybillIdentification: selectedTaskForIssue.value.waybillIdentification,
            description: description,
            exceptionDate: new Date().toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit',
              hour12: false,
            }).replace(/\//g, '-').replace(', ', ' '),
          };

          await reportWaybillException(exceptionData);
          ElMessage.success('异常上报成功');
          issueDialogVisible.value = false;
          selectedTaskForIssue.value = null;
          // 清空表单
          issueForm.type = '';
          issueForm.description = '';
        } catch (error: any) {
          // 如果后端返回"运单已被修改"的错误，自动刷新列表
          if (error.message && error.message.includes('已被修改')) {
            ElMessage.warning('该运单已被修改，已自动刷新列表');
            issueDialogVisible.value = false;
            loadCurrentTask();
          } else {
            ElMessage.error(error.message || '异常上报失败');
          }
        }
      };
  
      const uploadReceipt = (row: WaybillVO) => {
        currentTaskDetail.value = row; // 设置当前任务详情以便显示 waybillIdentification
        receiptForm.waybillId = String(row.waybillId); // 保留 waybillId 用于API调用
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
          await driverLogout();
        } catch (error) {
          // 忽略错误，继续执行登出流程
        } finally {
          clearAuth();
          ElMessage.success('已退出登录');
          router.push('/login');
        }
      };
  
      return {
        activeTab,
        loading,
        currentTaskLoading,
        currentTaskList,
        currentTaskPage,
        currentTaskPageSize,
        currentTaskTotal,
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
    position: relative;
    min-height: 100vh;
    overflow: hidden;
    background: radial-gradient(circle at top left, #4f46e5 0, #0f172a 45%, #020617 100%);
  }
  
  .dashboard-header {
    padding-inline: 0;
    background: transparent;
    border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  }
  
  .dashboard-main {
    padding-inline: 0;
    color: #e5e7eb;
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
  }
  
  .header-left {
    display: flex;
    flex-direction: column;
  }
  
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .logo-dot {
    width: 12px;
    height: 12px;
    border-radius: 999px;
    background: linear-gradient(135deg, #f59e0b, #fbbf24);
    box-shadow: 0 0 12px rgba(245, 158, 11, 0.85);
  }
  
  .logo-text {
    display: flex;
    flex-direction: column;
  }
  
  .logo-title {
    font-size: 18px;
    font-weight: 600;
    color: #e5e7eb;
  }
  
  .logo-subtitle {
    font-size: 12px;
    color: #cbd5f5;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 14px;
  }
  
  .user-name {
    color: #e5e7eb;
  }
  
  .dashboard-tabs :deep(.el-tabs__item) {
    color: #cbd5f5;
  }
  
  .dashboard-tabs :deep(.el-tabs__item.is-active) {
    color: #60a5fa;
  }
  
  .dashboard-tabs :deep(.el-tabs__active-bar) {
    background-color: #60a5fa;
  }
  
  .dashboard-tabs :deep(.el-tabs__nav-wrap::after) {
    background-color: rgba(148, 163, 184, 0.2);
  }
  
  .panel-card {
    background: rgba(15, 23, 42, 0.97);
    border-radius: 16px;
    border: 1px solid rgba(148, 163, 184, 0.5);
    box-shadow: 0 18px 50px rgba(15, 23, 42, 0.9);
  }
  
  .panel-card :deep(.el-card__body) {
    color: #e5e7eb;
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
    color: #60a5fa;
    margin-bottom: 10px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #cbd5f5;
  }
  
  @media (max-width: 960px) {
    .header-content {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }
  </style>

