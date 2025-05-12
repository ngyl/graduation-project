<template>
    <app-layout>
        <!-- 未登录状态的欢迎界面 -->
        <template v-if="!isLoggedIn">
            <div class="welcome-section">
                <div class="welcome-content">
                    <h2>欢迎来到动漫社交平台</h2>
                    <p>这里是动漫爱好者的专属社区</p>

                    <div class="feature-list">
                        <div class="feature-item">
                            <el-icon><ChatDotRound /></el-icon>
                            <h3>参与讨论</h3>
                            <p>与其他动漫爱好者交流心得</p>
                        </div>
                        <div class="feature-item">
                            <el-icon><Star /></el-icon>
                            <h3>分享资源</h3>
                            <p>发现和分享精彩动漫资源</p>
                        </div>
                        <div class="feature-item">
                            <el-icon><User /></el-icon>
                            <h3>结交好友</h3>
                            <p>认识志同道合的朋友</p>
                        </div>
                    </div>
                    <div class="welcome-actions">
                        <el-button type="primary" size="large" class="register-button" @click="$router.push('/register')">立即加入</el-button>
                        <el-button size="large" class="login-button" @click="$router.push('/login')">已有账号？登录</el-button>
                    </div>
                </div>
            </div>
        </template>

        <!-- 已登录状态的内容 -->
        <template v-else>
            <div class="banner">
                <el-carousel height="400px" :interval="4000">
                    <el-carousel-item v-for="anime in featuredAnime" :key="anime.id">
                        <div class="carousel-item">
                            <img :src="anime.images?.large || `https://picsum.photos/1920/1080?random=${anime.id}`" alt="banner" />
                            <div class="carousel-content">
                                <h2>{{ anime.name_cn || anime.name }}</h2>
                                <p>{{ truncateText(anime.summary, 100) || '发现更多精彩内容' }}</p>
                            </div>
                        </div>
                    </el-carousel-item>
                </el-carousel>
            </div>

            <!-- 添加动漫推荐部分 -->
            <div class="content-section">
                <div class="section-title">
                    <h2>热门动漫推荐</h2>
                </div>
                <div v-if="loading.anime" class="loading-container">
                    <el-skeleton :rows="3" animated />
                </div>
                <div v-else-if="hotAnime.length === 0" class="empty-container">
                    <el-empty description="暂无动漫推荐" />
                </div>
                <div v-else class="anime-grid">
                    <el-card v-for="anime in hotAnime" :key="anime.id" class="anime-card" shadow="hover" @click="showAnimeDetail(anime.id)">
                        <div class="anime-image">
                            <img :src="anime.images?.common || anime.images?.medium || `https://picsum.photos/300/400?random=${anime.id}`" :alt="anime.name_cn || anime.name" />
                            <div v-if="anime.rating?.score" class="anime-rating">
                                <el-rate
                                    v-model="anime.rating.score"
                                    disabled
                                    allow-half
                                    :max="10"
                                    :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                                    style="display: inline-block"
                                />
                                <span>{{ anime.rating.score.toFixed(1) }}</span>
                            </div>
                        </div>
                        <div class="anime-content">
                            <h3>{{ anime.name_cn || anime.name }}</h3>
                            <p class="anime-info">
                                <span v-if="anime.date">放送: {{ anime.date }}</span>
                                <span v-if="anime.eps">集数: {{ anime.eps }}集</span>
                            </p>
                            <p>{{ truncateText(anime.summary, 80) }}</p>
                        </div>
                    </el-card>
                </div>
            </div>

            <div class="content-section">
                <div class="section-title">
                    <h2>热门讨论</h2>
                    <el-button type="primary" plain @click="$router.push('/posts')">查看更多</el-button>
                </div>
                <div v-if="loading.posts" class="loading-container">
                    <el-skeleton :rows="3" animated />
                </div>
                <div v-else-if="hotPosts.length === 0" class="empty-container">
                    <el-empty description="暂无热门讨论" />
                </div>
                <div v-else class="post-grid">
                    <el-card v-for="post in hotPosts" :key="post.id" class="post-card" shadow="hover" @click="$router.push(`/posts/${post.id}`)">
                        <template #header>
                            <div class="post-header">
                                <div class="post-user">
                                    <el-avatar :size="32" :src="post.userDTO.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                                    <span class="username">{{ post.userDTO.username }}</span>
                                </div>
                                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                            </div>
                        </template>
                        <div class="post-content">
                            <h3>{{ post.title }}</h3>
                            <p>{{ post.content }}</p>
                        </div>
                        <div class="post-footer">
                            <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
                            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
                            <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
                        </div>
                    </el-card>
                </div>
            </div>

            <!-- 添加资源推荐部分 -->
            <div class="content-section">
                <div class="section-title">
                    <h2>热门资源</h2>
                    <el-button type="primary" plain @click="$router.push('/resources')">查看更多</el-button>
                </div>
                <div v-if="loading.resources" class="loading-container">
                    <el-skeleton :rows="3" animated />
                </div>
                <div v-else-if="hotResources.length === 0" class="empty-container">
                    <el-empty description="暂无热门资源" />
                </div>
                <div v-else class="resources-grid">
                    <el-card v-for="resource in hotResources" :key="resource.id" class="resource-card" shadow="hover" @click="$router.push(`/resources/${resource.id}`)">
                        <div class="resource-image">
                            <template v-if="resource.coverUrl">
                                <img :src="resource.coverUrl" :alt="resource.title" />
                            </template>
                            <div v-else class="file-type-icon">
                                <el-icon :size="40" :color="getIconColor(resource.fileType || 'unknown')">
                                    <component :is="getFileTypeIcon(resource.fileType || 'unknown')"></component>
                                </el-icon>
                                <div class="file-type-label">{{ resource.fileType || '未知类型' }}</div>
                            </div>
                            <div class="resource-type">{{ getResourceType(resource.fileType) }}</div>
                        </div>
                        <div class="resource-content">
                            <h3>{{ resource.title }}</h3>
                            <p>{{ truncateText(resource.description, 80) }}</p>
                            <div class="resource-meta">
                                <span><el-icon><Download /></el-icon> {{ resource.downloadCount || 0 }}</span>
                                <span><el-icon><Star /></el-icon> {{ resource.likeCount }}</span>
                            </div>
                        </div>
                    </el-card>
                </div>
            </div>
        </template>
    </app-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { View, ChatDotRound, Star, User, Download, Picture, VideoPlay, Headset, Document, Collection, More } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'
import { getHotPosts } from '@/api/post'
import { getHotResources } from '@/api/resource'
import { getHotAnime } from '@/api/bangumi'
import type { Post } from '@/types/post'
import type { Resource } from '@/types/resource'
import type { AnimeSubject } from '@/types/bangumi'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 数据和加载状态
const hotPosts = ref<Post[]>([])
const hotResources = ref<Resource[]>([])
const hotAnime = ref<AnimeSubject[]>([])
const featuredAnime = ref<AnimeSubject[]>([])
const loading = ref({
    posts: false,
    resources: false,
    anime: false
})

// 获取热门帖子数据
const fetchHotPosts = async () => {
    loading.value.posts = true
    try {
        const res = await getHotPosts()
        
        if (res.data.code === 200) {
            hotPosts.value = res.data.data.items
        } else {
            ElMessage.error('获取热门讨论失败')
        }
    } catch (error: any) {
        console.error('获取热门讨论失败:', error)
        ElMessage.error('获取热门讨论失败，请稍后重试')
    } finally {
        loading.value.posts = false
    }
}

// 获取热门资源数据
const fetchHotResources = async () => {
    loading.value.resources = true
    try {
        const res = await getHotResources()
        
        if (res.data.code === 200) {
            hotResources.value = res.data.data.items
        } else {
            ElMessage.error('获取热门资源失败')
        }
    } catch (error: any) {
        console.error('获取热门资源失败:', error)
        ElMessage.error('获取热门资源失败，请稍后重试')
    } finally {
        loading.value.resources = false
    }
}

// 获取热门动漫数据
const fetchHotAnime = async () => {
    loading.value.anime = true
    try {
        const res = await getHotAnime()
        
        if (res.data && Array.isArray(res.data.data)) {
            hotAnime.value = res.data.data
            
            // 为轮播图选择前4个动漫
            featuredAnime.value = hotAnime.value.slice(0, 4)
        } else {
            ElMessage.error('获取热门动漫失败')
        }
    } catch (error: any) {
        console.error('获取热门动漫失败:', error)
        ElMessage.error('获取热门动漫失败，请稍后重试')
        
        // 添加默认数据，以防API调用失败
        featuredAnime.value = Array(4).fill(0).map((_, i) => ({
            id: i + 1,
            type: 2,
            name: `动漫示例 ${i + 1}`,
            name_cn: `动漫示例 ${i + 1}`,
            summary: '这是一个示例动漫介绍，当API调用失败时显示',
            nsfw: false,
            locked: false
        }))
    } finally {
        loading.value.anime = false
    }
}

// 显示动漫详情
const showAnimeDetail = (id: number) => {
    window.open(`https://bgm.tv/subject/${id}`, '_blank')
}

// 格式化时间
const formatTime = (time: string) => {
    const date = new Date(time)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    
    // 小于一小时
    if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000)
        return `${minutes}分钟前`
    }
    
    // 小于一天
    if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000)
        return `${hours}小时前`
    }
    
    // 小于7天
    if (diff < 604800000) {
        const days = Math.floor(diff / 86400000)
        return `${days}天前`
    }
    
    // 大于7天，显示具体日期
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 获取资源类型显示文本
const getResourceType = (fileType?: string) => {
    if (!fileType) return '未知'
    
    if (fileType.startsWith('image/')) return '图片'
    if (fileType.startsWith('video/')) return '视频'
    if (fileType.startsWith('audio/')) return '音频'
    if (fileType === 'application/pdf') return 'PDF'
    if (fileType.includes('zip') || fileType.includes('rar') || fileType.includes('7z')) return '压缩包'
    
    return fileType.split('/')[1] || '文件'
}

// 获取文件类型图标
const getFileTypeIcon = (fileType?: string) => {
    // 确保fileType是字符串并转换为小写
    const type = (fileType || '').toLowerCase();
    
    // 检查文件类型并返回相应图标
    if (type.includes('pdf')) {
        return Document;
    } else if (type.includes('doc')) {
        return Document;
    } else if (type.includes('txt')) {
        return Document;
    } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
        return VideoPlay;
    } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
        return Picture;
    } else if (type.includes('zip') || type.includes('rar') || type.includes('7z') || type.includes('application')) {
        return Collection;
    } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
        return Headset;
    } else {
        return More;
    }
};

// 获取文件类型图标颜色
const getIconColor = (fileType?: string) => {
    // 确保fileType是字符串并转换为小写
    const type = (fileType || '').toLowerCase();
    
    // 检查文件类型并返回相应颜色
    if (type.includes('pdf')) {
        return '#E53935';
    } else if (type.includes('doc')) {
        return '#1565C0';
    } else if (type.includes('txt')) {
        return '#607D8B';
    } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
        return '#D81B60';
    } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
        return '#43A047';
    } else if (type.includes('zip') || type.includes('rar') || type.includes('7z') || type.includes('application')) {
        return '#FF9800';
    } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
        return '#7B1FA2';
    } else {
        return '#9E9E9E';
    }
};

// 截断文本
const truncateText = (text: string, maxLength: number) => {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
};

// 页面加载时获取数据
onMounted(() => {
    if (isLoggedIn.value) {
        fetchHotPosts()
        fetchHotResources()
        fetchHotAnime()
    }
})
</script>

<style scoped>
/* Welcome section */
.welcome-section {
    background: var(--bg-secondary);
    padding: 50px 0;
    text-align: center;
    border-radius: var(--border-radius-lg);
    margin-bottom: 30px;
}

.welcome-content {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 20px;
}

.welcome-content h2 {
    font-size: 36px;
    margin-bottom: 10px;
    color: var(--text-primary);
}

.welcome-content p {
    font-size: 18px;
    margin-bottom: 40px;
    color: var(--text-secondary);
}

/* 搜索框样式 */
.search-box {
    max-width: 600px;
    margin: 0 auto 40px;
}

.search-section {
    max-width: 600px;
    margin: 0 auto 30px;
    padding: 0 20px;
}

.feature-list {
    display: flex;
    justify-content: space-around;
    margin-bottom: 40px;
    gap: 20px;
}

.feature-item {
    padding: 20px;
    width: 30%;
    background-color: var(--bg-primary);
    border-radius: var(--border-radius-md);
    box-shadow: var(--shadow-md);
    transition: var(--transition-base);
}

.feature-item:hover {
    transform: translateY(-5px);
    box-shadow: var(--shadow-lg);
}

.feature-item .el-icon {
    font-size: 32px;
    color: var(--primary-color);
    margin-bottom: 15px;
}

.feature-item h3 {
    font-size: 20px;
    margin-bottom: 10px;
    color: var(--text-primary);
}

.feature-item p {
    font-size: 14px;
    color: var(--text-secondary);
}

.welcome-actions {
    display: flex;
    justify-content: center;
    gap: 15px;
}

.register-button {
    background-color: var(--primary-color);
    border-color: var(--primary-color);
    transition: var(--transition-base);
}

.register-button:hover {
    background-color: var(--primary-light);
    border-color: var(--primary-light);
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
}

.login-button {
    color: var(--text-primary);
    border-color: var(--gray-300);
    transition: var(--transition-base);
}

.login-button:hover {
    color: var(--primary-color);
    border-color: var(--primary-color);
    transform: translateY(-2px);
    box-shadow: var(--shadow-sm);
}

/* Banner */
.banner {
    margin-bottom: 20px;
    overflow: hidden;
    border-radius: var(--border-radius-lg);
}

.carousel-item {
    position: relative;
    height: 400px;
}

.carousel-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.carousel-content {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 20px;
    background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0));
    color: var(--text-light);
}

.carousel-content h2 {
    font-size: 24px;
    margin-bottom: 5px;
}

/* Content section */
.content-section {
    margin-bottom: 40px;
}

.section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.section-title h2 {
    font-size: 22px;
    color: var(--text-primary);
    margin: 0;
}

.post-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
}

.post-card {
    transition: var(--transition-base);
    cursor: pointer;
    height: 100%;
}

.post-card:hover {
    transform: translateY(-5px);
    box-shadow: var(--shadow-lg);
}

.post-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.post-user {
    display: flex;
    align-items: center;
    gap: 10px;
}

.username {
    color: var(--primary-color);
    font-weight: 500;
}

.post-time {
    color: var(--text-hint);
    font-size: 14px;
}

.post-content h3 {
    margin-top: 0;
    font-size: 18px;
    margin-bottom: 10px;
    color: var(--text-primary);
}

.post-content p {
    color: var(--text-secondary);
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    line-height: 1.6;
    margin-bottom: 20px;
}

.post-footer {
    display: flex;
    justify-content: space-between;
    color: var(--text-hint);
    font-size: 14px;
}

.post-footer span {
    display: flex;
    align-items: center;
    gap: 5px;
}

/* 资源卡片样式 */
.resources-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 20px;
}

.resource-card {
    transition: var(--transition-base);
    cursor: pointer;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.resource-card:hover {
    transform: translateY(-5px);
    box-shadow: var(--shadow-lg);
}

.resource-image {
    position: relative;
    height: 160px;
    overflow: hidden;
    border-radius: var(--border-radius-md) var(--border-radius-md) 0 0;
}

.resource-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.resource-type {
    position: absolute;
    top: 10px;
    right: 10px;
    background-color: var(--primary-color);
    color: var(--text-light);
    padding: 2px 8px;
    border-radius: var(--border-radius-sm);
    font-size: 12px;
}

.resource-content {
    padding: 15px;
}

.resource-content h3 {
    margin-top: 0;
    font-size: 16px;
    margin-bottom: 8px;
    color: var(--text-primary);
}

.resource-content p {
    color: var(--text-secondary);
    font-size: 14px;
    margin-bottom: 15px;
    line-height: 1.4;
}

.resource-meta {
    display: flex;
    justify-content: space-between;
    color: var(--text-hint);
    font-size: 14px;
}

.resource-meta span {
    display: flex;
    align-items: center;
}

.resource-meta .el-icon {
    margin-right: 5px;
}

/* 加载和空状态容器 */
.loading-container, .empty-container {
    padding: 20px;
}

/* 文件类型图标 */
.file-type-icon {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: var(--bg-secondary);
}

.file-type-label {
    margin-top: 10px;
    font-size: 14px;
    color: var(--text-secondary);
    text-transform: uppercase;
}

/* Responsive */
@media screen and (max-width: 768px) {
    .welcome-content h2 {
        font-size: 28px;
    }
    
    .feature-list {
        flex-direction: column;
        align-items: center;
    }
    
    .feature-item {
        width: 100%;
    }
    
    .welcome-actions {
        flex-direction: column;
    }
    
    .post-grid,
    .resources-grid {
        grid-template-columns: 1fr;
    }
    
    .banner {
        margin: 0 -10px 10px;
    }
    
    .carousel-item {
        height: 300px;
    }
}

/* 动漫卡片样式 */
.anime-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
}

.anime-card {
    transition: var(--transition-base);
    cursor: pointer;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.anime-card:hover {
    transform: translateY(-5px);
    box-shadow: var(--shadow-lg);
}

.anime-image {
    position: relative;
    height: 300px;
    overflow: hidden;
    border-radius: var(--border-radius-md) var(--border-radius-md) 0 0;
}

.anime-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.anime-rating {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 8px;
    background: rgba(0, 0, 0, 0.7);
    color: var(--text-light);
    display: flex;
    align-items: center;
    justify-content: center;
}

.anime-rating .el-rate {
    transform: scale(0.8);
    margin-right: 5px;
}

.anime-content {
    padding: 15px;
    display: flex;
    flex-direction: column;
    flex-grow: 1;
}

.anime-content h3 {
    margin-top: 0;
    font-size: 16px;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--text-primary);
}

.anime-info {
    display: flex;
    justify-content: space-between;
    color: var(--text-secondary);
    font-size: 12px;
    margin-bottom: 10px;
}

.anime-content p {
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.4;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
}

/* 自定义Element Plus组件样式 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--primary-color);
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: var(--primary-color);
}

:deep(.el-rate__icon) {
  font-size: 16px;
  margin-right: 2px;
}

:deep(.el-carousel__indicator:hover button) {
  background-color: var(--primary-light);
}

:deep(.el-carousel__indicator.is-active button) {
  background-color: var(--primary-color);
}

:deep(.el-tabs__item) {
  color: var(--text-secondary);
}

:deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

:deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
}

:deep(.el-button--primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.el-button--primary:hover),
:deep(.el-button--primary:focus) {
  background-color: var(--primary-light);
  border-color: var(--primary-light);
}

:deep(.el-button--default:hover),
:deep(.el-button--default:focus) {
  color: var(--primary-color);
  border-color: var(--primary-color);
}
</style> 