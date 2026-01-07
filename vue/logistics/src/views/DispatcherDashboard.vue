<template>
  <div class="dashboard">
    <el-container>
      <el-header class="dashboard-header">
        <div class="header-content">
          <div class="header-left">
            <div class="logo">
              <span class="logo-dot" />
              <div class="logo-text">
                <div class="logo-title">调度员工作台</div>
                <div class="logo-subtitle">运单调度与车辆管理</div>
              </div>
            </div>
          </div>
          <div class="user-info">
            <span class="user-name">{{ userInfo?.username || '调度员' }}</span>
            <el-button link @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main class="dashboard-main">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="dashboard-tabs">
          <!-- 运单管理 -->
          <el-tab-pane label="运单管理" name="waybills">
            <el-card class="panel-card">
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
                <el-table-column prop="waybillIdentification" label="运单号" width="120" />
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
            <el-card class="panel-card">
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
                <el-table-column label="司机" width="200">
                  <template #default="scope">
                    <div class="driver-cell">
                      <span v-if="scope.row.driverId">
                        司机ID：{{ scope.row.driverId }}
                      </span>
                      <span v-if="scope.row.driverId" class="driver-id">
                        姓名：{{ scope.row.driverName || '暂无姓名（仅绑定了ID）' }}
                      </span>
                      <span v-else>未分配</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="位置" width="200">
                  <template #default="scope">
                    <div v-if="scope.row.location">
                      <div v-if="scope.row.locationName" style="color: #67c23a;">
                        {{ scope.row.locationName }}
                      </div>
                      <div v-else-if="scope.row.location.match(/^[\d.]+,\s*[\d.]+$/)" style="color: #909399; font-size: 12px;">
                        <el-button 
                          type="text" 
                          size="small" 
                          @click="reverseGeocodeCarLocationInList(scope.row)"
                        >
                          查询地址
                        </el-button>
                      </div>
                      <div v-else>
                        {{ scope.row.location }}
                      </div>
                    </div>
                    <span v-else style="color: #909399;">未设置</span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getCarStatusType(scope.row.status)">
                      {{ getCarStatusDesc(scope.row.status) }}
                    </el-tag>
          </template>
                </el-table-column>
                <el-table-column label="操作" width="260" fixed="right">
                  <template #default="scope">
                    <el-button size="small" @click="editCar(scope.row)">编辑</el-button>
                    <el-button size="small" type="danger" @click="deleteCar(scope.row.carId)">删除</el-button>
                    <el-button
                      v-if="scope.row.status === 1"
                      size="small"
                      type="warning"
                      @click="releaseCar(scope.row.carId)"
                    >
                      释放车辆
                    </el-button>
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
            <el-card class="panel-card">
              <div class="map-controls" style="margin-bottom: 15px">
                <el-select v-model="selectedCarId" placeholder="选择车辆" clearable style="width: 200px" @change="onCarSelectChange">
                  <el-option
                    v-for="car in carList"
                    :key="car.carId"
                    :label="`车辆${car.carId}${car.driverName ? ' - ' + car.driverName : ''}`"
                    :value="car.carId"
                  />
                </el-select>
                <el-button type="primary" @click="refreshMap" style="margin-left: 10px">刷新位置</el-button>
                <el-button @click="showAllCars" style="margin-left: 10px">显示全部车辆</el-button>
              </div>
              <div class="map-container" id="monitorMapContainer" style="height: 600px"></div>
            </el-card>
          </el-tab-pane>

          <!-- 数据统计 -->
          <el-tab-pane label="数据统计" name="statistics">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="panel-card">
                  <div class="stat-item">
                    <div class="stat-value">{{ statistics.totalWaybills }}</div>
                    <div class="stat-label">总运单数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="panel-card">
                  <div class="stat-item">
                    <div class="stat-value">{{ statistics.completedWaybills }}</div>
                    <div class="stat-label">已完成运单</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="panel-card">
                  <div class="stat-item">
                    <div class="stat-value">{{ statistics.onTimeRate }}%</div>
                    <div class="stat-label">准时率</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="panel-card">
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
          <el-input :value="currentWaybill?.waybillIdentification || assignForm.waybillId" disabled />
        </el-form-item>
        <el-form-item label="选择车辆" required>
          <el-select v-model="assignForm.carId" placeholder="请选择车辆" style="width: 100%">
            <el-option
              v-for="car in availableCars"
              :key="car.carId"
              :label="`车辆${car.carId}${car.licensePlate ? ' / ' + car.licensePlate : ''}${car.driverName ? ' / 司机：' + car.driverName : ''}`"
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
    <el-dialog v-model="carDialogVisible" :title="carDialogTitle" width="800px">
      <el-form :model="carForm" :rules="carRules" ref="carFormRef" label-width="100px">
        <el-form-item label="司机" prop="driverId">
          <div class="driver-edit">
            <el-input-number
              v-model="carForm.driverId"
              :min="1"
              :controls="false"
              style="width: 100%"
              placeholder="填写要绑定的司机ID，可留空"
            />
            <div class="driver-edit-tip">
              <span v-if="carForm.carId">
                当前司机：
                <strong>{{ carForm.driverName || '未绑定' }}</strong>
                <span v-if="carForm.driverId">（ID: {{ carForm.driverId }}）</span>
              </span>
              <span v-else>新增车辆时可暂不绑定司机，后续在此处填写司机ID。</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="位置">
          <div style="display: flex; flex-direction: column; gap: 10px;">
            <el-input 
              v-model="carForm.location" 
              placeholder="格式: lat,lng 或 地址，也可点击地图选择位置"
              readonly
            />
            <div style="display: flex; gap: 10px;">
              <el-button type="primary" size="small" @click="initCarLocationMap">在地图上选择位置</el-button>
              <el-button size="small" @click="clearCarLocation">清除位置</el-button>
            </div>
            <div v-if="carLocationMapVisible" id="carLocationMapContainer" style="height: 400px; width: 100%; border-radius: 8px; overflow: hidden;"></div>
            <div v-if="carForm.location && carForm.location.match(/^[\d.]+,\s*[\d.]+$/)">
              <el-button type="success" size="small" @click="reverseGeocodeCarLocation">查询位置名称</el-button>
              <span v-if="carLocationName" style="margin-left: 10px; color: #67c23a;">{{ carLocationName }}</span>
            </div>
          </div>
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
        <el-descriptions-item label="运单号">{{ currentWaybill.waybillIdentification }}</el-descriptions-item>
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
  updateCarStatus,
  WaybillVO,
  CarVO,
  getCarLocation,
} from '../api';
import { clearAuth, getUserInfo } from '../utils/auth';
import { getStatusDesc, getStatusType, getCarStatusDesc, getCarStatusType } from '../utils/waybillStatus';
import { AMAP_API_KEY, DEFAULT_MAP_CENTER, DEFAULT_MAP_ZOOM } from '../config/map';

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

    const assignForm = reactive<{
      waybillId: string;
      carId: number | null;
    }>({
      waybillId: '',
      carId: null,
    });

    const carForm = reactive<Partial<CarCreateDTO> & { carId?: number; driverName?: string }>({
      driverId: 0,
      location: '',
      status: 0,
      driverName: '',
    });

    const carRules = {
      driverId: [{ required: true, message: '请输入司机ID', trigger: 'blur' }],
    };

    const statistics = reactive({
      totalWaybills: 0,
      completedWaybills: 0,
      onTimeRate: 0,
      totalCars: 0,
    });

    // 地图相关
    const selectedCarId = ref<number | null>(null);
    let mapInstance: any = null;
    let markers: any[] = [];
    // 车辆位置选择地图
    let carLocationMapInstance: any = null;
    let carLocationMarker: any = null;
    const carLocationMapVisible = ref(false);
    const carLocationName = ref('');

    onMounted(() => {
      loadUserInfo();
      loadWaybills();
      loadCars();
      loadStatistics();
      // 延迟加载地图，确保DOM已渲染
      setTimeout(() => {
        initMap();
      }, 500);
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
      } else if (tab === 'monitor') {
        // 切换到地图标签页时，确保车辆数据已加载，然后初始化地图并加载车辆位置
        if (carList.value.length === 0) {
          loadCars().then(() => {
            setTimeout(() => {
              if (!mapInstance) {
                initMap();
              } else {
                refreshMap();
              }
            }, 300);
          });
        } else {
          setTimeout(() => {
            if (!mapInstance) {
              initMap();
            } else {
              refreshMap();
            }
          }, 100);
        }
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
          const records: any[] = pageData.records || [];
          // 1) 过滤掉明确标记为 changed=1 的旧单
          const filtered = records.filter((w) => w.changed === 0 || w.changed === undefined);
          // 2) 按业务字段去重，保留"最新且状态优先"的一条
          const dedupMap = new Map<string, any>();
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
            const statusPriority = (s: number) => {
              if (s === 1 || s === 2 || s === 3) return 2;
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
          waybillList.value = Array.from(dedupMap.values());
          waybillTotal.value = waybillList.value.length;
        }
      } catch (error: any) {
        // 只在真正严重错误时显示提示（如401、403等），其他错误静默处理
        if (error.message?.includes('401') || error.message?.includes('403') || error.message?.includes('登录')) {
          ElMessage.error(error.message || '加载运单列表失败');
        } else {
          console.warn('加载运单列表失败:', error.message || error);
          // 如果已有数据，不清空，保持显示
          if (waybillList.value.length === 0) {
            ElMessage.warning('加载运单列表失败，请稍后重试');
          }
        }
      } finally {
        loading.value = false;
      }
    };

    // 批量逆地理编码车辆位置
    const batchReverseGeocodeCars = async (cars: any[]) => {
      const apiKey = String(AMAP_API_KEY);
      const promises = cars.map(async (car) => {
        if (!car.location) return;
        
        const match = car.location.match(/([\d.]+),\s*([\d.]+)/);
        if (!match) return;
        
        const lat = match[1];
        const lng = match[2];
        const url = `https://restapi.amap.com/v3/geocode/regeo?location=${lng},${lat}&key=${apiKey}`;
        
        try {
          const response = await fetch(url);
          const data = await response.json();
          
          if (data.status === '1' && data.regeocode) {
            car.locationName = data.regeocode.formatted_address || '未知地址';
          }
        } catch (error) {
          console.warn(`车辆 ${car.carId} 位置查询失败:`, error);
        }
      });
      
      await Promise.all(promises);
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
          
          // 自动批量查询所有有坐标的车辆的位置名称
          await batchReverseGeocodeCars(carList.value);
        }
      } catch (error: any) {
        // 只在真正严重错误时显示提示（如401、403等），其他错误静默处理
        if (error.message?.includes('401') || error.message?.includes('403') || error.message?.includes('登录')) {
          ElMessage.error(error.message || '加载车辆列表失败');
        } else {
          console.warn('加载车辆列表失败:', error.message || error);
          // 如果已有数据，不清空，保持显示
          if (carList.value.length === 0) {
            ElMessage.warning('加载车辆列表失败，请稍后重试');
          }
        }
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
      currentWaybill.value = row; // 设置当前运单以便显示 waybillIdentification
      assignForm.waybillId = row.waybillId; // 保留 waybillId 用于API调用
      assignForm.carId = null;
      // 重新加载可用车辆
      loadCars();
      assignDialogVisible.value = true;
    };

    const confirmAssign = async () => {
      if (assignForm.carId === null || assignForm.carId === undefined) {
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
      carLocationName.value = '';
      carLocationMapVisible.value = false;
      carDialogVisible.value = true;
    };

    const editCar = (row: CarVO) => {
      carDialogTitle.value = '编辑车辆';
      carForm.carId = row.carId;
      carForm.licensePlate = row.licensePlate;
      carForm.carType = row.carType;
      carForm.loadCapacity = row.loadCapacity;
      carForm.driverId = row.driverId;
      carForm.driverName = row.driverName || '';
      carForm.location = row.location;
      carForm.status = row.status;
      carLocationName.value = '';
      carLocationMapVisible.value = false;
      carDialogVisible.value = true;
      
      // 如果已有位置且是坐标格式，自动查询地址名称
      if (carForm.location && carForm.location.match(/^[\d.]+,\s*[\d.]+$/)) {
        setTimeout(() => {
          reverseGeocodeCarLocation();
        }, 500);
      }
    };

    // 初始化车辆位置选择地图
    const initCarLocationMap = () => {
      // 先显示地图容器
      carLocationMapVisible.value = true;

      // 等待DOM更新后再初始化地图
      setTimeout(() => {
        const container = document.getElementById('carLocationMapContainer');
        if (!container) {
          ElMessage.warning('地图容器不存在，请稍后重试');
          carLocationMapVisible.value = false;
          return;
        }

        if (typeof (window as any).AMap === 'undefined') {
          const apiKey = String(AMAP_API_KEY);
          if (!apiKey || apiKey === 'YOUR_API_KEY' || apiKey.trim() === '') {
            ElMessage.warning('请先配置高德地图API密钥，详见 src/config/map.ts');
            carLocationMapVisible.value = false;
            return;
          }
          const script = document.createElement('script');
          script.type = 'text/javascript';
          script.src = `https://webapi.amap.com/maps?v=2.0&key=${apiKey}&callback=initCarLocationAMap`;
          script.async = true;
          (window as any).initCarLocationAMap = () => {
            createCarLocationMapInstance();
          };
          document.head.appendChild(script);
        } else {
          createCarLocationMapInstance();
        }
      }, 300); // 增加等待时间，确保对话框和DOM完全渲染
    };

    // 创建车辆位置选择地图实例
    const createCarLocationMapInstance = () => {
      const container = document.getElementById('carLocationMapContainer');
      if (!container) return;

      try {
        // 如果已有实例，先销毁
        if (carLocationMapInstance) {
          carLocationMapInstance.destroy();
          carLocationMapInstance = null;
          carLocationMarker = null;
        }

        // 解析当前位置（如果有）
        let center: [number, number] = DEFAULT_MAP_CENTER;
        if (carForm.location) {
          const match = carForm.location.match(/([\d.]+),\s*([\d.]+)/);
          if (match) {
            center = [parseFloat(match[2]), parseFloat(match[1])]; // [lng, lat]
          }
        }

        carLocationMapInstance = new (window as any).AMap.Map('carLocationMapContainer', {
          zoom: 13,
          center: center,
        });

        // 如果已有位置，显示标记
        if (carForm.location && carForm.location.match(/^[\d.]+,\s*[\d.]+$/)) {
          const match = carForm.location.match(/([\d.]+),\s*([\d.]+)/);
          if (match) {
            const lat = parseFloat(match[1]);
            const lng = parseFloat(match[2]);
            carLocationMarker = new (window as any).AMap.Marker({
              position: [lng, lat],
              draggable: true,
            });
            carLocationMarker.setMap(carLocationMapInstance);
            carLocationMapInstance.setCenter([lng, lat]);
            
            // 标记拖拽事件
            carLocationMarker.on('dragend', () => {
              const position = carLocationMarker.getPosition();
              const lat = position.getLat();
              const lng = position.getLng();
              carForm.location = `${lat},${lng}`;
              carLocationName.value = '';
            });
          }
        }

        // 地图点击事件
        carLocationMapInstance.on('click', (e: any) => {
          const lng = e.lnglat.getLng();
          const lat = e.lnglat.getLat();
          carForm.location = `${lat},${lng}`;
          carLocationName.value = '';

          // 移除旧标记
          if (carLocationMarker) {
            carLocationMapInstance.remove(carLocationMarker);
          }

          // 添加新标记
          carLocationMarker = new (window as any).AMap.Marker({
            position: [lng, lat],
            draggable: true,
          });
          carLocationMarker.setMap(carLocationMapInstance);

          // 标记拖拽事件
          carLocationMarker.on('dragend', () => {
            const position = carLocationMarker.getPosition();
            const lat = position.getLat();
            const lng = position.getLng();
            carForm.location = `${lat},${lng}`;
            carLocationName.value = '';
          });

          ElMessage.success(`已选择位置: ${lat}, ${lng}`);
        });
      } catch (error) {
        console.error('地图初始化失败:', error);
        ElMessage.warning('地图初始化失败，请检查高德地图API配置');
      }
    };

    // 清除车辆位置
    const clearCarLocation = () => {
      carForm.location = '';
      carLocationName.value = '';
      if (carLocationMarker) {
        carLocationMapInstance?.remove(carLocationMarker);
        carLocationMarker = null;
      }
      ElMessage.info('已清除位置');
    };

    // 逆地理编码：将坐标转换为地址
    const reverseGeocodeCarLocation = async () => {
      if (!carForm.location) {
        ElMessage.warning('请先选择位置');
        return;
      }

      const match = carForm.location.match(/([\d.]+),\s*([\d.]+)/);
      if (!match) {
        ElMessage.warning('位置格式不正确，应为 lat,lng');
        return;
      }

      const lat = match[1];
      const lng = match[2];
      const apiKey = String(AMAP_API_KEY);
      const url = `https://restapi.amap.com/v3/geocode/regeo?location=${lng},${lat}&key=${apiKey}`;

      try {
        const response = await fetch(url);
        const data = await response.json();
        
        if (data.status === '1' && data.regeocode) {
          carLocationName.value = data.regeocode.formatted_address || '未知地址';
          ElMessage.success('位置查询成功');
        } else {
          ElMessage.warning('位置查询失败: ' + (data.info || '未知错误'));
        }
      } catch (error: any) {
        console.error('逆地理编码失败:', error);
        ElMessage.error('位置查询失败: ' + (error.message || '未知错误'));
      }
    };

    // 在列表中逆地理编码车辆位置
    const reverseGeocodeCarLocationInList = async (car: any) => {
      if (!car.location) {
        ElMessage.warning('该车辆没有位置信息');
        return;
      }

      const match = car.location.match(/([\d.]+),\s*([\d.]+)/);
      if (!match) {
        ElMessage.warning('位置格式不正确');
        return;
      }

      const lat = match[1];
      const lng = match[2];
      const apiKey = String(AMAP_API_KEY);
      const url = `https://restapi.amap.com/v3/geocode/regeo?location=${lng},${lat}&key=${apiKey}`;

      try {
        const response = await fetch(url);
        const data = await response.json();
        
        if (data.status === '1' && data.regeocode) {
          car.locationName = data.regeocode.formatted_address || '未知地址';
          ElMessage.success('位置查询成功');
        } else {
          ElMessage.warning('位置查询失败: ' + (data.info || '未知错误'));
        }
      } catch (error: any) {
        console.error('逆地理编码失败:', error);
        ElMessage.error('位置查询失败: ' + (error.message || '未知错误'));
      }
    };

    const saveCar = async () => {
      if (!carFormRef.value) return;
      await carFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          try {
            if (carForm.carId) {
              const updateData: CarUpdateDTO = {
                carId: carForm.carId,
                driverId: carForm.driverId,
                location: carForm.location,
                status: carForm.status,
              };
              await updateCar(carForm.carId, updateData);
              ElMessage.success('更新成功');
            } else {
              const createData: CarCreateDTO = {
                driverId: carForm.driverId ?? 0,
                licensePlate: carForm.licensePlate || '',
                carType: carForm.carType || '',
                loadCapacity: carForm.loadCapacity ?? 0,
                location: carForm.location || '',
                status: carForm.status ?? 0,
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

    const releaseCar = async (carId: number) => {
      try {
        await ElMessageBox.confirm('确认将该车辆状态改为“可用”吗？', '释放车辆', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });
        await updateCarStatus(carId, 0);
        ElMessage.success('车辆已释放为可用');
        loadCars();
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '释放车辆失败');
        }
      }
    };

    // 初始化地图
    const initMap = () => {
      const container = document.getElementById('monitorMapContainer');
      if (!container) {
        console.warn('地图容器不存在');
        return;
      }

      // 检查是否已加载高德地图API
      if (typeof (window as any).AMap === 'undefined') {
        const apiKey = String(AMAP_API_KEY);
        if (!apiKey || apiKey === 'YOUR_API_KEY' || apiKey.trim() === '') {
          ElMessage.warning('请先配置高德地图API密钥，详见 src/config/map.ts');
          return;
        }
        // 动态加载高德地图API（包含地理编码服务）
        const script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${apiKey}&plugin=AMap.Geocoder&callback=initAMap`;
        script.async = true;
        (window as any).initAMap = () => {
          createMapInstance();
        };
        document.head.appendChild(script);
      } else {
        createMapInstance();
      }
    };

    // 创建地图实例
    const createMapInstance = () => {
      const container = document.getElementById('monitorMapContainer');
      if (!container) return;

      try {
        mapInstance = new (window as any).AMap.Map('monitorMapContainer', {
          zoom: DEFAULT_MAP_ZOOM,
          center: DEFAULT_MAP_CENTER,
        });
        refreshMap();
      } catch (error) {
        console.error('地图初始化失败:', error);
        ElMessage.warning('地图初始化失败，请检查高德地图API配置');
      }
    };

    // 常见地址的坐标映射（长春市）
    const addressMap: Record<string, [number, number]> = {
      '长春': [125.323544, 43.817071],
      '长春市': [125.323544, 43.817071],
      '长春工业大学': [125.2875, 43.8964],
      '长春工业大学北湖西区': [125.2875, 43.8964],
      '长春工业大学北湖校区': [125.2875, 43.8964],
      '吉林大学': [125.2985, 43.8844],
      '吉林大学前卫校区': [125.2985, 43.8844],
      '宽城': [125.3200, 43.9000],
      '宽城区': [125.3200, 43.9000],
      '南关': [125.3300, 43.8600],
      '南关区': [125.3300, 43.8600],
      '朝阳': [125.3000, 43.8300],
      '朝阳区': [125.3000, 43.8300],
      '绿园': [125.2500, 43.8800],
      '绿园区': [125.2500, 43.8800],
      '二道': [125.3800, 43.8700],
      '二道区': [125.3800, 43.8700],
      '双阳': [125.6500, 43.5200],
      '双阳区': [125.6500, 43.5200],
      '净月': [125.4500, 43.8000],
      '净月区': [125.4500, 43.8000],
      '净月潭': [125.4500, 43.8000],
      '长春站': [125.3235, 43.9065],
      '长春火车站': [125.3235, 43.9065],
      '长春西站': [125.2000, 43.8500],
      '龙嘉机场': [125.7000, 44.0000],
      '长春龙嘉国际机场': [125.7000, 44.0000],
      '人民广场': [125.3200, 43.8900],
      '文化广场': [125.3100, 43.8800],
      '南湖公园': [125.3000, 43.8500],
      '伪满皇宫': [125.3500, 43.9200],
      '长影世纪城': [125.4500, 43.7800],
      '欧亚卖场': [125.2500, 43.8600],
      '红旗街': [125.2900, 43.8700],
      '重庆路': [125.3200, 43.8900],
      '桂林路': [125.3100, 43.8700],
      '汽车厂': [125.2200, 43.8500],
      '一汽': [125.2200, 43.8500],
      '一汽大众': [125.2200, 43.8500],
    };

    // 解析位置字符串为坐标
    const parseLocation = (location: string): [number, number] | null => {
      if (!location) return null;
      
      // 1. 先检查地址映射
      const trimmedLocation = location.trim();
      if (addressMap[trimmedLocation]) {
        console.log(`使用地址映射: ${trimmedLocation} -> [${addressMap[trimmedLocation][0]}, ${addressMap[trimmedLocation][1]}]`);
        return addressMap[trimmedLocation];
      }
      
      // 2. 尝试解析 "lat,lng" 格式
      const match = location.match(/([\d.]+),\s*([\d.]+)/);
      if (match) {
        return [parseFloat(match[2]), parseFloat(match[1])]; // 注意：高德地图是 [lng, lat]
      }
      
      return null;
    };

    // 地理编码：将地址转换为坐标（使用高德地图Web服务API）
    const geocodeAddress = (address: string): Promise<[number, number] | null> => {
      return new Promise((resolve) => {
        try {
          if (!address) {
            resolve(null);
            return;
          }

          // 尝试解析坐标格式
          const coords = parseLocation(address);
          if (coords) {
            resolve(coords);
            return;
          }

          // 直接使用JS API进行地理编码（因为当前API密钥是JS API类型）
          // 如果Web服务API密钥可用，可以改用Web服务API
          if (typeof (window as any).AMap !== 'undefined') {
            // 优先使用JS API
            tryGeocodeWithJSAPI(address, resolve);
          } else {
            // 如果AMap未加载，尝试使用Web服务API（需要Web服务类型的密钥）
            const apiKey = String(AMAP_API_KEY);
            const encodedAddress = encodeURIComponent(address);
            const url = `https://restapi.amap.com/v3/geocode/geo?key=${apiKey}&address=${encodedAddress}&city=长春市`;
            
            console.log(`AMap未加载，尝试使用Web API地理编码: ${address}`);
            
            const timeout = setTimeout(() => {
              console.warn(`地理编码超时: ${address}`);
              resolve(null);
            }, 15000);

            fetch(url)
              .then(response => {
                console.log(`Web API响应状态: ${response.status} for ${address}`);
                if (!response.ok) {
                  throw new Error(`HTTP ${response.status}`);
                }
                return response.json();
              })
              .then(data => {
                clearTimeout(timeout);
                console.log(`Web API返回数据:`, data);
                if (data.status === '1' && data.geocodes && data.geocodes.length > 0) {
                  const location = data.geocodes[0].location.split(',');
                  const lng = parseFloat(location[0]);
                  const lat = parseFloat(location[1]);
                  console.log(`地理编码成功: ${address} -> [${lng}, ${lat}]`);
                  resolve([lng, lat]);
                } else {
                  console.warn(`地理编码失败: ${address}`, data);
                  resolve(null);
                }
              })
              .catch(error => {
                clearTimeout(timeout);
                console.error(`地理编码请求失败: ${address}`, error);
                resolve(null);
              });
          }
        } catch (err) {
          console.error(`地理编码处理异常: ${address}`, err);
          resolve(null);
        }
      });
    };

    // 备用方案：使用JS API进行地理编码
    const tryGeocodeWithJSAPI = (address: string, resolve: (value: [number, number] | null) => void) => {
      console.log(`尝试使用JS API地理编码: ${address}`);
      if (typeof (window as any).AMap === 'undefined') {
        console.warn('AMap未加载，无法使用JS API');
        resolve(null);
        return;
      }

      const AMap = (window as any).AMap;
      let timeoutId: number | null = null;
      let resolved = false;

      const cleanup = () => {
        if (timeoutId) {
          clearTimeout(timeoutId);
          timeoutId = null;
        }
      };

      const doResolve = (value: [number, number] | null) => {
        if (!resolved) {
          resolved = true;
          cleanup();
          resolve(value);
        }
      };

      // 设置超时（20秒）
      timeoutId = setTimeout(() => {
        console.warn(`JS API地理编码超时: ${address}`);
        doResolve(null);
      }, 20000);

      // 确保Geocoder插件已加载
      const loadGeocoder = (callback: () => void) => {
        if (AMap.Geocoder && typeof AMap.Geocoder === 'function') {
          console.log('Geocoder已可用');
          callback();
        } else {
          console.log('正在加载Geocoder插件...');
          AMap.plugin('AMap.Geocoder', () => {
            console.log('Geocoder插件加载完成');
            if (AMap.Geocoder && typeof AMap.Geocoder === 'function') {
              callback();
            } else {
              console.error('Geocoder插件加载失败');
              doResolve(null);
            }
          });
        }
      };

      loadGeocoder(() => {
        try {
          console.log(`使用Geocoder进行地理编码: ${address}`);
          const geocoder = new AMap.Geocoder({
            city: '长春市',
          });
          
          console.log(`调用getLocation: ${address}`);
          geocoder.getLocation(address, (status: string, result: any) => {
            console.log(`Geocoder回调被触发 - 状态: ${status} for ${address}`, result);
            if (status === 'complete' && result && result.geocodes && result.geocodes.length > 0) {
              const location = result.geocodes[0].location;
              if (location && typeof location.lng === 'number' && typeof location.lat === 'number') {
                console.log(`地理编码成功(JS API): ${address} -> [${location.lng}, ${location.lat}]`);
                doResolve([location.lng, location.lat]);
              } else {
                console.warn(`地理编码返回的坐标格式不正确:`, location);
                doResolve(null);
              }
            } else {
              console.warn(`地理编码失败(JS API): ${address}`, status, result);
              doResolve(null);
            }
          });
          
          // 额外检查：如果5秒后还没有回调，记录警告
          setTimeout(() => {
            if (!resolved) {
              console.warn(`警告: getLocation调用5秒后仍未收到回调 for ${address}`);
            }
          }, 5000);
        } catch (err) {
          console.error(`地理编码异常(JS API): ${address}`, err);
          doResolve(null);
        }
      });
    };

    // 刷新地图上的车辆位置
    const refreshMap = async () => {
      if (!mapInstance) {
        console.log('地图实例不存在，正在初始化...');
        initMap();
        // 等待地图初始化完成
        setTimeout(() => {
          if (mapInstance) {
            refreshMap();
          }
        }, 1000);
        return;
      }

      // 清除现有标记
      markers.forEach(marker => {
        mapInstance.remove(marker);
      });
      markers = [];

      try {
        // 检查车辆列表是否已加载
        if (carList.value.length === 0) {
          console.log('车辆列表为空，正在加载...');
          await loadCars();
        }

        console.log('当前车辆列表:', carList.value.map(car => ({
          carId: car.carId,
          location: car.location,
          status: car.status,
          driverId: car.driverId,
        })));

        // 获取所有车辆的位置
        const carsToShow = selectedCarId.value
          ? carList.value.filter(car => car.carId === selectedCarId.value)
          : carList.value;

        console.log(`准备显示 ${carsToShow.length} 辆车辆`);

        if (carsToShow.length === 0) {
          ElMessage.info('当前没有可显示的车辆');
          return;
        }

        // 检查有多少车辆有位置信息
        const carsWithLocation = carsToShow.filter(car => car.location && car.location.trim() !== '');
        console.log(`有位置信息的车辆: ${carsWithLocation.length}/${carsToShow.length}`);
        if (carsWithLocation.length === 0) {
          ElMessage.warning('当前车辆都没有位置信息，无法在地图上显示');
          console.warn('所有车辆都没有位置信息:', carsToShow);
          return;
        }

        // 统计信息
        let successCount = 0;
        let failCount = 0;
        const failedCars: string[] = [];

        // 使用 Promise.all 并行处理所有车辆的地理编码
        const carPromises = carsToShow.map(async (car) => {
          if (!car.location) {
            failCount++;
            failedCars.push(`车辆${car.carId}(无位置信息)`);
            console.warn(`车辆 ${car.carId} 没有位置信息`);
            return null;
          }

          console.log(`正在解析车辆 ${car.carId} 的位置: ${car.location}`);
          const coords = await geocodeAddress(car.location);
          if (!coords) {
            failCount++;
            failedCars.push(`车辆${car.carId}(${car.location})`);
            console.warn(`车辆 ${car.carId} 的位置无法解析: ${car.location}`);
            return null;
          }

          successCount++;
          console.log(`车辆 ${car.carId} 位置解析成功: [${coords[0]}, ${coords[1]}]`);
          return {
            car,
            coords,
          };
        });

        const carLocations = await Promise.all(carPromises);

        // 创建标记
        for (const item of carLocations) {
          if (!item) continue;

          const { car, coords } = item;
          try {
            const marker = new (window as any).AMap.Marker({
              position: coords,
              title: `车辆${car.carId}${car.driverName ? ' - ' + car.driverName : ''}`,
              label: {
                content: `车辆${car.carId}`,
                direction: 'right',
              },
            });

            marker.setMap(mapInstance);
            markers.push(marker);
            console.log(`车辆 ${car.carId} 标记已添加到地图`);
          } catch (err) {
            console.error(`创建车辆 ${car.carId} 标记失败:`, err);
            failCount++;
            successCount--;
          }
        }

        // 如果有标记，调整地图视野
        if (markers.length > 0) {
          mapInstance.setFitView(markers);
          console.log(`成功显示 ${markers.length} 辆车辆的位置`);
          if (failCount > 0) {
            ElMessage.warning(`成功显示 ${successCount} 辆车辆，${failCount} 辆车辆位置解析失败`);
          } else {
            ElMessage.success(`成功显示 ${successCount} 辆车辆的位置`);
          }
        } else {
          ElMessage.warning('没有车辆位置可以显示在地图上。请检查车辆是否有位置信息，或位置格式是否正确');
          console.warn('没有可显示的车辆位置', {
            totalCars: carsToShow.length,
            failedCars,
          });
        }
      } catch (error: any) {
        console.error('刷新地图异常:', error);
        ElMessage.error('刷新地图失败: ' + (error.message || '未知错误'));
      }
    };

    // 车辆选择变化
    const onCarSelectChange = () => {
      refreshMap();
    };

    // 显示全部车辆
    const showAllCars = () => {
      selectedCarId.value = null;
      refreshMap();
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
      releaseCar,
      handleLogout,
      getStatusDesc,
      getStatusType,
      getCarStatusDesc,
      getCarStatusType,
      selectedCarId,
      refreshMap,
      onCarSelectChange,
      showAllCars,
      carLocationMapVisible,
      carLocationName,
      initCarLocationMap,
      clearCarLocation,
      reverseGeocodeCarLocation,
      reverseGeocodeCarLocationInList,
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
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.85);
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

.search-bar {
  margin-bottom: 16px;
}

.map-container {
  background-color: #020617;
  border-radius: 8px;
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

.driver-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.driver-id {
  color: #94a3b8;
  font-size: 12px;
}

.driver-edit {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.driver-edit-tip {
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 960px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
