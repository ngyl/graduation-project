<template>
  <app-layout>
    <div class="favorites-container">
      <h1 class="page-title">我的收藏</h1>
      
      <el-empty v-if="favorites.length === 0" description="暂无收藏内容"></el-empty>
      
      <el-row :gutter="20" v-else>
        <el-col v-for="favorite in favorites" :key="favorite.id" :xs="24" :sm="12" :md="8" :lg="6">
          <resource-card :resource="favorite" />
        </el-col>
      </el-row>
      
      <el-pagination
        v-if="total > pageSize"
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
        class="pagination"
      ></el-pagination>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserFavoritesWithPagination, removeFavorite } from '../api/favorite';
import { useUserStore } from '../stores/user';
import { useRouter } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import ResourceCard from '@/components/ResourceCard.vue';
import type { Resource } from '@/types/resource';
import { ApiResponseUtil } from '@/types/api';

const userStore = useUserStore();
const router = useRouter();
const favorites = ref<Resource[]>([]);
const total = ref<number>(0);
const pageSize = ref<number>(12);
const currentPage = ref<number>(1);
const loading = ref<boolean>(false);

// 加载收藏列表
const loadFavorites = async () => {
  if (!userStore.user) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loading.value = true;
  try {
    const res = await getUserFavoritesWithPagination(
      userStore.user.id, 
      currentPage.value, 
      pageSize.value
    );

    // 详细日志，输出完整的响应对象
    console.log('完整响应:', res);
    console.log('响应数据:', res.data);
    
    if (ApiResponseUtil.isSuccess(res.data)) {
      favorites.value = res.data.data.items;
      total.value = res.data.data.total;
    } else {
      ElMessage.error(res.data.message || '获取收藏列表失败');
    }
  } catch (error) {
    console.error('获取收藏列表失败', error);
    ElMessage.error('获取收藏列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 取消收藏
const cancelFavorite = async (favorite: Resource) => {
  if (!userStore.user) {
    ElMessage.warning('请先登录');
    return;
  }
  
  ElMessageBox.confirm('确定要取消收藏该资源吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await removeFavorite(favorite.id, userStore.user!.id);
      if (ApiResponseUtil.isSuccess(res.data)) {
        ElMessage.success('取消收藏成功');
        // 重新加载收藏列表
        loadFavorites();
      } else {
        ElMessage.error(res.data.message || '取消收藏失败');
      }
    } catch (error) {
      console.error('取消收藏失败', error);
      ElMessage.error('取消收藏失败，请稍后重试');
    }
  }).catch(() => {
    // 取消操作，不做处理
  });
};

// 页码变化处理
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadFavorites();
};

// 组件挂载时加载数据
onMounted(() => {
  loadFavorites();
});
</script>

<style scoped>
.favorites-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 