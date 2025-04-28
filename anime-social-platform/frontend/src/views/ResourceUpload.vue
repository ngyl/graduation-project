<template>
  <app-layout>
    <div class="resource-upload-container">
      <div class="page-header">
        <h1>上传动漫资源</h1>
        <p class="subtitle">分享您喜爱的动漫资源，与社区成员一起交流</p>
      </div>

      <el-card class="upload-card">
        <el-form 
          :model="resourceForm" 
          :rules="rules" 
          ref="formRef" 
          label-width="100px"
          label-position="left"
        >
          <el-form-item label="资源标题" prop="title">
            <el-input v-model="resourceForm.title" placeholder="请输入资源标题" />
          </el-form-item>
          
          <el-form-item label="资源描述" prop="description">
            <el-input 
              v-model="resourceForm.description" 
              type="textarea" 
              :rows="5" 
              placeholder="请详细描述资源内容，如动漫名称、季度、画质等"
            />
          </el-form-item>
          
          <el-form-item label="封面图片" prop="coverFile">
            <el-upload
              class="cover-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleCoverChange"
              accept="image/*"
            >
              <img v-if="coverImageUrl" :src="coverImageUrl" class="cover-preview" />
              <div v-else class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <div class="upload-text">点击上传封面（可选）</div>
              </div>
            </el-upload>
            <div class="upload-tip">推荐尺寸: 800 x 450 像素，支持jpg、png格式，大小不超过2MB，可以不上传</div>
          </el-form-item>
          
          <el-form-item label="资源文件" prop="resourceFile">
            <el-upload
              class="resource-file-uploader"
              :auto-upload="false"
              :show-file-list="true"
              :on-change="handleResourceChange"
              :limit="1"
              accept=".jpg,.zip,.rar,.7z,.pdf,.mp4,.mkv"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择资源文件
              </el-button>
              <template #tip>
                <div class="upload-tip">支持jpg、zip、rar、7z、pdf、mp4、mkv等格式，大小限制50MB</div>
              </template>
            </el-upload>
          </el-form-item>
          
          <el-form-item label="标签" prop="tags">
            <el-select
              v-model="resourceForm.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入标签"
              style="width: 100%;"
            >
              <el-option
                v-for="tag in tagList"
                :key="tag.id || tag"
                :label="tag.name || tag"
                :value="tag.id || tag"
              />
            </el-select>
            <div class="upload-tip">最多可添加5个标签，每个标签不超过10个字符</div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting">
              发布资源
            </el-button>
            <el-button @click="cancelUpload">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Upload } from '@element-plus/icons-vue';
import AppLayout from '@/components/AppLayout.vue';
import * as resourceApi from '@/api/resource';
import { uploadResource, uploadCover } from '@/api/resource';
import { getTagsByType } from '@/api/tag';
import type { TagDTO } from '@/types/tag';

const router = useRouter();
const formRef = ref();
const submitting = ref(false);

// 标签列表
const tagList = ref<TagDTO[]>([]);
// 默认标签（用作备用，当API获取失败时使用）
const defaultTags = [
  '热血', '冒险', '战斗', '搞笑', '恋爱', '校园', '奇幻', '科幻', '魔法',
  '青春', '悬疑', '推理', '治愈', '日常', '励志', '机战', '后宫', '百合'
];

// 表单数据
const resourceForm = reactive({
  title: '',
  fileType: '',
  description: '',
  tags: [],
  coverFile: null as File | null,
  coverPath: '',
  resourceFile: null as File | null,
  filePath: ''
});

// 临时存储封面图片URL
const coverImageUrl = ref('');

// 加载标签数据
const loadTags = async () => {
  try {
    const response = await getTagsByType('resource');
    if (response.data.code === 200) {
      tagList.value = response.data.data;
    } else {
      // 如果API调用失败，使用默认标签
      tagList.value = defaultTags.map((name, index) => ({ 
        id: -index - 1, // 使用负数ID以区分本地创建的标签
        name, 
        type: 'resource' as const,
        category: '默认分类'  // 添加必要的category字段
      }));
      ElMessage.warning('获取标签失败，使用默认标签');
    }
  } catch (error) {
    console.error('加载资源标签失败:', error);
    // 如果API调用失败，使用默认标签
    tagList.value = defaultTags.map((name, index) => ({ 
      id: -index - 1, 
      name, 
      type: 'resource' as const,
      category: '默认分类'  // 添加必要的category字段
    }));
    ElMessage.warning('获取标签失败，使用默认标签');
  }
};

// 组件加载时获取标签
onMounted(() => {
  loadTags();
});

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入资源标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在2-50个字符之间', trigger: 'blur' }
  ],
  fileType: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入资源描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '描述长度应在10-1000个字符之间', trigger: 'blur' }
  ],
  tags: [
    { 
      validator: (rule: any, value: string[], callback: Function) => {
        if (value.length > 5) {
          callback(new Error('最多添加5个标签'));
        } else {
          for (const tag of value) {
            if (typeof tag === 'string' && tag.length > 10) {
              callback(new Error('每个标签不能超过10个字符'));
              return;
            }
          }
          callback();
        }
      }, 
      trigger: 'change' 
    }
  ]
};

// 处理封面图片选择
const handleCoverChange = (file: any) => {
  // 检查文件类型和大小
  const isImage = file.raw.type.startsWith('image/');
  const isLt2M = file.raw.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('封面必须是图片格式!');
    return;
  }
  
  if (!isLt2M) {
    ElMessage.error('封面图片大小不能超过2MB!');
    return;
  }
  
  // 上传封面图片
  uploadCoverFile(file.raw);
};

// 上传封面图片的方法
const uploadCoverFile = async (file: File) => {
  try {
    const response = await uploadCover(file);
    
    if (response.data.code === 200) {
      resourceForm.coverFile = file;
      resourceForm.coverPath = response.data.data.url;
      coverImageUrl.value = response.data.data.url;
      ElMessage.success('封面上传成功');
    } else {
      ElMessage.error(response.data.message || '封面上传失败');
    }
  } catch (error: any) {
    console.error('封面上传失败', error);
    ElMessage.error(error.response?.data?.message || '封面上传失败');
  }
};

// 使用on-change处理资源文件选择
const handleResourceChange = (file: any) => {
  // 检查文件大小
  const isLt50M = file.raw.size / 1024 / 1024 < 50;
  
  if (!isLt50M) {
    ElMessage.error('资源文件大小不能超过50MB!');
    return;
  }
  
  // 上传文件
  uploadResourceFile(file.raw);
};

// 上传资源文件的方法
const uploadResourceFile = async (file: File) => {
  try {
    const response = await uploadResource(file);
    
    if (response.data.code === 200) {
      // 保存文件引用
      resourceForm.resourceFile = file;
      // 保存服务器返回的URL
      resourceForm.filePath = response.data.data.url;
      // 设置资源类型
      resourceForm.fileType = resourceForm.fileType || getResourceType(response.data.data.fileType);
      ElMessage.success(`资源文件 ${file.name} 上传成功`);
    } else {
      ElMessage.error(response.data.message || '资源文件上传失败');
    }
  } catch (error: any) {
    console.error('资源文件上传失败', error);
    ElMessage.error(error.response?.data?.message || '资源文件上传失败');
  }
};

// 根据文件类型推荐资源类型
const getResourceType = (fileType: string) => {
  const typeMap: Record<string, string> = {
    'video': '动画',
    'image': '壁纸',
    'audio': '音乐',
    'document': '小说',
    'archive': '漫画'
  };
  return typeMap[fileType] || '';
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      ElMessage.error('请检查表单填写是否正确');
      return;
    }
    
    if (!resourceForm.resourceFile && !resourceForm.filePath) {
      ElMessage.error('请上传资源文件');
      return;
    }
    
    submitting.value = true;
    
    try {
      // 将表单数据转换为FormData
      const formData = new FormData();
      formData.append('title', resourceForm.title);
      formData.append('description', resourceForm.description);
      formData.append('type', resourceForm.fileType);
      
      // 添加标签
      if (resourceForm.tags.length > 0) {
        resourceForm.tags.forEach((tag: string | number, index: number) => {
          formData.append(`tagIds[${index}]`, String(tag));
        });
      }
      
      // 添加文件路径（而非文件本身，因为文件已经上传）
      formData.append('filePath', resourceForm.filePath);
      
      // 只有在有封面时才添加封面路径
      if (resourceForm.coverPath) {
        formData.append('coverPath', resourceForm.coverPath);
      }
      
      // 发送请求
      const response = await resourceApi.createResource(formData);
      
      if (response.data.code === 200) {
        ElMessage.success('资源发布成功!');
        
        // 上传成功后跳转到资源列表页面
        setTimeout(() => {
          router.push('/resources');
        }, 1500);
      } else {
        ElMessage.error(response.data.message || '发布失败，请重试');
      }
    } catch (error: any) {
      console.error('发布资源出错:', error);
      ElMessage.error(error.response?.data?.message || '发布失败，请检查网络连接');
    } finally {
      submitting.value = false;
    }
  });
};

// 取消上传
const cancelUpload = () => {
  if (resourceForm.title || resourceForm.description || resourceForm.coverFile || resourceForm.resourceFile) {
    ElMessageBox.confirm(
      '确定要取消上传吗？已填写的内容将不会保存',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '继续编辑',
        type: 'warning'
      }
    ).then(() => {
      router.push('/resources');
    }).catch(() => {
      // 用户点击了"继续编辑"，什么都不做
    });
  } else {
    router.push('/resources');
  }
};
</script>

<style scoped>
.resource-upload-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 32px;
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
}

.subtitle {
  color: var(--el-text-color-secondary);
  font-size: 16px;
}

.upload-card {
  margin-bottom: 30px;
}

.cover-uploader {
  display: flex;
  justify-content: center;
  width: 100%;
}

.cover-preview {
  width: 100%;
  max-width: 400px;
  max-height: 225px;
  object-fit: cover;
  border-radius: 4px;
}

.upload-placeholder {
  width: 100%;
  max-width: 400px;
  height: 225px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 1px dashed var(--el-border-color);
  border-radius: 4px;
  background-color: var(--el-fill-color-lighter);
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-placeholder:hover {
  border-color: var(--el-color-primary);
}

.upload-placeholder .el-icon {
  font-size: 28px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.upload-text {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  margin-top: 8px;
}

.resource-file-uploader {
  width: 100%;
}

@media (max-width: 767px) {
  .resource-upload-container {
    padding: 15px;
  }
  
  .cover-preview, .upload-placeholder {
    max-width: 100%;
    height: auto;
    aspect-ratio: 16/9;
  }
}
</style> 