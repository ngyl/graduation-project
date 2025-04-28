<template>
  <app-layout>
    <div class="resource-list-container">
      <div class="page-header">
        <h1>动漫资源</h1>
        <div class="filter-bar">
          <el-select 
            v-model="filter.tagId" 
            placeholder="标签" 
            clearable 
            :loading="tagsLoading" 
            class="filter-select"
          >
            <el-option
              v-for="tag in resourceTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
          <el-select v-model="filter.sort" placeholder="排序" class="filter-select">
            <el-option label="最新发布" value="latest" />
            <el-option label="最多收藏" value="likes" />
            <el-option label="最高评分" value="rating" />
          </el-select>
        </div>
      </div>

      <div class="content-section" v-loading="loading">
        <el-row :gutter="20">
          <el-col 
            v-for="resource in resources" 
            :key="resource.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            class="resource-col"
          >
            <resource-card :resource="resource" />
          </el-col>
        </el-row>

        <!-- 无结果状态 -->
        <div v-if="!loading && resources.length === 0" class="empty-state">
          <el-empty description="暂无资源" />
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="total"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>

      <!-- 新增资源按钮 -->
      <el-button 
        v-if="userStore.isLoggedIn" 
        type="primary"
        class="upload-button"
        @click="goToUpload"
      >
        <el-icon><Plus /></el-icon>
        发布新资源
      </el-button>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { Plus } from '@element-plus/icons-vue';
import ResourceCard from '@/components/ResourceCard.vue';
import type { Resource } from '@/types/resource.ts';
import AppLayout from '@/components/AppLayout.vue';
import { getResources } from '@/api/resource';
import { getAllTags } from '@/api/tag';
import type { TagDTO } from '@/types/tag';

const router = useRouter();
const userStore = useUserStore();

// 资源列表状态
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const resources = ref<Resource[]>([]);

// 标签数据
const allTags = ref<TagDTO[]>([]);
const tagsLoading = ref(false);

// 筛选条件
const filter = ref({
  tagId: null as number | null,
  type: '',
  sort: 'latest'
});

// 计算属性：资源标签
const resourceTags = computed(() => {
  return allTags.value.filter(tag => tag.type === 'resource');
});

// 监听筛选条件变化
watch(() => filter.value, () => {
  currentPage.value = 1;
    loadResources();
}, { deep: true });

// 组件挂载时加载数据
onMounted(() => {
  loadTags();
  loadResources();
});

// 加载标签
const loadTags = async () => {
  try {
    tagsLoading.value = true;
    const response = await getAllTags();
    if (response.data.code === 200) {
      allTags.value = response.data.data;
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载标签失败');
  } finally {
    tagsLoading.value = false;
  }
};

// 加载资源列表
const loadResources = async () => {
  try {
    loading.value = true;
    const response = await getResources(
      currentPage.value,
      pageSize.value,
      filter.value.tagId || undefined,
      filter.value.sort
    );

    if (response.data.code === 200) {
      // 根据API返回格式处理数据
      resources.value = response.data.data.items || [];
      total.value = response.data.data.total || 0;
      
      // 如果有类型筛选，前端再次过滤
      if (filter.value.type) {
        resources.value = resources.value.filter(
          resource => resource.fileType === filter.value.type
        );
      }
    } else {
      ElMessage.error(response.data.message || '获取资源列表失败');
      resources.value = [];
      total.value = 0;
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取资源列表失败');
    resources.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理分页
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1; // 修改每页数量时重置为第一页
    loadResources();
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
    loadResources();
};

// 跳转到上传页面
const goToUpload = () => {
  router.push('/resource-upload');
};
</script>

<style scoped>
.resource-list-container {
  max-width: var(--container-width);
  margin: 0 auto;
  padding: var(--spacing-4);
}

.page-header {
  margin-bottom: var(--spacing-8);
}

.page-header h1 {
  font-size: var(--font-size-3xl);
  color: var(--text-primary);
  margin-bottom: var(--spacing-4);
}

.filter-bar {
  display: flex;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-4);
  flex-wrap: wrap;
}

.filter-select {
  width: 150px;
}

.content-section {
  margin-bottom: var(--spacing-8);
}

.resource-col {
  margin-bottom: var(--spacing-6);
}

.empty-state {
  padding: var(--spacing-16) 0;
  text-align: center;
}

.pagination-container {
  margin-top: var(--spacing-8);
  display: flex;
  justify-content: center;
}

.upload-button {
  position: fixed;
  right: var(--spacing-6);
  bottom: var(--spacing-6);
  box-shadow: var(--shadow-lg);
  z-index: 10;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    gap: var(--spacing-2);
  }
  
  .filter-select {
    width: 100%;
  }
  
  .upload-button {
    right: var(--spacing-4);
    bottom: var(--spacing-4);
  }
}
</style> 