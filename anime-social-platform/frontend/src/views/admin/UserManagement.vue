<template>
  <admin-layout>
    <div class="user-management">
      <h1>用户管理</h1>
      
      <!-- 搜索区域 -->
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或简介"
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><search /></el-icon>
            </el-button>
          </template>
        </el-input>
        
        <el-select
          v-model="userType"
          placeholder="用户类型"
          clearable
          @change="handleSearch"
        >
          <el-option label="全部用户" value="" />
          <el-option label="普通用户" value="user" />
          <el-option label="管理员" value="admin" />
        </el-select>
      </div>
      
      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%; margin-top: 20px;"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="bio" label="简介" show-overflow-tooltip min-width="200" />
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
        <el-table-column label="类型" width="100">
          <template #default="scope">
            <el-tag type="warning" v-if="scope.row.isAdmin">管理员</el-tag>
            <el-tag v-else>普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button-group>
              <el-button 
                size="small" 
                @click="$router.push(`/profile/${scope.row.id}`)"
              >
                查看
              </el-button>
              <el-button 
                size="small" 
                :type="scope.row.status === 1 ? 'danger' : 'success'"
                @click="handleStatusChange(scope.row)"
                :disabled="scope.row.isAdmin"
              >
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="small"
                :type="scope.row.isAdmin ? 'warning' : 'info'"
                @click="handleAdminRoleChange(scope.row)"
                v-if="canManageAdmin"
              >
                {{ scope.row.isAdmin ? '取消管理员' : '设为管理员' }}
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <!-- 确认对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="30%"
      >
        <span>{{ dialogContent }}</span>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmAction">
              确认
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import * as adminApi from '@/api/admin';
import { Search } from '@element-plus/icons-vue';
import type { UserDTO } from '@/types/user';
import AdminLayout from '@/components/AdminLayout.vue';
import { useUserStore } from '@/stores/user';

// 状态
const loading = ref(false);
const userList = ref<UserDTO[]>([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchKeyword = ref('');
const userType = ref('');

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref('');
const dialogContent = ref('');
const currentUser = ref<UserDTO | null>(null);
const actionType = ref<'status' | 'admin'>('status');

// 当前登录用户信息
const userStore = useUserStore();
const canManageAdmin = computed(() => userStore.isAdmin);

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true;
  console.log('开始获取用户列表，筛选条件:', {
    searchKeyword: searchKeyword.value,
    userType: userType.value
  });

  try {
    let response;
    
    // 根据筛选条件选择合适的API调用
    if (userType.value === 'admin') {
      console.log('正在获取所有管理员用户');
      response = await adminApi.getAllAdmins();
    } else if (searchKeyword.value) {
      console.log('正在搜索用户, 关键词:', searchKeyword.value);
      response = await adminApi.searchUsers(searchKeyword.value);
    } else {
      console.log('正在获取所有用户');
      response = await adminApi.getAllUsers();
    }
    
    console.log('用户API响应:', response);
    
    if (response.data && response.data.code === 200) {
      let userData = response.data.data;
      
      // 如果不是通过管理员筛选器获取的，但需要筛选用户类型
      if (userType.value && userType.value !== 'admin') {
        console.log('根据用户类型筛选返回结果:', userType.value);
        userData = userData.filter(user => 
          (userType.value === 'user' && !user.isAdmin) || 
          (userType.value === 'admin' && user.isAdmin)
        );
      }
      
      userList.value = userData;
      total.value = userData.length;
      console.log('处理后的用户列表:', userList.value);
    } else {
      console.error('获取用户列表失败:', response.data);
      ElMessage.error((response.data && response.data.message) ?? '获取用户列表失败');
    }
  } catch (error: any) {
    console.error('获取用户列表出错:', error);
    ElMessage.error(error.message ?? '获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  page.value = 1;
  fetchUsers();
};

// 处理页面大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchUsers();
};

// 处理页码变化
const handleCurrentChange = (current: number) => {
  page.value = current;
  fetchUsers();
};

// 处理用户状态变更
const handleStatusChange = (user: UserDTO) => {
  currentUser.value = user;
  actionType.value = 'status';
  
  const newStatus = user.status === 1 ? 0 : 1;
  dialogTitle.value = newStatus === 1 ? '启用用户' : '禁用用户';
  dialogContent.value = newStatus === 1 
    ? `确认要启用用户 ${user.username} 吗？`
    : `确认要禁用用户 ${user.username} 吗？这将使其无法登录系统。`;
  
  dialogVisible.value = true;
};

// 处理管理员角色变更
const handleAdminRoleChange = (user: UserDTO) => {
  currentUser.value = user;
  actionType.value = 'admin';
  
  dialogTitle.value = user.isAdmin ? '取消管理员权限' : '设置为管理员';
  dialogContent.value = user.isAdmin
    ? `确认要取消 ${user.username} 的管理员权限吗？`
    : `确认要将 ${user.username} 设置为管理员吗？这将授予其系统管理权限。`;
  
  dialogVisible.value = true;
};

// 确认操作
const confirmAction = async () => {
  if (!currentUser.value) {
    dialogVisible.value = false;
    return;
  }
  
  try {
    loading.value = true;
    let response;
    
    if (actionType.value === 'status') {
      // 更新用户状态
      const newStatus = currentUser.value.status === 1 ? 0 : 1;
      const userId = currentUser.value.id || 0; // 确保userId不为undefined
      response = await adminApi.updateUserStatus(userId, newStatus);
      
      if (response.data && response.data.code === 200) {
        ElMessage.success(newStatus === 1 ? '用户已启用' : '用户已禁用');
        
        // 更新本地列表中的用户状态
        const userIndex = userList.value.findIndex(u => u.id === currentUser.value!.id);
        if (userIndex !== -1) {
          userList.value[userIndex].status = newStatus;
        }
      } else {
        ElMessage.error((response.data && response.data.message) ?? '操作失败');
      }
    } else if (actionType.value === 'admin') {
      // 更新管理员权限
      const isAdmin = !currentUser.value.isAdmin;
      const userId = currentUser.value.id || 0; // 确保userId不为undefined
      response = await adminApi.updateUserAdminRole(userId, isAdmin);
      
      if (response.data && response.data.code === 200) {
        ElMessage.success(isAdmin ? '已设置为管理员' : '已取消管理员权限');
        
        // 更新本地列表中的管理员状态
        const userIndex = userList.value.findIndex(u => u.id === currentUser.value!.id);
        if (userIndex !== -1) {
          userList.value[userIndex].isAdmin = isAdmin;
        }
      } else {
        ElMessage.error((response.data && response.data.message) ?? '操作失败');
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '操作失败');
  } finally {
    loading.value = false;
    dialogVisible.value = false;
  }
};

// 初始化
onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-management {
  max-width: 1400px;
  margin: 0 auto;
}

.search-container {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

h1 {
  margin-bottom: 30px;
  color: #303133;
  border-bottom: 1px solid #EBEEF5;
  padding-bottom: 15px;
}
</style> 