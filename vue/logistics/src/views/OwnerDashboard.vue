<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>货主工作台</h2>
          <div class="user-info">
            <span>{{ userInfo?.phone || '货主' }}</span>
            <el-button type="text" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 发布运单 -->
          <el-tab-pane label="发布运单" name="create">
            <el-card>
              <el-form :model="waybillForm" :rules="waybillRules" ref="waybillFormRef" label-width="120px">
                <el-form-item label="货物信息" prop="goodsInformation">
                  <el-input
                    v-model="waybillForm.goodsInformation"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入货物描述、重量、体积等信息"
                  />
                </el-form-item>
                <el-form-item label="发货地址" prop="startAddress">
                  <el-input v-model="waybillForm.startAddress" placeholder="请输入提货地址" />
                </el-form-item>
                <el-form-item label="收货地址" prop="endAddress">
                  <el-input v-model="waybillForm.endAddress" placeholder="请输入送货地址" />
                </el-form-item>
                <el-form-item label="期望时效" prop="expectedTimeLimit">
                  <el-date-picker
                    v-model="waybillForm.expectedTimeLimit"
                    type="datetime"
                    placeholder="选择期望送达时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="费用(元)" prop="cost">
                  <el-input-number
                    v-model="waybillForm.cost"
                    :min="0.01"
                    :precision="2"
                    placeholder="请输入运费"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="收货人ID" prop="receivingConsignorId">
                  <el-input-number
                    v-model="waybillForm.receivingConsignorId"
                    :min="1"
                    placeholder="请输入收货人ID"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleCreateWaybill" :loading="creating">
                    提交运单
                  </el-button>
                  <el-button @click="resetForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <!-- 运单追踪 -->
          <el-tab-pane label="运单追踪" name="track">
            <el-card>
              <div class="search-bar">
                <el-select v-model="queryStatus" placeholder="按状态筛选" clearable style="width: 150px">
                  <el-option label="待分配" :value="0" />
                  <el-option label="已分配" :value="1" />
                  <el-option label="运输中" :value="2" />
                  <el-option label="已完成" :value="3" />
                </el-select>
                <el-button type="primary" @click="loadWaybills" style="margin-left: 10px">查询</el-button>
              </div>
              <el-table :data="waybillList" style="width: 100%; margin-top: 20px" v-loading="loading">
                <el-table-column prop="waybillId" label="运单号" width="120" />
                <el-table-column prop="goodsInformation" label="货物信息" width="200" />
                <el-table-column prop="startAddress" label="发货地址" width="150" />
                <el-table-column prop="endAddress" label="收货地址" width="150" />
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
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button size="small" @click="viewWaybillDetail(scope.row)">详情</el-button>
                    <el-button
                      size="small"
                      type="danger"
                      @click="cancelWaybill(scope.row.waybillId)"
                      v-if="scope.row.status === 0"
                    >
                      取消
                    </el-button>
                    <el-button
                      size="small"
                      type="success"
                      @click="confirmReceive(scope.row)"
                      v-if="scope.row.status === 2"
                    >
                      确认签收
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadWaybills"
                @current-change="loadWaybills"
                style="margin-top: 20px"
              />
            </el-card>
          </el-tab-pane>

          <!-- 实时追踪 -->
          <el-tab-pane label="实时追踪" name="realtime">
            <el-card>
              <div class="map-container" id="mapContainer" style="height: 600px">
                <div style="text-align: center; padding-top: 250px; color: #999">
                  地图功能需要集成高德地图或百度地图API
                  <br />
                  请配置地图API密钥后使用
                </div>
              </div>
            </el-card>
          </el-tab-pane>

          <!-- 个人信息 -->
          <el-tab-pane label="个人信息" name="profile">
            <el-card>
              <el-form :model="profileForm" label-width="120px">
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone" disabled />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateProfile">保存</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <!-- 运单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="运单详情" width="800px">
      <el-descriptions :column="2" border v-if="currentWaybill">
        <el-descriptions-item label="运单号">{{ currentWaybill.waybillId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentWaybill.status)">
            {{ getStatusDesc(currentWaybill.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="货物信息" :span="2">
          {{ currentWaybill.goodsInformation }}
        </el-descriptions-item>
        <el-descriptions-item label="发货地址">{{ currentWaybill.startAddress }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ currentWaybill.endAddress }}</el-descriptions-item>
        <el-descriptions-item label="费用">{{ currentWaybill.cost?.toFixed(2) }} 元</el-descriptions-item>
        <el-descriptions-item label="期望时效">{{ currentWaybill.expectedTimeLimit }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentWaybill.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createWaybill,
  queryWaybillsByStatus,
  getWaybillDetail,
  cancelWaybill as cancelWaybillAPI,
  getConsignorInfo,
  WaybillCreateDTO,
  WaybillVO,
} from '../api';
import { clearAuth, getUserInfo } from '../utils/auth';
import { getStatusDesc, getStatusType } from '../utils/waybillStatus';

export default defineComponent({
  name: 'OwnerDashboard',
  setup() {
    const router = useRouter();
    const activeTab = ref('create');
    const loading = ref(false);
    const creating = ref(false);
    const waybillFormRef = ref();
    const waybillList = ref<WaybillVO[]>([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const queryStatus = ref<number | undefined>(undefined);
    const detailDialogVisible = ref(false);
    const currentWaybill = ref<WaybillVO | null>(null);
    const userInfo = ref<any>(null);
    const profileForm = reactive({
      phone: '',
      email: '',
    });

    const waybillForm = reactive<WaybillCreateDTO & { receivingConsignorId: number }>({
      receivingConsignorId: 0,
      goodsInformation: '',
      startAddress: '',
      endAddress: '',
      expectedTimeLimit: '',
      cost: 0,
    });

    const waybillRules = {
      goodsInformation: [{ required: true, message: '请输入货物信息', trigger: 'blur' }],
      startAddress: [{ required: true, message: '请输入发货地址', trigger: 'blur' }],
      endAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }],
      expectedTimeLimit: [{ required: true, message: '请选择期望时效', trigger: 'change' }],
      cost: [{ required: true, message: '请输入费用', trigger: 'blur' }],
      receivingConsignorId: [{ required: true, message: '请输入收货人ID', trigger: 'blur' }],
    };

    onMounted(() => {
      loadUserInfo();
      if (activeTab.value === 'track') {
        loadWaybills();
      }
    });

    const loadUserInfo = async () => {
      try {
        const info = getUserInfo();
        userInfo.value = info;
        const result = await getConsignorInfo();
        if (result.data) {
          const userData = result.data as any;
          profileForm.phone = userData.phone || '';
          profileForm.email = userData.email || '';
        }
      } catch (error) {
        console.error('加载用户信息失败:', error);
      }
    };

    const handleTabChange = (tab: string) => {
      if (tab === 'track') {
        loadWaybills();
      }
    };

    const handleCreateWaybill = async () => {
      if (!waybillFormRef.value) return;
      await waybillFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          creating.value = true;
          try {
            await createWaybill(waybillForm);
            ElMessage.success('运单创建成功');
            resetForm();
            activeTab.value = 'track';
            loadWaybills();
          } catch (error: any) {
            ElMessage.error(error.message || '创建运单失败');
          } finally {
            creating.value = false;
          }
        }
      });
    };

    const resetForm = () => {
      waybillForm.receivingConsignorId = 0;
      waybillForm.goodsInformation = '';
      waybillForm.startAddress = '';
      waybillForm.endAddress = '';
      waybillForm.expectedTimeLimit = '';
      waybillForm.cost = 0;
      waybillFormRef.value?.resetFields();
    };

    const loadWaybills = async () => {
      loading.value = true;
      try {
        const result = await queryWaybillsByStatus(queryStatus.value, currentPage.value, pageSize.value);
        if (result.data) {
          const pageData = result.data as any;
          waybillList.value = pageData.records || [];
          total.value = pageData.total || 0;
        }
      } catch (error: any) {
        ElMessage.error(error.message || '加载运单列表失败');
      } finally {
        loading.value = false;
      }
    };

    const viewWaybillDetail = async (row: WaybillVO) => {
      try {
        const result = await getWaybillDetail(row.waybillId);
        if (result.data) {
          currentWaybill.value = result.data as unknown as WaybillVO;
          detailDialogVisible.value = true;
        }
      } catch (error: any) {
        ElMessage.error(error.message || '获取运单详情失败');
      }
    };

    const cancelWaybill = async (waybillId: number) => {
      try {
        await ElMessageBox.confirm('确定要取消该运单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });
        await cancelWaybillAPI(waybillId);
        ElMessage.success('运单已取消');
        loadWaybills();
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '取消运单失败');
        }
      }
    };

    const confirmReceive = async (row: WaybillVO) => {
      try {
        await ElMessageBox.confirm('确认已收到货物？', '确认签收', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'success',
        });
        ElMessage.success('签收成功');
        loadWaybills();
      } catch (error) {
        // 用户取消
      }
    };

    const updateProfile = () => {
      ElMessage.info('个人信息更新功能待实现');
    };

    const handleLogout = async () => {
      try {
        clearAuth();
        ElMessage.success('已退出登录');
        router.push('/login');
      } catch (error) {
        router.push('/login');
      }
    };

    return {
      activeTab,
      loading,
      creating,
      waybillForm,
      waybillRules,
      waybillFormRef,
      waybillList,
      currentPage,
      pageSize,
      total,
      queryStatus,
      detailDialogVisible,
      currentWaybill,
      userInfo,
      profileForm,
      handleTabChange,
      handleCreateWaybill,
      resetForm,
      loadWaybills,
      viewWaybillDetail,
      cancelWaybill,
      confirmReceive,
      updateProfile,
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

.search-bar {
  margin-bottom: 20px;
}

.map-container {
  background-color: #f0f0f0;
  border-radius: 4px;
}
</style>
