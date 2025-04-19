<template>
    <div class="home-container">
        <div class="header">
            <div class="header-content">
                <div class="logo">
                    <h1>动漫社交平台</h1>
                </div>
                <div class="nav">
                    <el-menu mode="horizontal" :ellipsis="false" class="nav-menu">
                        <el-menu-item index="1" @click="$router.push('/')">首页</el-menu-item>
                        <el-menu-item index="2" @click="$router.push('/posts')">社区</el-menu-item>
                        <!-- <el-menu-item index="3" @click="$router.push('/resources')">资源</el-menu-item> -->
                        <!-- <el-menu-item index="4" @click="$router.push('/events')">活动</el-menu-item> -->
                        <div class="flex-grow"></div>
                        <!-- 未登录状态 -->
                        <template v-if="!isLoggedIn">
                            <el-menu-item index="5">
                                <el-button type="primary" @click="$router.push('/login')">登录</el-button>
                            </el-menu-item>
                            <el-menu-item index="6">
                                <el-button @click="$router.push('/register')">注册</el-button>
                            </el-menu-item>
                        </template>
                        <!-- 已登录状态 -->
                        <template v-else>
                            <el-menu-item index="5" class="user-menu">
                                <el-dropdown>
                                    <span class="user-dropdown">
                                        <el-avatar :size="32" :src="userInfo.avatar || defaultAvatar" />
                                        <span class="username-text">{{ userInfo.username }}</span>
                                    </span>
                                    <template #dropdown>
                                        <el-dropdown-menu>
                                            <el-dropdown-item @click="handleDropdownClick(`/profile/${userStore.userInfo.id}`)">个人中心</el-dropdown-item>
                                            <el-dropdown-item @click="handleDropdownClick('/favorites')">我的收藏</el-dropdown-item>
                                            <el-dropdown-item @click="handleDropdownClick('/messages')">消息通知</el-dropdown-item>
                                            <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                                        </el-dropdown-menu>
                                    </template>
                                </el-dropdown>
                            </el-menu-item>
                        </template>
                    </el-menu>
                </div>
            </div>
        </div>

        <div class="main-content">
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
                        <el-button type="primary" plain>查看更多</el-button>
                    </div>
                    <div class="post-grid">
                        <el-card v-for="i in 6" :key="i" class="post-card" shadow="hover">
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
            </template>
        </div>

        <div class="footer">
            <div class="footer-content">
                <div class="footer-section">
                    <h3>关于我们</h3>
                    <p>动漫社交平台致力于为动漫爱好者提供优质的交流平台，打造一个充满活力的动漫社区。</p>
                </div>
                <div class="footer-section">
                    <h3>联系我们</h3>
                    <p>邮箱：contact@example.com</p>
                </div>
            </div>
            <div class="footer-bottom">
                <p>© 2024 动漫社交平台. All rights reserved.</p>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { View, ChatDotRound, Star, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)

const handleLogout = async () => {
    try {
        await userStore.logoutUser()
        ElMessage.success('退出登录成功')
        router.push('/')
    } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '退出登录失败')
    }
}

// 处理下拉菜单点击事件
const handleDropdownClick = (path: string) => {
    router.push(path)
}
</script>

<style scoped>
.home-container {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f5f7fa;
}

.header {
    background-color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    position: sticky;
    top: 0;
    z-index: 100;
}

.header-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    padding: 0 20px;
}

.logo {
    padding: 15px 0;
}

.logo h1 {
    color: #2c3e50;
    font-size: 24px;
    margin: 0;
}

.nav {
    flex: 1;
}

.nav-menu {
    border-bottom: none;
    justify-content: flex-end;
}

.nav-menu :deep(.el-menu-item) {
    font-size: 16px;
    padding: 0 20px;
}

.user-menu {
    margin-left: 20px;
}

.user-dropdown {
    display: flex;
    align-items: center;
    cursor: pointer;
}

.username-text {
    margin-left: 8px;
    color: #2c3e50;
}

.main-content {
    flex: 1;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    width: 100%;
}

.banner {
    margin-bottom: 40px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.carousel-item {
    position: relative;
    height: 100%;
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
    right: 0;
    padding: 40px;
    background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
    color: white;
}

.carousel-content h2 {
    margin: 0;
    font-size: 32px;
    font-weight: 600;
}

.carousel-content p {
    margin: 10px 0 0;
    font-size: 18px;
    opacity: 0.9;
}

.content-section {
    margin-bottom: 40px;
}

.section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 10px;
}

.section-title h2 {
    margin: 0;
    color: #2c3e50;
    font-size: 24px;
    font-weight: 600;
}

.post-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    padding: 10px;
}

.post-card {
    border-radius: 12px;
    transition: all 0.3s ease;
}

.post-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
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
    font-weight: 500;
    color: #2c3e50;
}

.post-time {
    color: #999;
    font-size: 14px;
}

.post-content {
    padding: 15px 0;
}

.post-content h3 {
    margin: 0 0 10px;
    font-size: 18px;
    color: #2c3e50;
}

.post-content p {
    margin: 0 0 15px;
    color: #666;
    font-size: 14px;
    line-height: 1.6;
}

.post-image {
    width: 100%;
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 15px;
}

.post-image img {
    width: 100%;
    height: 200px;
    object-fit: cover;
}

.post-footer {
    display: flex;
    gap: 20px;
    color: #666;
    font-size: 14px;
}

.post-footer span {
    display: flex;
    align-items: center;
    gap: 4px;
}

.footer {
    background-color: #fff;
    padding: 40px 0 20px;
    margin-top: 40px;
}

.footer-content {
    max-width: 1200px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 40px;
    padding: 0 20px;
}

.footer-section h3 {
    color: #2c3e50;
    margin: 0 0 15px;
    font-size: 18px;
}

.footer-section p {
    color: #666;
    line-height: 1.6;
    margin: 0;
}

.footer-section ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.footer-section ul li {
    margin-bottom: 10px;
}

.footer-section ul li a {
    color: #666;
    text-decoration: none;
    transition: color 0.3s ease;
}

.footer-section ul li a:hover {
    color: #409EFF;
}

.footer-bottom {
    max-width: 1200px;
    margin: 20px auto 0;
    padding: 20px 20px 0;
    border-top: 1px solid #eee;
    text-align: center;
    color: #999;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .header-content,
    .main-content,
    .footer-content {
        max-width: 960px;
    }

    .post-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 768px) {
    .header-content {
        flex-direction: column;
        padding: 10px;
    }

    .nav-menu {
        justify-content: center;
    }

    .main-content {
        padding: 15px;
    }

    .post-grid {
        grid-template-columns: 1fr;
    }

    .carousel-content h2 {
        font-size: 24px;
    }

    .carousel-content p {
        font-size: 16px;
    }

    .footer-content {
        grid-template-columns: 1fr;
        gap: 20px;
    }
}

@media (max-width: 480px) {
    .logo h1 {
        font-size: 20px;
    }

    .nav-menu :deep(.el-menu-item) {
        padding: 0 10px;
        font-size: 14px;
    }

    .carousel-content {
        padding: 20px;
    }

    .carousel-content h2 {
        font-size: 20px;
    }

    .section-title {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }

    .post-image img {
        height: 160px;
    }
}

/* 添加未登录状态的样式 */
.welcome-section {
    min-height: 600px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    margin-bottom: 40px;
    color: white;
}

.welcome-content {
    text-align: center;
    max-width: 800px;
}

.welcome-content h2 {
    font-size: 36px;
    font-weight: 600;
    margin-bottom: 16px;
}

.welcome-content > p {
    font-size: 18px;
    opacity: 0.9;
    margin-bottom: 48px;
}

.feature-list {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 30px;
    margin-bottom: 48px;
}

.feature-item {
    padding: 20px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    backdrop-filter: blur(10px);
}

.feature-item .el-icon {
    font-size: 32px;
    margin-bottom: 16px;
}

.feature-item h3 {
    font-size: 20px;
    margin-bottom: 8px;
}

.feature-item p {
    font-size: 14px;
    opacity: 0.8;
}

.welcome-actions {
    display: flex;
    gap: 20px;
    justify-content: center;
}

.welcome-actions .el-button {
    padding: 12px 36px;
    font-size: 16px;
}

/* 响应式设计补充 */
@media (max-width: 768px) {
    .feature-list {
        grid-template-columns: 1fr;
        gap: 20px;
    }

    .welcome-content h2 {
        font-size: 28px;
    }

    .welcome-content > p {
        font-size: 16px;
    }

    .welcome-actions {
        flex-direction: column;
    }

    .welcome-actions .el-button {
        width: 100%;
    }
}

@media (max-width: 480px) {
    .welcome-section {
        min-height: 400px;
        padding: 30px 15px;
    }

    .welcome-content h2 {
        font-size: 24px;
    }

    .feature-item {
        padding: 15px;
    }

    .feature-item h3 {
        font-size: 18px;
    }
}
</style> 