<template>
    <div class="header">
        <div class="header-content">
            <div class="logo">
                <h1>动漫社交平台</h1>
            </div>
            <div class="nav">
                <el-menu mode="horizontal" :ellipsis="false" class="nav-menu" :default-active="activeIndex">
                    <el-menu-item index="/" @click="navigateTo('/')">首页</el-menu-item>
                    <el-menu-item index="/posts" @click="navigateTo('/posts')">社区</el-menu-item>
                    <el-menu-item index="/resources" @click="navigateTo('/resources')">资源</el-menu-item>
                    <el-menu-item index="/events" @click="navigateTo('/events')">活动</el-menu-item>
                    
                    <!-- 搜索图标 -->
                    <el-menu-item index="/search" @click="showSearchInput = !showSearchInput" class="search-icon-item">
                        <el-tooltip content="搜索" placement="bottom" effect="light">
                            <el-icon size="20px" class="search-icon"><Search /></el-icon>
                        </el-tooltip>
                    </el-menu-item>
                    
                    <div class="flex-grow"></div>
                    
                    <!-- 搜索输入框 - 仅在点击搜索图标后显示 -->
                    <div class="search-container" v-show="showSearchInput" ref="searchContainerRef">
                        <el-input
                            v-model="searchKeyword"
                            placeholder="搜索动漫、帖子、资源..."
                            clearable
                            @keyup.enter="handleSearch"
                            ref="searchInputRef"
                        >
                            <template #append>
                                <el-button @click="handleSearch">搜索</el-button>
                            </template>
                        </el-input>
                    </div>
                    
                    <!-- 未登录状态 -->
                    <template v-if="!isLoggedIn">
                        <el-menu-item index="/login">
                            <el-button type="primary" @click="navigateTo('/login')">登录</el-button>
                        </el-menu-item>
                        <el-menu-item index="/register">
                            <el-button @click="navigateTo('/register')">注册</el-button>
                        </el-menu-item>
                    </template>
                    <!-- 已登录状态 -->
                    <template v-else>
                        <!-- 消息通知图标 -->
                        <el-menu-item index="/messages" v-if="isLoggedIn">
                            <el-badge :value="unreadMessages" :max="99" :hidden="unreadMessages === 0">
                                <el-icon size="24px" @click="navigateTo('/messages')">
                                    <Bell />
                                </el-icon>
                            </el-badge>
                        </el-menu-item>
                        
                        <!-- 管理员入口 -->
                        <el-menu-item index="/admin" v-if="isAdmin">
                            <el-button type="success" @click="navigateTo('/admin')">管理控制台</el-button>
                        </el-menu-item>
                        
                        <!-- 用户菜单 -->
                        <el-menu-item index="/profile" class="user-menu">
                            <el-dropdown trigger="click">
                                <span class="user-dropdown">
                                    <el-avatar :size="32" :src="userInfo.avatar || defaultAvatar" />
                                    <span class="username-text">{{ userInfo.username }}</span>
                                </span>
                                <template #dropdown>
                                    <el-dropdown-menu>
                                        <el-dropdown-item @click="handleDropdownClick(`/profile/${userInfo.id}`)">个人中心</el-dropdown-item>
                                        <el-dropdown-item @click="handleDropdownClick('/favorites')">我的收藏</el-dropdown-item>
                                        <el-dropdown-item @click="handleDropdownClick('/friends')">我的好友</el-dropdown-item>
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
</template>

<script setup lang="ts">
import { computed, onMounted, ref, nextTick, watch, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { Bell, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const messageStore = useMessageStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const unreadMessages = ref(0)
const searchKeyword = ref('')
const showSearchInput = ref(false)
const searchInputRef = ref()
const searchContainerRef = ref()

// 监听搜索框显示状态，显示时自动聚焦
watch(showSearchInput, async (newVal) => {
    if (newVal) {
        await nextTick()
        searchInputRef.value?.focus()
    }
})

// 点击页面其他区域关闭搜索框
const handleClickOutside = (event: MouseEvent) => {
    const target = event.target as HTMLElement
    const searchIcon = document.querySelector('.search-icon-item')
    const searchContainer = searchContainerRef.value

    if (
        showSearchInput.value && 
        searchContainer && 
        !searchContainer.contains(target) && 
        searchIcon && 
        !searchIcon.contains(target)
    ) {
        showSearchInput.value = false
    }
}

// 添加全局点击事件监听
onMounted(() => {
    // 消息初始化逻辑保持不变
    if (isLoggedIn.value) {
        messageStore.initialize()
        // 获取未读消息数量
        unreadMessages.value = messageStore.unreadTotal
        
        // 监听未读消息变化
        const unwatch = computed(() => messageStore.unreadTotal)
        unreadMessages.value = unwatch.value
    }

    // 添加点击事件监听
    document.addEventListener('click', handleClickOutside)
})

// 移除事件监听
onBeforeUnmount(() => {
    document.removeEventListener('click', handleClickOutside)
})

// 计算当前的激活菜单项
const activeIndex = computed(() => {
    const path = route.path
    if (path === '/') return '/'
    return `/${path.split('/')[1]}`
})

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.isAdmin)
const userInfo = computed(() => userStore.userInfo)

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
    
    // 搜索后隐藏搜索框
    showSearchInput.value = false
}

// 通用导航方法
const navigateTo = (path: string) => {
    try {
        // 直接导航，身份验证由全局路由守卫处理
        router.push(path).catch(err => {
            console.error('导航错误:', err)
        })
    } catch (error) {
        console.error('导航过程中发生错误:', error)
    }
}

// 处理退出登录
const handleLogout = async () => {
    try {
        await userStore.logoutUser()
        ElMessage.success('退出登录成功')
        // 使用catch处理可能的导航错误
        router.push('/').catch(err => {
            console.error('导航错误:', err)
        })
    } catch (error) {
        console.error('退出登录失败:', error)
        ElMessage.error('退出登录失败')
    }
}

// 处理下拉菜单点击事件
const handleDropdownClick = (path: string) => {
    navigateTo(path);
}
</script>

<style scoped>
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
    flex-shrink: 0;
    padding: 5px 0;
}

.logo h1 {
    margin: 0;
    font-size: 20px;
    color: #409eff;
}

.nav {
    flex: 1;
    display: flex;
}

.nav-menu {
    width: 100%;
    border-bottom: none;
}

.flex-grow {
    flex-grow: 1;
}

.search-icon-item {
    position: relative;
}

.search-icon {
    color: #409eff;
    transition: transform 0.3s ease;
}

.search-icon:hover {
    transform: scale(1.2);
}

/* 搜索容器样式 */
.search-container {
    position: absolute;
    top: 60px;
    left: 50%;
    transform: translateX(-50%);
    width: 400px;
    background-color: white;
    padding: 12px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    z-index: 100;
    transition: all 0.3s ease;
}

.user-menu {
    margin-left: 10px;
}

.user-dropdown {
    display: flex;
    align-items: center;
    cursor: pointer;
}

.username-text {
    margin-left: 5px;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 手机端响应式 */
@media screen and (max-width: 768px) {
    .header-content {
        flex-direction: column;
        padding: 10px;
    }
    
    .logo {
        margin-bottom: 10px;
    }
    
    .nav-menu {
        justify-content: center;
    }
    
    .username-text {
        display: none;
    }
    
    .search-container {
        width: 90%;
        left: 50%;
        transform: translateX(-50%);
    }
}
</style> 