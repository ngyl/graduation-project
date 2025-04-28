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
                        <el-button type="primary" size="large" @click="$router.push('/register')">立即加入</el-button>
                        <el-button size="large" @click="$router.push('/login')">已有账号？登录</el-button>
                    </div>
                </div>
            </div>
        </template>

        <!-- 已登录状态的内容 -->
        <template v-else>
            <div class="banner">
                <el-carousel height="400px" :interval="4000">
                    <el-carousel-item v-for="item in 4" :key="item">
                        <div class="carousel-item">
                            <img :src="`https://picsum.photos/1920/1080?random=${item}`" alt="banner" />
                            <div class="carousel-content">
                                <h2>热门动漫推荐</h2>
                                <p>发现更多精彩内容</p>
                            </div>
                        </div>
                    </el-carousel-item>
                </el-carousel>
            </div>

            <div class="content-section">
                <div class="section-title">
                    <h2>热门讨论</h2>
                    <el-button type="primary" plain @click="$router.push('/posts')">查看更多</el-button>
                </div>
                <div class="post-grid">
                    <el-card v-for="i in 6" :key="i" class="post-card" shadow="hover" @click="$router.push(`/posts/${i}`)">
                        <template #header>
                            <div class="post-header">
                                <div class="post-user">
                                    <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                                    <span class="username">用户{{ i }}</span>
                                </div>
                                <span class="post-time">2小时前</span>
                            </div>
                        </template>
                        <div class="post-content">
                            <h3>这是一个讨论标题</h3>
                            <p>这是讨论的内容，可以包含图片、文字等...</p>
                            <div class="post-image">
                                <img :src="`https://picsum.photos/300/200?random=${i}`" alt="帖子内容" />
                            </div>
                        </div>
                        <div class="post-footer">
                            <span><el-icon><View /></el-icon> 123</span>
                            <span><el-icon><ChatDotRound /></el-icon> 45</span>
                            <span><el-icon><Star /></el-icon> 67</span>
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
                <div class="resources-grid">
                    <el-card v-for="i in 4" :key="i" class="resource-card" shadow="hover" @click="$router.push(`/resources/${i}`)">
                        <div class="resource-image">
                            <img :src="`https://picsum.photos/300/200?random=${i+10}`" alt="资源封面" />
                            <div class="resource-type">视频</div>
                        </div>
                        <div class="resource-content">
                            <h3>热门动漫资源 {{ i }}</h3>
                            <p>这是一段资源描述，介绍资源内容和特点...</p>
                            <div class="resource-meta">
                                <span><el-icon><Download /></el-icon> 1.2k</span>
                                <span><el-icon><Star /></el-icon> 560</span>
                            </div>
                        </div>
                    </el-card>
                </div>
            </div>
        </template>
    </app-layout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { View, ChatDotRound, Star, User, Download } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

const isLoggedIn = computed(() => userStore.isLoggedIn)

// 处理搜索
const handleSearch = () => {
    if (!searchKeyword.value.trim()) return
    
    router.push({
        path: '/search',
        query: {
            keyword: searchKeyword.value,
            tab: 'all'
        }
    })
}
</script>

<style scoped>
/* Welcome section */
.welcome-section {
    min-height: 60vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
}

.welcome-content {
    text-align: center;
    max-width: 800px;
    padding: 0 20px;
}

.welcome-content h2 {
    font-size: 36px;
    color: #333;
    margin-bottom: 20px;
}

.welcome-content > p {
    font-size: 18px;
    color: #666;
    margin-bottom: 30px;
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
    flex-wrap: wrap;
    justify-content: center;
    gap: 30px;
    margin-bottom: 40px;
}

.feature-item {
    flex: 1;
    min-width: 200px;
    max-width: 250px;
    text-align: center;
    padding: 20px;
    background-color: #fff;
    border-radius: 10px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s, box-shadow 0.3s;
}

.feature-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.feature-item .el-icon {
    font-size: 36px;
    color: #409eff;
    margin-bottom: 10px;
}

.feature-item h3 {
    margin: 10px 0;
    font-size: 18px;
    color: #333;
}

.feature-item p {
    color: #666;
    font-size: 14px;
}

.welcome-actions {
    display: flex;
    gap: 20px;
    justify-content: center;
}

/* Banner */
.banner {
    margin: 0 -20px 20px;
}

.carousel-item {
    height: 400px;
    overflow: hidden;
    position: relative;
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
    padding: 30px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
    color: #fff;
}

.carousel-content h2 {
    font-size: 28px;
    margin-bottom: 10px;
}

/* Content section */
.content-section {
    margin-top: 40px;
    padding: 0 20px;
}

.section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.section-title h2 {
    font-size: 24px;
    color: #333;
    margin: 0;
}

.post-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
}

.post-card {
    transition: transform 0.3s;
    cursor: pointer;
}

.post-card:hover {
    transform: translateY(-5px);
}

.post-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.post-user {
    display: flex;
    align-items: center;
}

.post-user .username {
    margin-left: 10px;
    font-weight: 500;
}

.post-time {
    color: #999;
    font-size: 12px;
}

.post-content h3 {
    margin-top: 0;
    font-size: 18px;
    margin-bottom: 10px;
}

.post-content p {
    color: #666;
    margin-bottom: 15px;
}

.post-image {
    margin-bottom: 15px;
    border-radius: 4px;
    overflow: hidden;
}

.post-image img {
    width: 100%;
    display: block;
}

.post-footer {
    display: flex;
    justify-content: space-between;
    color: #999;
    font-size: 14px;
}

.post-footer span {
    display: flex;
    align-items: center;
}

.post-footer .el-icon {
    margin-right: 5px;
}

/* 资源卡片样式 */
.resources-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.resource-card {
    transition: transform 0.3s;
    cursor: pointer;
}

.resource-card:hover {
    transform: translateY(-5px);
}

.resource-image {
    position: relative;
    height: 160px;
    overflow: hidden;
    border-radius: 4px 4px 0 0;
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
    background-color: rgba(64, 158, 255, 0.9);
    color: white;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
}

.resource-content {
    padding: 15px;
}

.resource-content h3 {
    margin-top: 0;
    font-size: 16px;
    margin-bottom: 8px;
}

.resource-content p {
    color: #666;
    font-size: 14px;
    margin-bottom: 15px;
    line-height: 1.4;
}

.resource-meta {
    display: flex;
    justify-content: space-between;
    color: #999;
    font-size: 14px;
}

.resource-meta span {
    display: flex;
    align-items: center;
}

.resource-meta .el-icon {
    margin-right: 5px;
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
</style> 