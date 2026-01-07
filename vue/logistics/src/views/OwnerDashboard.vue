<template>
  <div class="dashboard">
    <div class="dashboard-bg"></div>
    <el-container class="dashboard-shell">
      <el-header class="dashboard-header">
        <div class="header-content">
          <div class="header-left">
            <div class="logo">
              <span class="logo-dot" />
              <div class="logo-text">
                <span class="logo-title">货主工作台</span>
                <span class="logo-subtitle">下单 · 跟踪 · 签收，一站式管理您的运单</span>
              </div>
            </div>
          </div>
          <div class="user-info">
            <span class="user-name">{{ userInfo?.phone || '货主' }}</span>
            <el-button link @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main class="dashboard-main">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="dashboard-tabs">
          <!-- 发布运单 -->
          <el-tab-pane label="发布运单" name="create">
            <el-card class="panel-card">
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
            <el-card class="panel-card">
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
            <el-card class="panel-card">
              <div class="map-controls" style="margin-bottom: 15px">
                <el-select v-model="selectedWaybillId" placeholder="选择运单" clearable style="width: 200px" @change="onWaybillSelectChange">
                  <el-option
                    v-for="waybill in waybillList"
                    :key="waybill.waybillId"
                    :label="`运单${waybill.waybillIdentification} - ${waybill.goodsInformation}`"
                    :value="waybill.waybillId"
                  />
                </el-select>
                <el-button type="primary" @click="refreshOwnerMap" style="margin-left: 10px">刷新位置</el-button>
                <el-button @click="showAllWaybills" style="margin-left: 10px">显示全部运单</el-button>
              </div>
              <div class="map-container" id="mapContainer" style="height: 600px"></div>
            </el-card>
          </el-tab-pane>

          <!-- 个人信息 -->
          <el-tab-pane label="个人信息" name="profile">
            <el-card class="panel-card">
              <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="120px">
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone" disabled />
                  <div style="font-size: 12px; color: #909399; margin-top: 5px;">手机号不可修改</div>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
                </el-form-item>
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input 
                    v-model="profileForm.oldPassword" 
                    type="password" 
                    placeholder="修改密码时请输入原密码"
                    show-password
                  />
                  <div style="font-size: 12px; color: #909399; margin-top: 5px;">不修改密码可留空</div>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input 
                    v-model="profileForm.newPassword" 
                    type="password" 
                    placeholder="请输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input 
                    v-model="profileForm.confirmPassword" 
                    type="password" 
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateProfile" :loading="updating">保存</el-button>
                  <el-button @click="resetProfileForm">重置</el-button>
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
  createWaybill,
  queryWaybillsByStatus,
  getWaybillDetail,
  cancelWaybill as cancelWaybillAPI,
  getConsignorInfo,
  updateConsignorInfo,
  WaybillCreateDTO,
  WaybillVO,
} from '../api';
import { clearAuth, getUserInfo } from '../utils/auth';
import { getStatusDesc, getStatusType } from '../utils/waybillStatus';
import { AMAP_API_KEY, DEFAULT_MAP_CENTER, DEFAULT_MAP_ZOOM } from '../config/map';

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
    const profileFormRef = ref();
    const updating = ref(false);
    const profileForm = reactive({
      phone: '',
      email: '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    });

    // 个人信息表单验证规则
    const profileRules = {
      email: [
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
      ],
      oldPassword: [
        {
          validator: (rule: any, value: string, callback: (error?: Error) => void) => {
            // 如果填写了新密码，原密码必填
            if (profileForm.newPassword && !value) {
              callback(new Error('修改密码时请输入原密码'));
            } else {
              callback();
            }
          },
          trigger: 'blur',
        },
      ],
      newPassword: [
        {
          validator: (rule: any, value: string, callback: (error?: Error) => void) => {
            if (value) {
              if (value.length < 6) {
                callback(new Error('密码长度不能少于6位'));
              } else if (value.length > 20) {
                callback(new Error('密码长度不能超过20位'));
              } else {
                callback();
              }
            } else {
              callback();
            }
          },
          trigger: 'blur',
        },
      ],
      confirmPassword: [
        {
          validator: (rule: any, value: string, callback: (error?: Error) => void) => {
            if (profileForm.newPassword) {
              if (!value) {
                callback(new Error('请再次输入新密码'));
              } else if (value !== profileForm.newPassword) {
                callback(new Error('两次输入的密码不一致'));
              } else {
                callback();
              }
            } else {
              callback();
            }
          },
          trigger: 'blur',
        },
      ],
    };

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

    // 地图相关
    const selectedWaybillId = ref<string | null>(null);
    let ownerMapInstance: any = null;
    let ownerMarkers: any[] = [];

    onMounted(() => {
      loadUserInfo();
      if (activeTab.value === 'track') {
        loadWaybills();
      }
      // 延迟加载地图
      setTimeout(() => {
        initOwnerMap();
      }, 500);
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
      } else if (tab === 'realtime') {
        // 切换到地图标签页时，确保运单数据已加载，然后初始化地图并加载运单位置
        if (waybillList.value.length === 0) {
          loadWaybills().then(() => {
            setTimeout(() => {
              if (!ownerMapInstance) {
                initOwnerMap();
              } else {
                refreshOwnerMap();
              }
            }, 300);
          });
        } else {
          setTimeout(() => {
            if (!ownerMapInstance) {
              initOwnerMap();
            } else {
              refreshOwnerMap();
            }
          }, 100);
        }
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
          const records: any[] = pageData.records || [];
          // 1) 过滤掉明确标记为 changed=1 的旧单
          const filtered = records.filter((w) => w.changed === 0 || w.changed === undefined);
          // 2) 按业务字段去重（同一运单被复制时，这些字段是一致的），保留“最新且状态优先”的一条
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
            // 优先级：已分配/运输中/已完成 > 待分配；同优先级取创建时间更晚的一条
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
          total.value = waybillList.value.length;
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

    const cancelWaybill = async (waybillId: string) => {
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

    const resetProfileForm = () => {
      profileFormRef.value?.resetFields();
      // 重新加载用户信息
      loadUserInfo();
    };

    const updateProfile = async () => {
      if (!profileFormRef.value) return;

      try {
        // 表单验证
        await profileFormRef.value.validate();

        updating.value = true;

        // 构建更新数据
        const updateData: any = {
          email: profileForm.email,
        };

        // 如果填写了新密码，需要包含原密码和新密码
        if (profileForm.newPassword) {
          updateData.oldPassword = profileForm.oldPassword;
          updateData.newPassword = profileForm.newPassword;
        }

        // 调用更新API
        await updateConsignorInfo(updateData);

        ElMessage.success('个人信息更新成功');
        
        // 清空密码字段
        profileForm.oldPassword = '';
        profileForm.newPassword = '';
        profileForm.confirmPassword = '';
        
        // 重新加载用户信息
        await loadUserInfo();
      } catch (error: any) {
        if (error.message) {
          ElMessage.error(error.message);
        } else {
          ElMessage.error('更新失败，请稍后重试');
        }
      } finally {
        updating.value = false;
      }
    };

    // 初始化地图
    const initOwnerMap = () => {
      const container = document.getElementById('mapContainer');
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
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${apiKey}&plugin=AMap.Geocoder&callback=initOwnerAMap`;
        script.async = true;
        (window as any).initOwnerAMap = () => {
          createOwnerMapInstance();
        };
        document.head.appendChild(script);
      } else {
        createOwnerMapInstance();
      }
    };

    // 创建地图实例
    const createOwnerMapInstance = () => {
      const container = document.getElementById('mapContainer');
      if (!container) return;

      try {
        ownerMapInstance = new (window as any).AMap.Map('mapContainer', {
          zoom: DEFAULT_MAP_ZOOM,
          center: DEFAULT_MAP_CENTER,
        });
        refreshOwnerMap();
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

    // 地理编码：将地址转换为坐标（使用高德地图Web服务API）
    const geocodeAddress = (address: string): Promise<[number, number] | null> => {
      return new Promise((resolve) => {
        try {
          if (!address) {
            resolve(null);
            return;
          }

          // 1. 先检查地址映射
          const trimmedAddress = address.trim();
          if (addressMap[trimmedAddress]) {
            console.log(`使用地址映射: ${trimmedAddress} -> [${addressMap[trimmedAddress][0]}, ${addressMap[trimmedAddress][1]}]`);
            resolve(addressMap[trimmedAddress]);
            return;
          }

          // 2. 尝试解析坐标格式 \"lat,lng\"
          const coordMatch = address.match(/([\\d.]+),\\s*([\\d.]+)/);
          if (coordMatch) {
            resolve([parseFloat(coordMatch[2]), parseFloat(coordMatch[1])]); // [lng, lat]
            return;
          }

          // 使用高德地图Web服务API进行地理编码（更可靠）
          const apiKey = String(AMAP_API_KEY);
          const encodedAddress = encodeURIComponent(address);
          const url = `https://restapi.amap.com/v3/geocode/geo?key=${apiKey}&address=${encodedAddress}&city=长春市`;
          
          // 添加超时处理（15秒）
          const timeout = setTimeout(() => {
            console.warn(`地理编码超时: ${address}`);
            resolve(null);
          }, 15000);

          fetch(url)
            .then(response => response.json())
            .then(data => {
              clearTimeout(timeout);
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
              // 如果Web API失败，尝试使用JS API
              tryGeocodeWithJSAPI(address, resolve);
            });
        } catch (err) {
          console.error(`地理编码处理异常: ${address}`, err);
          resolve(null);
        }
      });
    };

    // 备用方案：使用JS API进行地理编码
    const tryGeocodeWithJSAPI = (address: string, resolve: (value: [number, number] | null) => void) => {
      if (typeof (window as any).AMap === 'undefined') {
        resolve(null);
        return;
      }

      const AMap = (window as any).AMap;
      const timeout = setTimeout(() => {
        resolve(null);
      }, 10000);

      // 检查 Geocoder 是否已加载
      if (AMap.Geocoder) {
        try {
          const geocoder = new AMap.Geocoder({
            city: '长春市',
          });
          geocoder.getLocation(address, (status: string, result: any) => {
            clearTimeout(timeout);
            if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
              const location = result.geocodes[0].location;
              console.log(`地理编码成功(JS API): ${address} -> [${location.lng}, ${location.lat}]`);
              resolve([location.lng, location.lat]);
            } else {
              console.warn(`地理编码失败(JS API): ${address}`, status);
              resolve(null);
            }
          });
        } catch (err) {
          clearTimeout(timeout);
          console.error(`地理编码异常(JS API): ${address}`, err);
          resolve(null);
        }
      } else {
        // 如果未加载，通过插件方式异步加载
        AMap.plugin('AMap.Geocoder', () => {
          try {
            const geocoder = new AMap.Geocoder({
              city: '长春市',
            });
            geocoder.getLocation(address, (status: string, result: any) => {
              clearTimeout(timeout);
              if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
                const location = result.geocodes[0].location;
                console.log(`地理编码成功(JS API): ${address} -> [${location.lng}, ${location.lat}]`);
                resolve([location.lng, location.lat]);
              } else {
                console.warn(`地理编码失败(JS API): ${address}`, status);
                resolve(null);
              }
            });
          } catch (err) {
            clearTimeout(timeout);
            console.error(`地理编码异常(JS API): ${address}`, err);
            resolve(null);
          }
        });
      }
    };

    // 刷新地图上的运单位置
    const refreshOwnerMap = async () => {
      if (!ownerMapInstance) {
        console.log('地图实例不存在，正在初始化...');
        initOwnerMap();
        // 等待地图初始化完成
        setTimeout(() => {
          if (ownerMapInstance) {
            refreshOwnerMap();
          }
        }, 1000);
        return;
      }

      // 清除现有标记
      ownerMarkers.forEach(marker => {
        ownerMapInstance.remove(marker);
      });
      ownerMarkers = [];

      try {
        // 检查运单列表是否已加载
        if (waybillList.value.length === 0) {
          console.log('运单列表为空，正在加载...');
          await loadWaybills();
        }

        // 获取要显示的运单
        const waybillsToShow = selectedWaybillId.value
          ? waybillList.value.filter(w => w.waybillId === selectedWaybillId.value)
          : waybillList.value.filter(w => w.status === 1 || w.status === 2); // 只显示已分配或运输中的运单

        console.log(`准备显示 ${waybillsToShow.length} 个运单`);

        if (waybillsToShow.length === 0) {
          ElMessage.info('当前没有可追踪的运单（只显示已分配或运输中的运单）');
          return;
        }

        // 统计信息
        let successCount = 0;
        let failCount = 0;
        const failedAddresses: string[] = [];

        for (const waybill of waybillsToShow) {
          console.log(`处理运单 ${waybill.waybillIdentification}: ${waybill.startAddress} -> ${waybill.endAddress}`);
          
          // 显示起点
          const startCoords = await geocodeAddress(waybill.startAddress);
          if (startCoords) {
            try {
              const startMarker = new (window as any).AMap.Marker({
                position: startCoords,
                title: `起点: ${waybill.startAddress}`,
                icon: new (window as any).AMap.Icon({
                  size: new (window as any).AMap.Size(32, 32),
                  image: 'https://webapi.amap.com/theme/v1.3/markers/n/start.png',
                }),
                label: {
                  content: '起点',
                  direction: 'right',
                },
              });
              startMarker.setMap(ownerMapInstance);
              ownerMarkers.push(startMarker);
              console.log(`起点标记已添加: ${waybill.startAddress} -> [${startCoords[0]}, ${startCoords[1]}]`);
            } catch (err) {
              console.error(`创建起点标记失败: ${waybill.startAddress}`, err);
              failCount++;
            }
          } else {
            console.warn(`起点地址无法解析: ${waybill.startAddress}`);
            failedAddresses.push(`起点: ${waybill.startAddress}`);
            failCount++;
          }

          // 显示终点
          const endCoords = await geocodeAddress(waybill.endAddress);
          if (endCoords) {
            try {
              const endMarker = new (window as any).AMap.Marker({
                position: endCoords,
                title: `终点: ${waybill.endAddress}`,
                icon: new (window as any).AMap.Icon({
                  size: new (window as any).AMap.Size(32, 32),
                  image: 'https://webapi.amap.com/theme/v1.3/markers/n/end.png',
                }),
                label: {
                  content: '终点',
                  direction: 'right',
                },
              });
              endMarker.setMap(ownerMapInstance);
              ownerMarkers.push(endMarker);
              console.log(`终点标记已添加: ${waybill.endAddress} -> [${endCoords[0]}, ${endCoords[1]}]`);
            } catch (err) {
              console.error(`创建终点标记失败: ${waybill.endAddress}`, err);
              failCount++;
            }
          } else {
            console.warn(`终点地址无法解析: ${waybill.endAddress}`);
            failedAddresses.push(`终点: ${waybill.endAddress}`);
            failCount++;
          }

          // 如果有起点和终点，绘制路线
          if (startCoords && endCoords) {
            try {
              const polyline = new (window as any).AMap.Polyline({
                path: [startCoords, endCoords],
                strokeColor: '#3366FF',
                strokeWeight: 3,
                strokeStyle: 'solid',
              });
              polyline.setMap(ownerMapInstance);
              ownerMarkers.push(polyline);
              successCount++;
              console.log(`路线已绘制: ${waybill.startAddress} -> ${waybill.endAddress}`);
            } catch (err) {
              console.error(`绘制路线失败:`, err);
            }
          }
        }

        // 如果有标记，调整地图视野
        if (ownerMarkers.length > 0) {
          ownerMapInstance.setFitView(ownerMarkers);
          console.log(`成功显示 ${ownerMarkers.length} 个标记`);
          if (failCount > 0) {
            ElMessage.warning(`成功显示 ${successCount} 个运单，${failCount} 个地址解析失败`);
          } else {
            ElMessage.success(`成功显示 ${successCount} 个运单的位置`);
          }
        } else {
          ElMessage.warning('没有运单位置可以显示在地图上。请检查运单地址是否正确');
          console.warn('没有可显示的运单位置', {
            totalWaybills: waybillsToShow.length,
            failedAddresses,
          });
        }
      } catch (error: any) {
        console.error('刷新地图异常:', error);
        ElMessage.error('刷新地图失败: ' + (error.message || '未知错误'));
      }
    };

    // 运单选择变化
    const onWaybillSelectChange = () => {
      refreshOwnerMap();
    };

    // 显示全部运单
    const showAllWaybills = () => {
      selectedWaybillId.value = null;
      refreshOwnerMap();
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
      profileFormRef,
      profileRules,
      updating,
      handleTabChange,
      handleCreateWaybill,
      resetForm,
      loadWaybills,
      viewWaybillDetail,
      cancelWaybill,
      confirmReceive,
      updateProfile,
      resetProfileForm,
      handleLogout,
      getStatusDesc,
      getStatusType,
      selectedWaybillId,
      refreshOwnerMap,
      onWaybillSelectChange,
      showAllWaybills,
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

.dashboard-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 10% 20%, rgba(96, 165, 250, 0.22), transparent 55%),
    radial-gradient(circle at 80% 90%, rgba(129, 140, 248, 0.25), transparent 55%);
  opacity: 0.9;
  filter: blur(2px);
}

.dashboard-shell {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
  padding: 20px 16px 32px;
  color: #e5e7eb;
}

.dashboard-header {
  padding-inline: 0;
}

.dashboard-main {
  padding-inline: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  background: linear-gradient(135deg, #22c55e, #4ade80);
  box-shadow: 0 0 12px rgba(22, 163, 74, 0.85);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 18px;
  font-weight: 600;
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

.panel-card {
  background: rgba(15, 23, 42, 0.97);
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.5);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.9);
}

.search-bar {
  margin-bottom: 16px;
}

.map-container {
  background-color: #020617;
  border-radius: 8px;
}

@media (max-width: 960px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
