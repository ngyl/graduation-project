<template>
    <div class="app-container">
        <app-header />
        
        <div class="main-content">
            <slot></slot>
        </div>
        
        <div class="footer" v-if="showFooter">
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
import AppHeader from './AppHeader.vue'

defineProps({
    showFooter: {
        type: Boolean,
        default: true
    }
})
</script>

<style scoped>
.app-container {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f5f7fa;
}

.main-content {
    flex: 1;
    width: 100%;
    max-width: 1200px;
    margin: 20px auto;
    padding: 0 20px;
}

.footer {
    background-color: #fff;
    padding: 40px 0 20px;
    margin-top: 40px;
    border-top: 1px solid #e6e6e6;
}

.footer-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    padding: 0 20px;
}

.footer-section {
    flex: 1;
    min-width: 250px;
    margin-bottom: 20px;
    padding-right: 20px;
}

.footer-section h3 {
    font-size: 18px;
    margin-bottom: 15px;
    color: #333;
}

.footer-section p {
    color: #666;
    line-height: 1.5;
}

.footer-bottom {
    text-align: center;
    padding: 20px 0 0;
    margin-top: 20px;
    border-top: 1px solid #e6e6e6;
}

.footer-bottom p {
    color: #999;
    font-size: 14px;
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
    .footer-content {
        flex-direction: column;
    }
    
    .footer-section {
        width: 100%;
        padding-right: 0;
    }
    
    .main-content {
        padding: 0 10px;
        margin: 10px auto;
    }
}
</style> 