# 用户标签API使用示例

本文档提供了用户标签相关API的使用示例，帮助前端开发人员理解如何与后端进行交互。

## API端点列表

### 1. 获取用户标签

**请求**:
```
GET /api/tags/user/{userId}
```

**响应**:
```json
{
  "userId": 1,
  "username": "anime_lover",
  "tags": [
    {
      "id": 5,
      "name": "热血",
      "category": "类型",
      "type": "user"
    },
    {
      "id": 8,
      "name": "冒险",
      "category": "类型", 
      "type": "user"
    }
  ],
  "tagCount": 2
}
```

### 2. 更新用户标签

**请求**:
```
PUT /api/tags/user/{userId}
Content-Type: application/json

{
  "tagIds": [5, 8, 12]
}
```

**响应**:
```json
{
  "userId": 1,
  "username": "anime_lover",
  "tags": [
    {
      "id": 5,
      "name": "热血",
      "category": "类型",
      "type": "user"
    },
    {
      "id": 8,
      "name": "冒险",
      "category": "类型", 
      "type": "user"
    },
    {
      "id": 12,
      "name": "奇幻",
      "category": "类型",
      "type": "user" 
    }
  ],
  "tagCount": 3
}
```

### 3. 获取热门用户标签

**请求**:
```
GET /api/tags/hot/user?limit=5
```

**响应**:
```json
[
  {
    "id": 5,
    "name": "热血",
    "category": "类型",
    "type": "user",
    "userCount": 245
  },
  {
    "id": 12,
    "name": "奇幻",
    "category": "类型",
    "type": "user",
    "userCount": 189
  },
  {
    "id": 8,
    "name": "冒险",
    "category": "类型",
    "type": "user",
    "userCount": 167
  },
  {
    "id": 23,
    "name": "校园",
    "category": "题材",
    "type": "user",
    "userCount": 142
  },
  {
    "id": 31,
    "name": "搞笑",
    "category": "风格",
    "type": "user",
    "userCount": 128
  }
]
```

### 4. 获取标签推荐

**请求**:
```
GET /api/tags/recommend?userId=1&limit=3
```

**响应**:
```json
[
  {
    "id": 15,
    "name": "战斗",
    "category": "元素",
    "type": "user",
    "relevanceScore": 0.85
  },
  {
    "id": 42,
    "name": "魔法",
    "category": "元素",
    "type": "user",
    "relevanceScore": 0.78
  },
  {
    "id": 27,
    "name": "剧情",
    "category": "风格",
    "type": "user",
    "relevanceScore": 0.72
  }
]
```

## 前端实现示例

### Vue3组件示例：用户标签选择器

```vue
<template>
  <div class="tag-selector">
    <h3>我的兴趣标签</h3>
    
    <div class="selected-tags">
      <span v-for="tag in selectedTags" :key="tag.id" class="tag">
        {{ tag.name }}
        <button @click="removeTag(tag.id)" class="remove-btn">×</button>
      </span>
    </div>
    
    <div class="tag-search">
      <input 
        v-model="searchQuery" 
        placeholder="搜索标签..." 
        @input="searchTags"
      />
      
      <div v-if="searchResults.length > 0" class="search-results">
        <div 
          v-for="tag in searchResults" 
          :key="tag.id"
          @click="addTag(tag)"
          class="tag-item"
        >
          {{ tag.name }}
        </div>
      </div>
    </div>
    
    <div class="tag-recommendations">
      <h4>推荐标签</h4>
      <div class="tag-cloud">
        <span 
          v-for="tag in recommendedTags" 
          :key="tag.id"
          @click="addTag(tag)"
          class="tag-rec"
        >
          {{ tag.name }}
        </span>
      </div>
    </div>
    
    <button @click="saveChanges" class="save-btn">保存更改</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  userId: {
    type: Number,
    required: true
  }
});

const selectedTags = ref([]);
const searchQuery = ref('');
const searchResults = ref([]);
const recommendedTags = ref([]);

// 加载用户当前标签
const loadUserTags = async () => {
  try {
    const response = await axios.get(`/api/tags/user/${props.userId}`);
    selectedTags.value = response.data.tags;
  } catch (error) {
    console.error('加载用户标签失败', error);
  }
};

// 加载推荐标签
const loadRecommendedTags = async () => {
  try {
    const response = await axios.get(`/api/tags/recommend?userId=${props.userId}&limit=5`);
    recommendedTags.value = response.data;
  } catch (error) {
    console.error('加载推荐标签失败', error);
  }
};

// 搜索标签
const searchTags = async () => {
  if (searchQuery.value.length < 2) {
    searchResults.value = [];
    return;
  }
  
  try {
    const response = await axios.get(`/api/tags/search?query=${searchQuery.value}`);
    // 过滤掉已选择的标签
    searchResults.value = response.data.filter(tag => 
      !selectedTags.value.some(selected => selected.id === tag.id)
    );
  } catch (error) {
    console.error('搜索标签失败', error);
  }
};

// 添加标签
const addTag = (tag) => {
  if (!selectedTags.value.some(t => t.id === tag.id)) {
    selectedTags.value.push(tag);
    // 从搜索结果中移除
    searchResults.value = searchResults.value.filter(t => t.id !== tag.id);
  }
};

// 移除标签
const removeTag = (tagId) => {
  selectedTags.value = selectedTags.value.filter(tag => tag.id !== tagId);
};

// 保存更改
const saveChanges = async () => {
  try {
    const tagIds = selectedTags.value.map(tag => tag.id);
    await axios.put(`/api/tags/user/${props.userId}`, { tagIds });
    alert('标签更新成功！');
  } catch (error) {
    console.error('保存标签失败', error);
    alert('标签更新失败，请重试');
  }
};

onMounted(() => {
  loadUserTags();
  loadRecommendedTags();
});
</script>

<style scoped>
.tag-selector {
  max-width: 500px;
  margin: 0 auto;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 15px 0;
}

.tag {
  background-color: #e0f7fa;
  border-radius: 16px;
  padding: 5px 12px;
  display: flex;
  align-items: center;
}

.remove-btn {
  background: none;
  border: none;
  margin-left: 5px;
  cursor: pointer;
  font-weight: bold;
}

.tag-search {
  margin-bottom: 20px;
  position: relative;
}

.tag-search input {
  width: 100%;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

.search-results {
  position: absolute;
  width: 100%;
  background: white;
  border: 1px solid #eee;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;
}

.tag-item {
  padding: 8px;
  cursor: pointer;
}

.tag-item:hover {
  background-color: #f5f5f5;
}

.tag-recommendations {
  margin-top: 20px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-rec {
  background-color: #f5f5f5;
  border-radius: 16px;
  padding: 5px 12px;
  cursor: pointer;
}

.tag-rec:hover {
  background-color: #e0e0e0;
}

.save-btn {
  margin-top: 20px;
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn:hover {
  background-color: #388e3c;
}
</style>
```

## 使用说明

1. 前端需要在页面加载时获取用户当前的标签列表
2. 当用户添加或删除标签时，只需更新本地状态，不需要立即发送请求
3. 只有当用户点击"保存"按钮时，才将完整的标签ID列表发送到服务器
4. 标签推荐功能可以根据用户已有标签提供相关推荐
5. 对于热门标签展示，可以在首页或用户设置页面显示 