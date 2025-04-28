<template>
  <admin-layout>
    <div class="event-management">
      <h1>活动管理</h1>
      
      <div class="action-bar">
        <el-button type="primary" @click="handleCreate">
          <el-icon><plus /></el-icon> 创建新活动
        </el-button>
      </div>
      
      <!-- 筛选区域 -->
      <div class="filter-bar">
        <el-select
          v-model="statusFilter"
          placeholder="活动状态"
          clearable
          @change="handleFilter"
          class="filter-select"
        >
          <el-option label="全部活动" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="已结束" value="finished" />
          <el-option label="未开始" value="upcoming" />
          <el-option label="已下线" value="offline" />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY/MM/DD"
          value-format="YYYY-MM-DD"
          @change="handleFilter"
          class="date-picker"
        />
      </div>
      
      <!-- 活动列表 -->
      <el-table
        v-loading="loading"
        :data="eventList"
        style="width: 100%; margin-top: 20px;"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdByUsername" label="创建者" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="活动状态" width="100">
          <template #default="scope">
            <el-tag :type="getEventStatusTag(scope.row)">
              {{ getEventStatusLabel(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上线状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已上线' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button 
                size="small" 
                @click="handleEdit(scope.row)"
              >
                编辑
              </el-button>
              <el-button 
                size="small" 
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                @click="handleStatusChange(scope.row)"
              >
                {{ scope.row.status === 1 ? '下线' : '上线' }}
              </el-button>
              <el-button 
                size="small" 
                type="danger"
                @click="handleDelete(scope.row)"
              >
                删除
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
      
      <!-- 活动表单对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑活动' : '创建活动'"
        width="50%"
      >
        <el-form 
          ref="eventFormRef"
          :model="eventForm"
          :rules="eventRules"
          label-width="100px"
        >
          <el-form-item label="活动标题" prop="title">
            <el-input v-model="eventForm.title" placeholder="请输入活动标题" />
          </el-form-item>
          <el-form-item label="活动描述" prop="description">
            <el-input 
              v-model="eventForm.description" 
              type="textarea" 
              :rows="4"
              placeholder="请输入活动描述" 
            />
          </el-form-item>
          <el-form-item label="活动时间" prop="time">
            <el-date-picker
              v-model="eventForm.time"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              :default-time="defaultTime"
              style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="上线状态" prop="status">
            <el-radio-group v-model="eventForm.status">
              <el-radio :label="1">上线</el-radio>
              <el-radio :label="0">下线</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEvent">确认</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 删除确认对话框 -->
      <el-dialog
        v-model="deleteDialogVisible"
        title="删除活动"
        width="30%"
      >
        <p>确定要删除活动"{{ currentEvent?.title }}"吗？此操作不可撤销。</p>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="deleteDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="confirmDelete">确认删除</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import * as adminApi from '@/api/admin';
import { Plus } from '@element-plus/icons-vue';
import AdminLayout from '@/components/AdminLayout.vue';

// 定义事件接口
interface Event {
  id: number;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  status: number; // 0下线,1上线
  createdBy: number;
  createdByUsername: string;
}

// 状态
const loading = ref(false);
const eventList = ref<Event[]>([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const statusFilter = ref('');
const dateRange = ref([]);

// 对话框
const dialogVisible = ref(false);
const isEdit = ref(false);
const currentEvent = ref<Event | null>(null);
const deleteDialogVisible = ref(false);
const eventFormRef = ref<FormInstance>();

// 表单数据
const eventForm = reactive({
  id: 0,
  title: '',
  description: '',
  time: [] as string[],
  status: 1
});

// 默认时间
const defaultTime = [
  new Date(2000, 0, 1, 0, 0, 0),
  new Date(2000, 0, 1, 23, 59, 59)
];

// 表单验证规则
const eventRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 3, max: 100, message: '标题长度应在3到100个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入活动描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度应在10到2000个字符之间', trigger: 'blur' }
  ],
  time: [
    { 
      type: 'array', 
      required: true, 
      len: 2, 
      message: '请选择活动开始和结束时间', 
      trigger: 'change' 
    }
  ]
});

// 获取活动列表
const fetchEvents = async () => {
  loading.value = true;
  try {
    // 添加调试信息
    console.log('获取活动列表，筛选条件:', { 
      statusFilter: statusFilter.value,
      dateRange: dateRange.value,
      page: page.value,
      pageSize: pageSize.value
    });
    
    const response = await adminApi.getAllEvents(page.value, pageSize.value);
    
    // 打印完整响应，帮助调试
    console.log('活动API完整响应:', response);
    
    if (response.data && response.data.code === 200) {
      const data = response.data.data as any;
      
      // 添加调试信息
      console.log('活动数据详细内容:', data);
      
      // 特殊处理：检查data是否为null或undefined
      if (!data) {
        console.warn('返回的活动数据为空');
        eventList.value = [];
        total.value = 0;
        return;
      }
      
      let rawEvents: any[] = [];
      
      if (data && Array.isArray(data)) {
        // 返回格式是数组
        console.log('处理数组格式数据');
        rawEvents = data;
        total.value = data.length;
      } else if (data && data.list && Array.isArray(data.list)) {
        // 返回格式是 {list: [...], total: number}
        console.log('处理list格式数据');
        rawEvents = data.list;
        total.value = data.total || 0;
      } else if (data && data.items && Array.isArray(data.items)) {
        // 返回格式是 {items: [...], total: number}
        console.log('处理items格式数据');
        rawEvents = data.items;
        total.value = data.total || 0;
      } else if (data && data.content && Array.isArray(data.content)) {
        // Spring Data JPA格式 {content: [...], totalElements: number}
        console.log('处理content格式数据');
        rawEvents = data.content;
        total.value = data.totalElements || 0;
      } else if (typeof data === 'object' && Object.keys(data).length > 0) {
        // 如果是对象且有键，尝试查找第一个数组类型的属性
        console.log('尝试自动检测数组属性');
        const arrayProps = Object.keys(data).filter(key => Array.isArray(data[key]));
        if (arrayProps.length > 0) {
          const arrayProp = arrayProps[0];
          console.log(`找到数组属性: ${arrayProp}`);
          rawEvents = data[arrayProp];
          
          // 尝试查找可能的总数属性
          const possibleTotalProps = ['total', 'totalCount', 'totalElements', 'count', 'size'];
          const totalProp = possibleTotalProps.find(prop => typeof data[prop] === 'number');
          total.value = totalProp ? data[totalProp] : rawEvents.length;
        } else {
          rawEvents = [];
          total.value = 0;
          console.error('无法在返回数据中找到数组属性');
        }
      } else {
        rawEvents = [];
        total.value = 0;
        console.error('返回的活动数据格式不正确:', data);
      }
      
      // 处理每一个活动项，确保正确的属性名
      let processedEvents = rawEvents.map(event => {
        return {
          id: event.id || 0,
          title: event.title || '未知活动',
          description: event.description || '',
          startTime: event.startTime || event.start_time || '',
          endTime: event.endTime || event.end_time || '',
          status: event.status || 0,
          createdBy: event.createdBy || event.created_by || 0,
          createdByUsername: event.createdByUsername || event.creatorName || '未知创建者'
        };
      });
      
      // 前端筛选：根据活动状态进行筛选
      if (statusFilter.value) {
        console.log(`应用活动状态筛选: ${statusFilter.value}`);
        const now = new Date();
        
        processedEvents = processedEvents.filter(event => {
          const startTime = new Date(event.startTime);
          const endTime = new Date(event.endTime);
          
          switch (statusFilter.value) {
            case 'active': // 进行中
              return event.status === 1 && now >= startTime && now <= endTime;
            case 'finished': // 已结束
              return now > endTime;
            case 'upcoming': // 未开始
              return event.status === 1 && now < startTime;
            case 'offline': // 已下线
              return event.status === 0;
            default:
              return true;
          }
        });
      }
      
      // 前端筛选：根据日期范围进行筛选
      if (dateRange.value && dateRange.value.length === 2) {
        console.log(`应用日期筛选: ${dateRange.value[0]} 至 ${dateRange.value[1]}`);
        const startDate = new Date(dateRange.value[0]);
        const endDate = new Date(dateRange.value[1]);
        endDate.setHours(23, 59, 59, 999); // 设置为当天结束时间
        
        processedEvents = processedEvents.filter(event => {
          const eventStartTime = new Date(event.startTime);
          // 活动开始时间在筛选范围内
          return eventStartTime >= startDate && eventStartTime <= endDate;
        });
      }
      
      eventList.value = processedEvents;
      
      // 打印最终结果
      console.log('最终处理后的活动列表:', eventList.value);
      console.log('总数:', eventList.value.length);
      total.value = eventList.value.length; // 更新过滤后的总数
    } else {
      const errorMsg = (response.data && response.data.message) ?? '获取活动列表失败';
      console.error('API响应错误:', errorMsg);
      ElMessage.error(errorMsg);
    }
  } catch (error: any) {
    console.error('获取活动列表出错:', error);
    ElMessage.error(error.message ?? '获取活动列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取活动状态标签
const getEventStatusLabel = (event: Event) => {
  const now = new Date();
  const startTime = new Date(event.startTime);
  const endTime = new Date(event.endTime);
  
  if (event.status === 0) {
    return '已下线';
  } else if (now < startTime) {
    return '未开始';
  } else if (now >= startTime && now <= endTime) {
    return '进行中';
  } else {
    return '已结束';
  }
};

// 获取活动状态标签样式
const getEventStatusTag = (event: Event) => {
  const now = new Date();
  const startTime = new Date(event.startTime);
  const endTime = new Date(event.endTime);
  
  if (event.status === 0) {
    return 'info';
  } else if (now < startTime) {
    return 'warning';
  } else if (now >= startTime && now <= endTime) {
    return 'success';
  } else {
    return 'danger';
  }
};

// 筛选处理
const handleFilter = () => {
  page.value = 1;
  fetchEvents();
};

// 处理页面大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchEvents();
};

// 处理页码变化
const handleCurrentChange = (p: number) => {
  page.value = p;
  fetchEvents();
};

// 创建活动
const handleCreate = () => {
  isEdit.value = false;
  eventForm.id = 0;
  eventForm.title = '';
  eventForm.description = '';
  eventForm.time = [];
  eventForm.status = 1;
  dialogVisible.value = true;
};

// 编辑活动
const handleEdit = (event: Event) => {
  isEdit.value = true;
  currentEvent.value = event;
  eventForm.id = event.id;
  eventForm.title = event.title;
  eventForm.description = event.description;
  eventForm.time = [event.startTime, event.endTime];
  eventForm.status = event.status;
  dialogVisible.value = true;
};

// 提交表单
const submitEvent = async () => {
  if (!eventFormRef.value) return;
  
  await eventFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    try {
      loading.value = true;
      
      const eventData = {
        title: eventForm.title,
        description: eventForm.description,
        startTime: eventForm.time[0],
        endTime: eventForm.time[1],
        status: eventForm.status
      };
      
      let response;
      if (isEdit.value) {
        response = await adminApi.updateEvent(eventForm.id, eventData as any);
      } else {
        response = await adminApi.createEvent(eventData as any);
      }
      
      if (response.data && response.data.code === 200) {
        ElMessage.success(isEdit.value ? '活动更新成功' : '活动创建成功');
        dialogVisible.value = false;
        fetchEvents();
      } else {
        ElMessage.error((response.data && response.data.message) ?? '操作失败');
      }
    } catch (error: any) {
      ElMessage.error(error.message ?? '操作失败');
    } finally {
      loading.value = false;
    }
  });
};

// 处理活动状态变更
const handleStatusChange = async (event: Event) => {
  try {
    const newStatus = event.status === 1 ? 0 : 1;
    const response = await adminApi.updateEventStatus(event.id, newStatus);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success(newStatus === 1 ? '活动已上线' : '活动已下线');
      
      // 更新本地数据
      const index = eventList.value.findIndex(e => e.id === event.id);
      if (index !== -1) {
        eventList.value[index].status = newStatus;
      }
    } else {
      ElMessage.error((response.data && response.data.message) ?? '操作失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '操作失败');
  }
};

// 处理删除
const handleDelete = (event: Event) => {
  currentEvent.value = event;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = async () => {
  if (!currentEvent.value) return;
  
  try {
    const response = await adminApi.deleteEvent(currentEvent.value.id);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('活动删除成功');
      
      // 更新本地数据
      const index = eventList.value.findIndex(e => e.id === currentEvent.value!.id);
      if (index !== -1) {
        eventList.value.splice(index, 1);
      }
      
      deleteDialogVisible.value = false;
    } else {
      ElMessage.error((response.data && response.data.message) ?? '删除失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '删除失败');
  }
};

onMounted(() => {
  fetchEvents();
});
</script>

<style scoped>
.event-management {
  max-width: 1400px;
  margin: 0 auto;
}

h1 {
  margin-bottom: 30px;
  color: #303133;
  border-bottom: 1px solid #EBEEF5;
  padding-bottom: 15px;
}

.action-bar {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
}

.filter-select {
  width: 150px;
}

.date-picker {
  width: 350px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 