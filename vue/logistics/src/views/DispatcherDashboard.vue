<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>调度员工作台</h2>
          <div class="user-info">
            <span>{{ userInfo?.username || '调度员' }}</span>
            <el-button type="text" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 运单管理 -->
          <el-tab-pane label="运单管理" name="waybills">
            <el-card>
              <div class="search-bar">
                <el-select v-model="waybillQuery.status" placeholder="按状态筛选" clearable style="width: 150px">
                  <el-option label="待分配" :value="0" />
                  <el-option label="已分配" :value="1" />
                  <el-option label="运输中" :value="2" />
                  <el-option label="已完成" :value="3" />
                </el-select>
                <el-input
                  v-model="waybillQuery.keyword"
                  placeholder="搜索运单号、地址"
                  style="width: 200px; margin-left: 10px"
                  clearable
                />
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
                <el-table-column label="操作" width="250" fixed="right">
                  <template #default="scope">
                    <el-button size="small" @click="viewWaybillDetail(scope.row)">详情</el-button>
                    <el-button
                      size="small"
                      type="primary"
                      @click="showAssignDialog(scope.row)"
                      v-if="scope.row.status === 0"
                    >
                      派单
                    </el-button>
                    <el-button
                      size="small"
                      type="success"
                      @click="autoAssign(scope.row)"
                      v-if="scope.row.status === 0"
                    >
                      智能调度
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-model:current-page="waybillPage"
                v-model:page-size="waybillPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="waybillTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadWaybills"
                @current-change="loadWaybills"
                style="margin-top: 20px"
              />
            </el-card>
          </el-tab-pane>

          <!-- 车辆管理 -->
          <el-tab-pane label="车辆管理" name="cars">
        <el-card>
              <div class="search-bar">
                <el-button type="primary" @click="showCarDialog">新增车辆</el-button>
                <el-select
                  v-model="carQuery.status"
                  placeholder="按状态筛选"
                  clearable
                  style="width: 150px; margin-left: 10px"
                >
                  <el-option label="可用" :value="0" />
                  <el-option label="已分配" :value="1" />
                  <el-option label="维修中" :value="2" />
                </el-select>
                <el-button type="primary" @click="loadCars" style="margin-left: 10px">查询</el-button>
            </div>
              <el-table :data="carList" style="width: 100%; margin-top: 20px" v-loading="carLoading">
                <el-table-column prop="carId" label="车辆ID" width="100" />
                <el-table-column prop="licensePlate" label="车牌号" width="120" />
                <el-table-column prop="carType" label="车型" width="120" />
                <el-table-column prop="loadCapacity" label="载重(吨)" width="100" />
                <el-table-column prop="driverName" label="司机" width="120" />
                <el-table-column prop="location" label="位置" width="150" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getCarStatusType(scope.row.status)">
                      {{ getCarStatusDesc(scope.row.status) }}
                    </el-tag>
          </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                    <el-button size="small" @click="editCar(scope.row)">编辑</el-button>
                    <el-button size="small" type="danger" @click="deleteCar(scope.row.carId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
              <el-pagination
                v-model:current-page="carPage"
                v-model:page-size="carPageSize"
                :page-sizes="[10, 20, 50]"
                :total="carTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadCars"
                @current-change="loadCars"
                style="margin-top: 20px"
              />
            </el-card>
          </el-tab-pane>

          <!-- 在途监控 -->
          <el-tab-pane label="在途监控" name="monitor">
            <el-card>
              <div class="map-container" id="monitorMapContainer" style="height: 600px">
                <div style="text-align: center; padding-top: 250px; color: #999">
                  地图功能需要集成高德地图或百度地图API
                  <br />
                  请配置地图API密钥后使用
                </div>
              </div>
            </el-card>
          </el-tab-pane>

          <!-- 数据统计 -->
          <el-tab-pane label="数据统计" name="statistics">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ statistics.totalWaybills }}</div>
                    <div class="stat-label">总运单数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ statistics.completedWaybills }}</div>
                    <div class="stat-label">已完成运单</div>
                  </div>
        </el-card>
      </el-col>
              <el-col :span="6">
        <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ statistics.onTimeRate }}%</div>
                    <div class="stat-label">准时率</div>
            </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ statistics.totalCars }}</div>
                    <div class="stat-label">车辆总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <!-- 派单对话框 -->
    <el-dialog v-model="assignDialogVisible" title="派单" width="600px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="运单号">
          <el-input v-model="assignForm.waybillId" disabled />
        </el-form-item>
        <el-form-item label="选择车辆" required>
          <el-select v-model="assignForm.carId" placeholder="请选择车辆" style="width: 100%">
            <el-option
              v-for="car in availableCars"
              :key="car.carId"
              :label="`${car.licensePlate} - ${car.driverName || '无司机'}`"
              :value="car.carId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="confirmAssign">确认派单</el-button>
          <el-button @click="assignDialogVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 车辆编辑对话框 -->
    <el-dialog v-model="carDialogVisible" :title="carDialogTitle" width="600px">
      <el-form :model="carForm" :rules="carRules" ref="carFormRef" label-width="100px">
        <el-form-item label="车牌号" prop="licensePlate">
          <el-input v-model="carForm.licensePlate" />
        </el-form-item>
        <el-form-item label="车型" prop="carType">
          <el-input v-model="carForm.carType" />
        </el-form-item>
        <el-form-item label="载重(吨)" prop="loadCapacity">
          <el-input-number v-model="carForm.loadCapacity" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="司机ID" prop="driverId">
          <el-input-number v-model="carForm.driverId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="carForm.location" placeholder="格式: lat,lng 或 地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveCar">保存</el-button>
          <el-button @click="carDialogVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

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
  dispatcherQueryWaybills,
  dispatcherGetWaybill,
  dispatcherLogout,
  queryCars,
  createCar,
  updateCar,
  deleteCar as deleteCarAPI,
  assignVehicle,
  autoAssignVehicle,
  getDispatcherInfo,
  DispatcherWaybillQueryDTO,
  CarCreateDTO,
  CarUpdateDTO,
  CarQueryDTO,
  WaybillVO,
  CarVO,
} from '../api';
import { clearAuth, getUserInfo } from '../utils/auth';
import { getStatusDesc, getStatusType, getCarStatusDesc, getCarStatusType } from '../utils/waybillStatus';

export default defineComponent({
  name: 'DispatcherDashboard',
  setup() {
    const router = useRouter();
    const activeTab = ref('waybills');
    const loading = ref(false);
    const carLoading = ref(false);
    const waybillList = ref<WaybillVO[]>([]);
    const carList = ref<CarVO[]>([]);
    const availableCars = ref<CarVO[]>([]);
    const waybillPage = ref(1);
    const waybillPageSize = ref(10);
    const waybillTotal = ref(0);
    const carPage = ref(1);
    const carPageSize = ref(10);
    const carTotal = ref(0);
    const userInfo = ref<any>(null);
    const assignDialogVisible = ref(false);
    const carDialogVisible = ref(false);
    const carDialogTitle = ref('新增车辆');
    const detailDialogVisible = ref(false);
    const currentWaybill = ref<WaybillVO | null>(null);
    const carFormRef = ref();

    const waybillQuery = reactive<DispatcherWaybillQueryDTO>({
      status: undefined,
      keyword: '',
      current: 1,
      size: 10,
    });

    const carQuery = reactive<CarQueryDTO>({
      status: undefined,
      current: 1,
      size: 10,
    });

    const assignForm = reactive({
      waybillId: 0,
      carId: 0,
    });

    const carForm = reactive<CarCreateDTO & { carId?: number }>({
      driverId: 0,
      licensePlate: '',
      carType: '',
      loadCapacity: 0,
      location: '',
      status: 0,
    });

    const carRules = {
      licensePlate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
      carType: [{ required: true, message: '请输入车型', trigger: 'blur' }],
      loadCapacity: [{ required: true, message: '请输入载重', trigger: 'blur' }],
      driverId: [{ required: true, message: '请输入司机ID', trigger: 'blur' }],
    };

    const statistics = reactive({
      totalWaybills: 0,
      completedWaybills: 0,
      onTimeRate: 0,
      totalCars: 0,
    });

    onMounted(() => {
      loadUserInfo();
      loadWaybills();
      loadCars();
      loadStatistics();
    });

    const loadUserInfo = async () => {
      try {
        const info = getUserInfo();
        userInfo.value = info;
        const result = await getDispatcherInfo();
        if (result.data) {
          userInfo.value = { ...userInfo.value, ...result.data };
        }
      } catch (error) {
        console.error('加载用户信息失败:', error);
      }
    };

    const handleTabChange = (tab: string) => {
      if (tab === 'waybills') {
        loadWaybills();
      } else if (tab === 'cars') {
        loadCars();
      } else if (tab === 'statistics') {
        loadStatistics();
      }
    };

    const loadWaybills = async () => {
      loading.value = true;
      try {
        waybillQuery.current = waybillPage.value;
        waybillQuery.size = waybillPageSize.value;
        const result = await dispatcherQueryWaybills(waybillQuery);
        if (result.data) {
          const pageData = result.data as any;
          waybillList.value = pageData.records || [];
          waybillTotal.value = pageData.total || 0;
        }
      } catch (error: any) {
        ElMessage.error(error.message || '加载运单列表失败');
      } finally {
        loading.value = false;
      }
    };

    const loadCars = async () => {
      carLoading.value = true;
      try {
        carQuery.current = carPage.value;
        carQuery.size = carPageSize.value;
        const result = await queryCars(carQuery);
        if (result.data) {
          const pageData = result.data as any;
          carList.value = pageData.records || [];
          carTotal.value = pageData.total || 0;
          // 获取可用车辆用于派单
          availableCars.value = carList.value.filter((car) => car.status === 0);
        }
      } catch (error: any) {
        ElMessage.error(error.message || '加载车辆列表失败');
      } finally {
        carLoading.value = false;
      }
    };

    const loadStatistics = async () => {
      // 模拟统计数据
      statistics.totalWaybills = waybillTotal.value;
      statistics.completedWaybills = waybillList.value.filter((w) => w.status === 3).length;
      statistics.onTimeRate = 95;
      statistics.totalCars = carTotal.value;
    };

    const viewWaybillDetail = async (row: WaybillVO) => {
      try {
        const result = await dispatcherGetWaybill(row.waybillId);
        if (result.data) {
          currentWaybill.value = result.data as unknown as WaybillVO;
          detailDialogVisible.value = true;
        }
      } catch (error: any) {
        ElMessage.error(error.message || '获取运单详情失败');
      }
    };

    const showAssignDialog = (row: WaybillVO) => {
      assignForm.waybillId = row.waybillId;
      assignForm.carId = 0;
      // 重新加载可用车辆
      loadCars();
      assignDialogVisible.value = true;
    };

    const confirmAssign = async () => {
      if (!assignForm.carId) {
        ElMessage.warning('请选择车辆');
        return;
      }
      try {
        await assignVehicle(assignForm.waybillId, assignForm.carId);
        ElMessage.success('派单成功');
        assignDialogVisible.value = false;
        loadWaybills();
        loadCars();
      } catch (error: any) {
        ElMessage.error(error.message || '派单失败');
      }
    };

    const autoAssign = async (row: WaybillVO) => {
      try {
        await ElMessageBox.confirm('系统将自动匹配最优车辆，是否继续？', '智能调度', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        });
        await autoAssignVehicle(row.waybillId);
        ElMessage.success('智能调度成功');
        loadWaybills();
        loadCars();
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '智能调度失败');
        }
      }
    };

    const showCarDialog = () => {
      carDialogTitle.value = '新增车辆';
      Object.keys(carForm).forEach((key) => {
        if (key === 'carId') {
          delete (carForm as any)[key];
        } else if (key === 'status') {
          (carForm as any)[key] = 0;
        } else {
          (carForm as any)[key] = '';
        }
      });
      carDialogVisible.value = true;
    };

    const editCar = (row: CarVO) => {
      carDialogTitle.value = '编辑车辆';
      carForm.carId = row.carId;
      carForm.licensePlate = row.licensePlate;
      carForm.carType = row.carType;
      carForm.loadCapacity = row.loadCapacity;
      carForm.driverId = row.driverId;
      carForm.location = row.location;
      carForm.status = row.status;
      carDialogVisible.value = true;
    };

    const saveCar = async () => {
      if (!carFormRef.value) return;
      await carFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          try {
            if (carForm.carId) {
              const updateData: CarUpdateDTO = {
                carId: carForm.carId,
                licensePlate: carForm.licensePlate,
                carType: carForm.carType,
                loadCapacity: carForm.loadCapacity,
                driverId: carForm.driverId,
                location: carForm.location,
                status: carForm.status,
              };
              await updateCar(carForm.carId, updateData);
              ElMessage.success('更新成功');
            } else {
              const createData: CarCreateDTO = {
                driverId: carForm.driverId,
                licensePlate: carForm.licensePlate,
                carType: carForm.carType,
                loadCapacity: carForm.loadCapacity,
                location: carForm.location,
                status: carForm.status,
              };
              await createCar(createData);
              ElMessage.success('创建成功');
            }
            carDialogVisible.value = false;
            loadCars();
          } catch (error: any) {
            ElMessage.error(error.message || '保存失败');
          }
        }
      });
    };

    const deleteCar = async (carId: number) => {
      try {
        await ElMessageBox.confirm('确定要删除该车辆吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });
        await deleteCarAPI(carId);
        ElMessage.success('删除成功');
        loadCars();
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '删除失败');
        }
      }
    };

    const handleLogout = async () => {
      try {
        await dispatcherLogout();
      } catch (error) {
        // 忽略错误
      } finally {
        clearAuth();
        ElMessage.success('已退出登录');
        router.push('/login');
      }
    };

    return {
      activeTab,
      loading,
      carLoading,
      waybillList,
      carList,
      availableCars,
      waybillPage,
      waybillPageSize,
      waybillTotal,
      carPage,
      carPageSize,
      carTotal,
      userInfo,
      waybillQuery,
      carQuery,
      assignDialogVisible,
      assignForm,
      carDialogVisible,
      carDialogTitle,
      carForm,
      carRules,
      carFormRef,
      detailDialogVisible,
      currentWaybill,
      statistics,
      handleTabChange,
      loadWaybills,
      loadCars,
      loadStatistics,
      viewWaybillDetail,
      showAssignDialog,
      confirmAssign,
      autoAssign,
      showCarDialog,
      editCar,
      saveCar,
      deleteCar,
      handleLogout,
      getStatusDesc,
      getStatusType,
      getCarStatusDesc,
      getCarStatusType,
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
