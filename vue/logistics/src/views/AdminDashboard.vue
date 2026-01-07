<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>系统管理员工作台</h2>
          <div class="user-info">
            <span>管理员</span>
            <el-button link @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 用户管理 -->
          <el-tab-pane label="用户管理" name="users">
            <el-card>
              <div class="search-bar">
                <el-select v-model="userQuery.role" placeholder="按角色筛选" clearable style="width: 150px">
                  <el-option label="货主" value="consignor" />
                  <el-option label="调度员" value="dispatcher" />
                  <el-option label="司机" value="driver" />
                </el-select>
                <el-input
                  v-model="userQuery.keyword"
                  placeholder="搜索用户名"
                  style="width: 200px; margin-left: 10px"
                  clearable
                />
                <el-button type="primary" @click="loadUsers" style="margin-left: 10px">查询</el-button>
                <el-button type="success" @click="openCreateUserDialog" style="margin-left: 10px">
                  新增用户
                </el-button>
              </div>
              <el-table :data="userList" style="width: 100%; margin-top: 20px" v-loading="loading">
                <el-table-column prop="userId" label="用户ID" width="100" />
                <el-table-column prop="type" label="角色" width="100">
                  <template #default="scope">
                    <el-tag>
                      {{
                        scope.row.type === 1
                          ? '货主'
                          : scope.row.type === 2
                            ? '调度员'
                            : scope.row.type === 3
                              ? '司机'
                              : '管理员'
                      }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="username" label="用户名" width="150" />
                <el-table-column prop="phone" label="手机号" width="120" />
                <el-table-column prop="email" label="邮箱" width="180" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                      {{ scope.row.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="260" fixed="right">
                  <template #default="scope">
                    <el-button size="small" @click="openEditUserDialog(scope.row)">编辑</el-button>
                    <el-button
                      size="small"
                      :type="scope.row.status === 1 ? 'danger' : 'success'"
                      @click="toggleUserStatus(scope.row)"
                    >
                      {{ scope.row.status === 1 ? '禁用' : '启用' }}
                    </el-button>
                    <el-button size="small" type="danger" @click="deleteUser(scope.row.userId)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-model:current-page="userPage"
                v-model:page-size="userPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="userTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadUsers"
                @current-change="loadUsers"
                style="margin-top: 20px"
              />
            </el-card>
          </el-tab-pane>

          <!-- 基础数据管理 -->
          <el-tab-pane label="基础数据管理" name="data">
            <el-card>
              <el-tabs v-model="dataTab">
                <el-tab-pane label="车型管理" name="carType">
                  <div class="data-manage">
                    <el-button type="primary" @click="showCarTypeDialog">新增车型</el-button>
                    <el-table :data="carTypeList" style="width: 100%; margin-top: 20px">
                      <el-table-column prop="name" label="车型名称" />
                      <el-table-column prop="loadCapacity" label="载重(吨)" />
                      <el-table-column prop="volume" label="容积(立方米)" />
                      <el-table-column label="操作" width="150">
                        <template #default="scope">
                          <el-button size="small" @click="editCarType(scope.row)">编辑</el-button>
                          <el-button size="small" type="danger" @click="deleteCarType(scope.row.id)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="计费规则" name="pricing">
                  <div class="data-manage">
                    <el-button type="primary" @click="showPricingDialog">新增规则</el-button>
                    <el-table :data="pricingList" style="width: 100%; margin-top: 20px">
                      <el-table-column prop="name" label="规则名称" />
                      <el-table-column prop="basePrice" label="基础价格(元)" />
                      <el-table-column prop="pricePerKm" label="每公里价格(元)" />
                      <el-table-column label="操作" width="150">
                        <template #default="scope">
                          <el-button size="small" @click="editPricing(scope.row)">编辑</el-button>
                          <el-button size="small" type="danger" @click="deletePricing(scope.row.id)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </el-tab-pane>

          <!-- 操作日志 -->
          <el-tab-pane label="操作日志" name="logs">
            <el-card>
              <div class="search-bar">
                <el-date-picker
                  v-model="logQuery.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
                <el-select v-model="logQuery.action" placeholder="操作类型" clearable style="width: 150px; margin-left: 10px">
                  <el-option label="登录" value="login" />
                  <el-option label="创建" value="create" />
                  <el-option label="更新" value="update" />
                  <el-option label="删除" value="delete" />
                </el-select>
                <el-button type="primary" @click="loadLogs" style="margin-left: 10px">查询</el-button>
              </div>
              <el-table :data="logList" style="width: 100%; margin-top: 20px" v-loading="logLoading">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="userId" label="用户ID" width="100" />
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column prop="action" label="操作" width="100" />
                <el-table-column prop="module" label="模块" width="120" />
                <el-table-column prop="description" label="描述" />
                <el-table-column prop="ip" label="IP地址" width="120" />
                <el-table-column prop="createTime" label="操作时间" width="180" />
              </el-table>
              <el-pagination
                v-model:current-page="logPage"
                v-model:page-size="logPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="logTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadLogs"
                @current-change="loadLogs"
                style="margin-top: 20px"
              />
            </el-card>
          </el-tab-pane>

          <!-- 系统监控 -->
          <el-tab-pane label="系统监控" name="monitor">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ systemStats.apiCalls }}</div>
                    <div class="stat-label">今日API调用量</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ systemStats.activeUsers }}</div>
                    <div class="stat-label">在线用户数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ systemStats.dbConnections }}</div>
                    <div class="stat-label">数据库连接数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card>
                  <div class="stat-item">
                    <div class="stat-value">{{ systemStats.cpuUsage }}%</div>
                    <div class="stat-label">CPU使用率</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-card style="margin-top: 20px">
              <div class="monitor-chart">
                <div style="text-align: center; padding: 100px; color: #999">
                  系统监控图表需要集成图表库（如ECharts）
                  <br />
                  可显示API调用趋势、系统负载等
                </div>
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog v-model="userDialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="用户角色" prop="type">
          <el-select v-model="userForm.type" placeholder="请选择角色" style="width: 100%">
            <el-option :value="1" label="货主" />
            <el-option :value="2" label="调度员" />
            <el-option :value="3" label="司机" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号（可选，司机/货主建议填写）" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱（可选，司机/货主建议填写）" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            show-password
            :placeholder="isEdit ? '不填写则不修改密码' : '请输入初始密码'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitUserForm">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  queryUsers,
  createUser,
  updateUser,
  deleteUser as deleteUserApi,
  updateUserStatus,
  adminLogout,
  type UserVO,
  type UserQueryDTO,
  type UserCreateDTO,
  type UserUpdateDTO,
} from '../api';
import { clearAuth } from '../utils/auth';

const router = useRouter();
const activeTab = ref('users');
const dataTab = ref('carType');
const loading = ref(false);
const logLoading = ref(false);
const userList = ref<UserVO[]>([]);
const carTypeList = ref<any[]>([]);
const pricingList = ref<any[]>([]);
const logList = ref<any[]>([]);
const userPage = ref(1);
const userPageSize = ref(10);
const userTotal = ref(0);
const logPage = ref(1);
const logPageSize = ref(10);
const logTotal = ref(0);

const userQuery = reactive({
  role: '',
  keyword: '',
});

const userDialogVisible = ref(false);
const isEdit = ref(false);
const userFormRef = ref();
const userForm = reactive<UserCreateDTO & { userId?: number }>({
  type: 1,
  username: '',
  password: '',
  email: '',
  phone: '',
});

const userFormRules = {
  type: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    {
      validator: (_: any, value: string, callback: (error?: Error) => void) => {
        // 新增时必须填密码，编辑时可空
        if (!isEdit.value && (!value || !value.trim())) {
          callback(new Error('请输入密码'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
  phone: [
    {
      validator: (_: any, value: string, callback: (error?: Error) => void) => {
        // 货主/司机必须提供手机号，调度员可选
        if ((userForm.type === 1 || userForm.type === 3) && (!value || !/^1[3-9]\d{9}$/.test(value))) {
          callback(new Error('请输入有效的手机号'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
  email: [
    {
      validator: (_: any, value: string, callback: (error?: Error) => void) => {
        // 货主/司机必须提供邮箱，调度员可选
        const emailRequired = userForm.type === 1 || userForm.type === 3;
        if (emailRequired && (!value || !/^[^@]+@[^@]+\.[^@]+$/.test(value))) {
          callback(new Error('请输入正确的邮箱'));
        } else if (value && !/^[^@]+@[^@]+\.[^@]+$/.test(value)) {
          callback(new Error('请输入正确的邮箱'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
};

const logQuery = reactive({
  dateRange: [] as string[],
  action: '',
});

const systemStats = reactive({
  apiCalls: 0,
  activeUsers: 0,
  dbConnections: 0,
  cpuUsage: 0,
});

onMounted(() => {
  loadUsers();
  loadSystemStats();
});

const loadUsers = async () => {
  loading.value = true;
  try {
    const dto: UserQueryDTO = {
      current: userPage.value,
      size: userPageSize.value,
    };
    if (userQuery.role) {
      dto.type =
        userQuery.role === 'consignor'
          ? 1
          : userQuery.role === 'dispatcher'
            ? 2
            : 3;
    }
    if (userQuery.keyword) {
      dto.username = userQuery.keyword;
    }
    const result = await queryUsers(dto);
    const pageData = result.data as any;
    userList.value = (pageData.records || []) as UserVO[];
    userTotal.value = pageData.total || 0;
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户列表失败');
  } finally {
    loading.value = false;
  }
};

const loadLogs = async () => {
  logLoading.value = true;
  try {
    // TODO: 调用API获取操作日志
    // const result = await getLogs(logQuery, logPage.value, logPageSize.value);
    // logList.value = result.data.records || [];
    // logTotal.value = result.data.total || 0;
    
    // 模拟数据
    logList.value = [];
    logTotal.value = 0;
  } catch (error: any) {
    ElMessage.error(error.message || '加载日志失败');
  } finally {
    logLoading.value = false;
  }
};

const loadSystemStats = () => {
  // TODO: 调用API获取系统统计
  // 模拟数据
  systemStats.apiCalls = 1234;
  systemStats.activeUsers = 56;
  systemStats.dbConnections = 8;
  systemStats.cpuUsage = 45;
};

const openCreateUserDialog = () => {
  isEdit.value = false;
  userForm.userId = undefined;
  userForm.type = 1;
  userForm.username = '';
  userForm.password = '';
  userForm.email = '';
  userForm.phone = '';
  userDialogVisible.value = true;
};

const openEditUserDialog = (row: UserVO) => {
  isEdit.value = true;
  userForm.userId = row.userId;
  userForm.type = row.type;
  userForm.username = row.username;
  userForm.email = row.email || '';
  userForm.phone = row.phone || '';
  userForm.password = '';
  userDialogVisible.value = true;
};

const submitUserForm = async () => {
  if (!userFormRef.value) return;
  try {
    await userFormRef.value.validate();
  } catch (e) {
    ElMessage.warning('请先修正表单校验错误');
    return;
  }
  try {
    if (isEdit.value && userForm.userId) {
      const dto: UserUpdateDTO = {
        username: userForm.username,
        email: userForm.email,
        phone: userForm.phone,
        password: userForm.password || undefined,
      };
      console.log('admin updateUser payload:', dto, 'userId:', userForm.userId);
      await updateUser(userForm.userId, dto);
      ElMessage.success('用户信息更新成功');
    } else {
      const dto: UserCreateDTO = {
        type: userForm.type,
        username: userForm.username,
        password: userForm.password,
        email: userForm.email,
        phone: userForm.phone,
      };
      console.log('admin createUser payload:', dto);
      await createUser(dto);
      ElMessage.success('用户创建成功');
    }
    userDialogVisible.value = false;
    loadUsers();
  } catch (error: any) {
    console.error('admin create/update user error:', error);
    ElMessage.error(error.message || '保存失败');
  }
};

const toggleUserStatus = async (user: UserVO) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1;
    await updateUserStatus(user.userId, newStatus);
    ElMessage.success('操作成功');
    loadUsers();
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  }
};

const deleteUser = async (userId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteUserApi(userId);
    ElMessage.success('删除成功');
    loadUsers();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};

const showCarTypeDialog = () => {
  ElMessage.info('车型管理功能待实现');
};

const editCarType = (row: any) => {
  ElMessage.info('编辑车型功能待实现');
};

const deleteCarType = (id: number) => {
  ElMessage.info('删除车型功能待实现');
};

const showPricingDialog = () => {
  ElMessage.info('计费规则管理功能待实现');
};

const editPricing = (row: any) => {
  ElMessage.info('编辑计费规则功能待实现');
};

const deletePricing = (id: number) => {
  ElMessage.info('删除计费规则功能待实现');
};

const handleLogout = async () => {
  try {
    await adminLogout();
  } catch {
    // 忽略登出错误
  }
  clearAuth();
  router.push('/login');
};
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

.data-manage {
  padding: 20px;
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

.monitor-chart {
  min-height: 300px;
}
</style>
