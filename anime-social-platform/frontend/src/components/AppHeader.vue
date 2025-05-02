<template>
    <div class="header">
        <div class="header-content">
            <div class="logo">
                <router-link to="/" class="logo-link">
                    <div class="logo-icon">
                        <svg viewBox="0 0 24 24" class="logo-svg">
                            <path d="M12,2L4,12l8,10l8-10L12,2z" />
                            <circle cx="12" cy="9" r="2" />
                        </svg>
                    </div>
                    <h1 class="logo-text">二次元社区</h1>
                </router-link>
            </div>
            <div class="nav">
                <el-menu mode="horizontal" :ellipsis="false" class="nav-menu" :default-active="activeIndex">
                    <el-menu-item index="/" @click="navigateTo('/')">
                        <div class="nav-item-content">
                            <el-icon><House /></el-icon>
                            <span>首页</span>
                        </div>
                    </el-menu-item>
                    <el-menu-item index="/posts" @click="navigateTo('/posts')">
                        <div class="nav-item-content">
                            <el-icon><ChatDotSquare /></el-icon>
                            <span>社区</span>
                        </div>
                    </el-menu-item>
                    <el-menu-item index="/resources" @click="navigateTo('/resources')">
                        <div class="nav-item-content">
                            <el-icon><Files /></el-icon>
                            <span>资源</span>
                        </div>
                    </el-menu-item>
                    <el-menu-item index="/events" @click="navigateTo('/events')">
                        <div class="nav-item-content">
                            <el-icon><Calendar /></el-icon>
                            <span>活动</span>
                        </div>
                    </el-menu-item>
                    
                    <!-- 搜索图标 -->
                    <el-menu-item index="/search" @click="openSearchDialog" class="search-icon-item">
                        <el-tooltip content="搜索" placement="bottom" effect="light">
                            <el-icon size="20px" class="search-icon"><Search /></el-icon>
                        </el-tooltip>
                    </el-menu-item>
                    
                    <div class="flex-grow"></div>
                    
                    <!-- 未登录状态 -->
                    <template v-if="!isLoggedIn">
                        <el-menu-item index="/login">
                            <el-button type="primary" @click="navigateTo('/login')" class="anime-btn">登录</el-button>
                        </el-menu-item>
                        <el-menu-item index="/register">
                            <el-button @click="navigateTo('/register')" class="anime-btn-outline">注册</el-button>
                        </el-menu-item>
                    </template>
                    <!-- 已登录状态 -->
                    <template v-else>
                        <!-- 消息通知图标 -->
                        <el-menu-item index="/messages" v-if="isLoggedIn" class="notification-menu-item">
                            <el-badge :value="unreadMessages" :max="99" :hidden="unreadMessages === 0" class="notification-badge">
                                <el-icon size="24px" @click="navigateTo('/messages')" class="notification-icon">
                                    <Bell />
                                </el-icon>
                            </el-badge>
                        </el-menu-item>
                        
                        <!-- 管理员入口 -->
                        <el-menu-item index="/admin" v-if="isAdmin">
                            <el-button type="success" @click="navigateTo('/admin')" class="admin-btn">管理控制台</el-button>
                        </el-menu-item>
                        
                        <!-- 用户菜单 -->
                        <el-menu-item index="/profile" class="user-menu">
                            <el-dropdown trigger="click">
                                <span class="user-dropdown">
                                    <el-avatar :size="36" :src="userInfo.avatar || defaultAvatar" class="user-avatar" />
                                    <span class="username-text">{{ userInfo.username }}</span>
                                </span>
                                <template #dropdown>
                                    <el-dropdown-menu class="anime-dropdown">
                                        <el-dropdown-item @click="handleDropdownClick(`/profile/${userInfo.id}`)">
                                            <div class="dropdown-item-content">
                                                <el-icon><User /></el-icon>
                                                <span>个人中心</span>
                                            </div>
                                        </el-dropdown-item>
                                        <el-dropdown-item @click="handleDropdownClick('/favorites')">
                                            <div class="dropdown-item-content">
                                                <el-icon><Star /></el-icon>
                                                <span>我的收藏</span>
                                            </div>
                                        </el-dropdown-item>
                                        <el-dropdown-item @click="handleDropdownClick('/friends')">
                                            <div class="dropdown-item-content">
                                                <el-icon><UserFilled /></el-icon>
                                                <span>我的好友</span>
                                            </div>
                                        </el-dropdown-item>
                                        <el-dropdown-item @click="handleDropdownClick('/messages')">
                                            <div class="dropdown-item-content">
                                                <el-icon><Message /></el-icon>
                                                <span>消息通知</span>
                                            </div>
                                        </el-dropdown-item>
                                        <el-dropdown-item divided @click="handleLogout">
                                            <div class="dropdown-item-content">
                                                <el-icon><SwitchButton /></el-icon>
                                                <span>退出登录</span>
                                            </div>
                                        </el-dropdown-item>
                                    </el-dropdown-menu>
                                </template>
                            </el-dropdown>
                        </el-menu-item>
                    </template>
                </el-menu>
            </div>
        </div>
        <!-- 添加装饰性波浪底部 -->
        <div class="header-wave">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 100">
                <path fill="rgba(255,107,129,0.1)" d="M0,32L48,37.3C96,43,192,53,288,58.7C384,64,480,64,576,58.7C672,53,768,43,864,42.7C960,43,1056,53,1152,53.3C1248,53,1344,43,1392,37.3L1440,32L1440,0L1392,0C1344,0,1248,0,1152,0C1056,0,960,0,864,0C768,0,672,0,576,0C480,0,384,0,288,0C192,0,96,0,48,0L0,0Z"></path>
            </svg>
        </div>
    </div>
    
    <!-- 搜索弹窗 -->
    <el-dialog
        v-model="searchDialogVisible"
        title="搜索"
        width="500px"
        :show-close="true"
        :destroy-on-close="false"
        class="search-dialog"
    >
        <div class="search-dialog-content">
            <el-input
                v-model="searchKeyword"
                placeholder="搜索动漫、帖子、资源..."
                clearable
                @keyup.enter="handleSearch"
                ref="searchInputRef"
                class="search-dialog-input"
            >
                <template #prefix>
                    <el-icon><Search /></el-icon>
                </template>
                <template #append>
                    <el-button @click="handleSearch">搜索</el-button>
                </template>
            </el-input>
            
            <div class="search-suggestions" v-if="searchSuggestions.length > 0">
                <div class="suggestions-title">热门搜索</div>
                <div class="suggestions-list">
                    <div
                        v-for="(suggestion, index) in searchSuggestions"
                        :key="index"
                        class="suggestion-item"
                        @click="selectSuggestion(suggestion)"
                    >
                        <el-icon><TopRight /></el-icon>
                        <span>{{ suggestion }}</span>
                    </div>
                </div>
            </div>
            
            <div class="search-history" v-if="searchHistory.length > 0">
                <div class="history-header">
                    <div class="history-title">搜索历史</div>
                    <el-button text @click="clearSearchHistory">清空</el-button>
                </div>
                <div class="history-list">
                    <div
                        v-for="(item, index) in searchHistory"
                        :key="index"
                        class="history-item"
                        @click="selectSuggestion(item)"
                    >
                        <div class="history-item-content">
                            <el-icon><Timer /></el-icon>
                            <span>{{ item }}</span>
                        </div>
                        <el-icon @click.stop="removeHistoryItem(index)"><Close /></el-icon>
                    </div>
                </div>
            </div>
        </div>
    </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, nextTick, watch, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { Bell, Search, House, ChatDotSquare, Files, Calendar, User, Star, UserFilled, Message, SwitchButton, Timer, Close, TopRight } from '@element-plus/icons-vue'
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

// 搜索弹窗相关
const searchDialogVisible = ref(false)
const searchSuggestions = ref([
    '人气动漫推荐',
    '最新cosplay',
    '夏日祭活动',
    '二次元手办',
    '漫展资讯',
    '动漫周边'
])
const searchHistory = ref<string[]>([])

// 打开搜索弹窗
const openSearchDialog = () => {
    searchDialogVisible.value = true
    // 加载历史记录
    loadSearchHistory()
    // 下一个更新周期聚焦搜索框
    nextTick(() => {
        searchInputRef.value?.focus()
    })
}

// 从本地存储加载搜索历史
const loadSearchHistory = () => {
    try {
        const history = localStorage.getItem('searchHistory')
        if (history) {
            searchHistory.value = JSON.parse(history)
        }
    } catch (e) {
        console.error('加载搜索历史失败:', e)
        searchHistory.value = []
    }
}

// 保存搜索历史到本地存储
const saveSearchHistory = () => {
    try {
        localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
    } catch (e) {
        console.error('保存搜索历史失败:', e)
    }
}

// 清空搜索历史
const clearSearchHistory = () => {
    searchHistory.value = []
    saveSearchHistory()
}

// 从历史记录中移除项目
const removeHistoryItem = (index: number) => {
    searchHistory.value.splice(index, 1)
    saveSearchHistory()
}

// 选择搜索建议
const selectSuggestion = (suggestion: string) => {
    searchKeyword.value = suggestion
    handleSearch()
}

// 添加到搜索历史
const addToSearchHistory = (keyword: string) => {
    // 避免重复
    const index = searchHistory.value.indexOf(keyword)
    if (index > -1) {
        searchHistory.value.splice(index, 1)
    }
    // 添加到头部
    searchHistory.value.unshift(keyword)
    // 限制历史记录数量
    if (searchHistory.value.length > 10) {
        searchHistory.value = searchHistory.value.slice(0, 10)
    }
    saveSearchHistory()
}

// 处理搜索
const handleSearch = () => {
    if (!searchKeyword.value.trim()) return
    
    // 添加到搜索历史
    addToSearchHistory(searchKeyword.value)
    
    router.push({
        path: '/search',
        query: {
            keyword: searchKeyword.value,
            tab: 'all'
        }
    })
    
    // 关闭搜索弹窗
    searchDialogVisible.value = false
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

    // 加载搜索历史
    loadSearchHistory()
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
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    position: sticky;
    top: 0;
    z-index: 100;
    position: relative;
}

.header-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    padding: 0 20px;
}

/* 动漫风格Logo */
.logo {
    flex-shrink: 0;
    padding: 5px 0;
}

.logo-link {
    display: flex;
    align-items: center;
    text-decoration: none;
}

.logo-icon {
    width: 32px;
    height: 32px;
    margin-right: 8px;
    color: var(--primary-color);
}

.logo-svg {
    fill: currentColor;
    animation: pulse 2s infinite;
}

.logo-text {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    font-family: 'ZCOOL QingKe HuangYou', sans-serif;
    letter-spacing: 1px;
}

.nav {
    flex: 1;
    display: flex;
}

.nav-menu {
    width: 100%;
    border-bottom: none;
}

/* 导航栏项目样式 */
.nav-item-content {
    display: flex;
    align-items: center;
    gap: 5px;
    transition: transform 0.3s;
}

.nav-item-content:hover {
    transform: translateY(-2px);
}

.el-menu-item {
    transition: all 0.3s;
}

.el-menu-item.is-active {
    color: var(--primary-color) !important;
    border-bottom-color: var(--primary-color) !important;
}

/* 搜索按钮动画 */
.search-icon {
    transition: transform 0.3s;
}

.search-icon:hover {
    transform: scale(1.15);
}

.search-container {
    width: 300px;
    margin-right: 15px;
    animation: slideIn 0.3s;
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateX(10px);
    }
    to {
        opacity: 1;
        transform: translateX(0);
    }
}

/* 自定义通知图标 */
.notification-menu-item {
    position: relative;
}

.notification-badge {
    margin-right: 15px;
}

.notification-icon {
    transition: transform 0.3s;
}

.notification-icon:hover {
    transform: rotate(15deg);
}

/* 二次元风格按钮 */
.anime-btn {
    background-color: var(--primary-color);
    border: none;
    border-radius: 20px;
    padding: 8px 18px;
    font-weight: 500;
    box-shadow: 0 4px 8px rgba(255, 107, 129, 0.2);
    transition: all 0.3s;
}

.anime-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(255, 107, 129, 0.3);
    background-color: var(--primary-light);
}

.anime-btn-outline {
    background-color: transparent;
    border: 2px solid var(--primary-color);
    color: var(--primary-color);
    border-radius: 20px;
    padding: 6px 16px;
    font-weight: 500;
    transition: all 0.3s;
}

.anime-btn-outline:hover {
    background-color: var(--primary-color);
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(255, 107, 129, 0.2);
}

.admin-btn {
    background-color: var(--success-color);
    border: none;
    border-radius: 20px;
    padding: 8px 18px;
    font-weight: 500;
    box-shadow: 0 4px 8px rgba(109, 213, 176, 0.2);
    transition: all 0.3s;
}

.admin-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(109, 213, 176, 0.3);
    background-color: #7eddbf;
}

/* 用户头像和下拉菜单 */
.user-dropdown {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 3px 8px;
    border-radius: 20px;
    transition: all 0.3s;
    background-color: rgba(255, 107, 129, 0.05);
}

.user-dropdown:hover {
    background-color: rgba(255, 107, 129, 0.1);
}

.user-avatar {
    border: 2px solid rgba(255, 107, 129, 0.3);
    transition: all 0.3s;
}

.user-dropdown:hover .user-avatar {
    transform: scale(1.05);
}

.username-text {
    margin-left: 8px;
    font-weight: 500;
    font-size: 14px;
    transition: all 0.3s;
}

/* 下拉菜单项样式 */
.dropdown-item-content {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 2px 0;
}

.anime-dropdown .el-dropdown-menu__item:hover {
    color: var(--primary-color);
    background-color: rgba(255, 107, 129, 0.05);
}

.flex-grow {
    flex-grow: 1;
}

/* 装饰性波浪底部 */
.header-wave {
    position: absolute;
    bottom: -20px;
    left: 0;
    width: 100%;
    overflow: hidden;
    line-height: 0;
    transform: rotate(180deg);
}

.header-wave svg {
    position: relative;
    display: block;
    width: 100%;
    height: 20px;
}

/* 搜索弹窗样式 */
.search-dialog :deep(.el-dialog__header) {
    padding: 15px 20px;
    margin-right: 0;
    border-bottom: 1px solid rgba(var(--gray-200-rgb, 233, 236, 239), 0.5);
}

.search-dialog :deep(.el-dialog__body) {
    padding: 20px;
}

.search-dialog-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.search-dialog-input {
    width: 100%;
}

.search-dialog-input :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.search-dialog-input :deep(.el-input__inner) {
    height: 48px;
    font-size: 16px;
}

/* 搜索建议 */
.search-suggestions {
    margin-top: 10px;
}

.suggestions-title, .history-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-secondary);
    margin-bottom: 10px;
}

.suggestions-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.suggestion-item {
    padding: 6px 12px;
    background-color: rgba(var(--secondary-color-rgb, 115, 103, 240), 0.08);
    border-radius: 16px;
    font-size: 13px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 5px;
    transition: all 0.3s;
}

.suggestion-item:hover {
    background-color: rgba(var(--secondary-color-rgb, 115, 103, 240), 0.15);
    transform: translateY(-2px);
}

/* 搜索历史 */
.search-history {
    margin-top: 10px;
}

.history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.history-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    border-radius: 8px;
    background-color: rgba(var(--gray-100-rgb, 248, 249, 250), 0.5);
    transition: all 0.3s;
}

.history-item:hover {
    background-color: rgba(var(--gray-200-rgb, 233, 236, 239), 0.5);
}

.history-item-content {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
}
</style> 