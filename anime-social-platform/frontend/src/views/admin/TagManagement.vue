<template>
  <admin-layout>
    <div class="tag-management">
      <h1>标签管理</h1>
      
      <div class="action-bar">
        <el-button type="primary" @click="handleCreate">
          <el-icon><plus /></el-icon> 创建新标签
        </el-button>
      </div>
      
      <!-- 筛选区域 -->
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索标签名称"
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
          v-model="tagTypeFilter"
          placeholder="标签类型"
          clearable
          @change="handleSearch"
          class="filter-select"
        >
          <el-option label="全部类型" value="" />
          <el-option label="帖子标签" value="post" />
          <el-option label="资源标签" value="resource" />
        </el-select>
      </div>
      
      <!-- 标签列表 -->
      <el-table
        v-loading="loading"
        :data="filteredTags"
        style="width: 100%; margin-top: 20px;"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" min-width="180" />
        <el-table-column prop="category" label="分类" width="150" />
        <el-table-column label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getTagTypeClass(scope.row.type)">
              {{ getTagTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usage" label="使用次数" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
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
                type="danger"
                :disabled="scope.row.usage && scope.row.usage > 0"
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 标签表单对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑标签' : '创建标签'"
        width="40%"
      >
        <el-form 
          ref="tagFormRef"
          :model="tagForm"
          :rules="tagRules"
          label-width="100px"
        >
          <el-form-item label="标签名称" prop="name">
            <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-input v-model="tagForm.category" placeholder="请输入分类，如：动画、游戏、音乐等" />
          </el-form-item>
          <el-form-item label="标签类型" prop="type">
            <el-select v-model="tagForm.type" style="width: 100%;">
              <el-option label="帖子标签" value="post" />
              <el-option label="资源标签" value="resource" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitTag">确认</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 删除确认对话框 -->
      <el-dialog
        v-model="deleteDialogVisible"
        title="删除标签"
        width="30%"
      >
        <p>确定要删除标签"{{ currentTag?.name }}"吗？此操作不可撤销。</p>
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
import { ref, computed, onMounted } from 'vue';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import * as adminApi from '@/api/admin';
import { Plus, Search } from '@element-plus/icons-vue';
import AdminLayout from '@/components/AdminLayout.vue';
import type { TagDTO, TagFormData } from '@/types/tag';

// 状态
const loading = ref(false);
const tagList = ref<TagDTO[]>([]);
const searchKeyword = ref('');
const tagTypeFilter = ref('');

// 对话框
const dialogVisible = ref(false);
const isEdit = ref(false);
const currentTag = ref<TagDTO | null>(null);
const deleteDialogVisible = ref(false);
const tagFormRef = ref<FormInstance>();

// 表单数据
const tagForm = ref<TagFormData>({
  name: '',
  category: '',
  type: 'post'
});

// 表单验证规则
const tagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 20, message: '标签名称长度应在1到20个字符之间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请输入分类', trigger: 'blur' },
    { min: 1, max: 20, message: '分类长度应在1到20个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择标签类型', trigger: 'change' }
  ]
};

// 计算属性：过滤后的标签列表
const filteredTags = computed(() => {
  let result = [...tagList.value];
  
  // 关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(tag => 
      tag.name.toLowerCase().includes(keyword) || 
      tag.category.toLowerCase().includes(keyword)
    );
  }
  
  // 类型筛选
  if (tagTypeFilter.value) {
    result = result.filter(tag => tag.type === tagTypeFilter.value);
  }
  
  return result;
});

// 获取标签列表
const fetchTags = async () => {
  loading.value = true;
  console.log('开始获取标签列表，筛选条件:', {
    searchKeyword: searchKeyword.value,
    tagTypeFilter: tagTypeFilter.value
  });
  
  try {
    let response;
    
    if (searchKeyword.value) {
      // 使用搜索接口
      response = await adminApi.searchTags(searchKeyword.value, tagTypeFilter.value || undefined);
    } else {
      // 使用获取所有标签接口
      response = await adminApi.getAllTags(tagTypeFilter.value || undefined);
    }
    
    console.log('标签API响应:', response);
    
    if (response.data && response.data.code === 200) {
      // 处理数据，添加默认的使用次数
      tagList.value = response.data.data.map(tag => ({
        ...tag,
        usage: tag.contentCount || 0 // 使用contentCount作为使用次数
      }));
      console.log('处理后的标签列表:', tagList.value);
    } else {
      console.error('获取标签列表失败:', response.data);
      ElMessage.error((response.data && response.data.message) ?? '获取标签列表失败');
    }
  } catch (error: any) {
    console.error('获取标签列表出错:', error);
    ElMessage.error(error.message ?? '获取标签列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  console.log('搜索条件:', { keyword: searchKeyword.value, type: tagTypeFilter.value });
  // 由于使用计算属性过滤，这里不需要额外操作
};

// 获取标签类型标签样式
const getTagTypeClass = (type: string) => {
  switch (type) {
    case 'post':
      return 'success';
    case 'resource':
      return 'warning';
    case 'user':
      return 'info';
    default:
      return '';
  }
};

// 获取标签类型显示标签
const getTagTypeLabel = (type: string) => {
  switch (type) {
    case 'post':
      return '帖子标签';
    case 'resource':
      return '资源标签';
    case 'user':
      return '用户标签';
    default:
      return '未知类型';
  }
};

// 创建标签
const handleCreate = () => {
  isEdit.value = false;
  tagForm.value = {
    name: '',
    category: '',
    type: 'post'
  };
  dialogVisible.value = true;
};

// 编辑标签
const handleEdit = (tag: TagDTO) => {
  isEdit.value = true;
  currentTag.value = tag;
  tagForm.value = {
    name: tag.name,
    category: tag.category,
    type: tag.type
  };
  dialogVisible.value = true;
};

// 提交表单
const submitTag = async () => {
  if (!tagFormRef.value) return;
  
  await tagFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        loading.value = true;
        let response;
        
        if (isEdit.value && currentTag.value) {
          // 更新标签
          response = await adminApi.updateTag(currentTag.value.id, {
            name: tagForm.value.name,
            type: tagForm.value.type,
            category: tagForm.value.category
          });
        } else {
          // 创建标签
          response = await adminApi.createTag({
            name: tagForm.value.name,
            type: tagForm.value.type,
            category: tagForm.value.category
          });
        }
        
        if (response.data && response.data.code === 200) {
          ElMessage.success(isEdit.value ? '标签更新成功' : '标签创建成功');
          dialogVisible.value = false;
          fetchTags(); // 刷新列表
        } else {
          ElMessage.error((response.data && response.data.message) ?? '操作失败');
        }
      } catch (error: any) {
        ElMessage.error(error.message ?? '操作失败');
      } finally {
        loading.value = false;
      }
    } else {
      console.log('表单验证失败:', fields);
    }
  });
};

// 处理删除
const handleDelete = (tag: TagDTO) => {
  if (tag.usage && tag.usage > 0) {
    ElMessage.warning(`标签"${tag.name}"正在使用中，无法删除`);
    return;
  }
  
  currentTag.value = tag;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = async () => {
  if (!currentTag.value) return;
  
  try {
    loading.value = true;
    const response = await adminApi.deleteTag(currentTag.value.id);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('标签删除成功');
      deleteDialogVisible.value = false;
      fetchTags(); // 刷新列表
    } else {
      ElMessage.error((response.data && response.data.message) ?? '删除失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '删除失败');
  } finally {
    loading.value = false;
    deleteDialogVisible.value = false;
  }
};

onMounted(() => {
  fetchTags();
});
</script>

<style scoped>
.tag-management {
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

.search-input {
  width: 300px;
}

.filter-select {
  width: 150px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 