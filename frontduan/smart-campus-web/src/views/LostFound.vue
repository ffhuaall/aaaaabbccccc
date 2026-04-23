<template>
  <div class="lost-found-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="emoji">🔍</span>
            <span class="title">校园失物招领中心</span>
          </div>
          <div class="header-right">
            <el-select v-model="filter.category" placeholder="物品类别" clearable style="width: 130px; margin-right: 12px;" @change="handleFilter">
              <el-option label="证件" value="证件" />
              <el-option label="电子产品" value="电子产品" />
              <el-option label="学习用品" value="学习用品" />
              <el-option label="生活用品" value="生活用品" />
              <el-option label="其他" value="其他" />
            </el-select>
            <el-input 
              v-model="filter.keyword" 
              placeholder="搜索物品名称或地点..." 
              clearable 
              prefix-icon="Search"
              style="width: 220px; margin-right: 12px;"
              @keyup.enter="handleFilter"
              @clear="handleFilter"
            />
            <el-button type="primary" icon="Plus" @click="openPublishDialog">发布信息</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="custom-tabs" @tab-change="handleFilter">
        <el-tab-pane name="all" label="全部信息" />
        <el-tab-pane name="lost" label="📢 寻物启事 (我丢了东西)" />
        <el-tab-pane name="found" label="🤝 失物招领 (我捡到东西)" />
        <el-tab-pane name="my" label="👤 我的发布" />
      </el-tabs>

      <div v-loading="loading" class="items-grid">
        <el-empty v-if="filteredList.length === 0" description="暂无相关物品信息" style="width: 100%; grid-column: 1 / -1;" />
        
        <el-card v-for="item in filteredList" :key="item.id" shadow="hover" class="item-card" :body-style="{ padding: '0px' }">
          <div class="item-image-wrapper">
            <el-image 
              :src="safeGetFirstImage(item.images) || defaultImage" 
              class="item-image" 
              fit="cover"
              :preview-src-list="safeParseImages(item.images)"
              preview-teleported
            />
            <div :class="['type-badge', item.type === 0 ? 'badge-lost' : 'badge-found']">
              {{ item.type === 0 ? '寻物' : '招领' }}
            </div>
            <div v-if="item.status === 1" class="status-badge resolved">已结案</div>
          </div>

          <div class="item-content">
            <div class="item-title" :title="item.title">{{ item.title }}</div>
            <div class="item-desc">
              <el-tag size="small" type="info" style="margin-right: 5px;">{{ item.category || '其他' }}</el-tag>
              <span class="location-text"><el-icon><Location /></el-icon> {{ item.location }}</span>
            </div>
            <div class="item-time"><el-icon><Clock /></el-icon> {{ item.createTime?.substring(0, 16) }}</div>
            
            <div class="item-footer">
              <el-button v-if="activeTab !== 'my'" type="primary" plain size="small" style="width: 100%;" @click="viewContact(item)">联系发布者</el-button>
              
              <div v-else style="display: flex; gap: 10px; width: 100%;">
                <el-button v-if="item.status === 0" type="success" size="small" style="flex: 1;" @click="handleResolve(item.id)">标记找到</el-button>
                <el-button type="danger" plain size="small" style="flex: 1;" @click="handleDelete(item.id)">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog v-model="publishVisible" title="发布失物/招领信息" width="500px" append-to-body>
      <el-form ref="publishFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="信息类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button :label="0">寻物启事 (丢东西)</el-radio-button>
            <el-radio-button :label="1">失物招领 (捡东西)</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="物品名称" prop="title">
          <el-input v-model="form.title" placeholder="如：黑色联想笔记本电源线" />
        </el-form-item>
        
        <el-form-item label="物品类别" prop="category">
          <el-select v-model="form.category" placeholder="选择类别" style="width: 100%;">
              <el-option label="证件" value="证件" />
              <el-option label="电子产品" value="电子产品" />
              <el-option label="学习用品" value="学习用品" />
              <el-option label="生活用品" value="生活用品" />
              <el-option label="其他" value="其他" />
            </el-select>
        </el-form-item>

        <el-form-item label="地点" prop="location">
          <el-input v-model="form.location" placeholder="丢失或捡到的地点，如：图书馆二楼南区" />
        </el-form-item>

        <el-form-item label="详细特征" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="描述颜色、特征、内含物品等细节..." />
        </el-form-item>

        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="如：手机号 / 微信号 / QQ，仅点击联系时可见" />
        </el-form-item>

        <el-form-item label="上传照片">
          <el-upload
            action="http://localhost:8080/file/upload"
            :headers="uploadHeaders"  
            list-type="picture-card"
            v-model:file-list="fileList"
            :limit="2"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPublish">发布上墙</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// 基础数据
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = userInfo.id || 1001
const uploadHeaders = { token: localStorage.getItem('Authorization') }
const defaultImage = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png' // 默认无图占位图

const loading = ref(false)
const allItems = ref([])
const activeTab = ref('all')
const filter = reactive({ category: '', keyword: '' })

// 发布表单数据
const publishVisible = ref(false)
const submitting = ref(false)
const publishFormRef = ref(null)
const fileList = ref([])

// 这里的字段要与你后端的 BizLostFound 实体类对应
const form = reactive({
  type: 0, // 0:寻物, 1:招领
  title: '',
  category: '',
  location: '',
  description: '',
  contact: '',
  images: '[]'
})

const rules = {
  type: [{ required: true, message: '请选择信息类型' }],
  title: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择物品类别', trigger: 'change' }],
  location: [{ required: true, message: '请输入相关地点', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系方式以便归还', trigger: 'blur' }]
}

// 辅助方法：解析图片 JSON
const safeParseImages = (imgStr) => {
  if (!imgStr) return []
  try { return JSON.parse(imgStr) } catch (e) { return [imgStr] }
}
const safeGetFirstImage = (imgStr) => {
  const arr = safeParseImages(imgStr)
  return arr.length > 0 ? arr[0] : null
}

// 获取大厅列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/lost-found/list')
    allItems.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 纯前端的高效筛选逻辑
const filteredList = computed(() => {
  return allItems.value.filter(item => {
    // 1. Tab 过滤
    if (activeTab.value === 'lost' && item.type !== 0) return false
    if (activeTab.value === 'found' && item.type !== 1) return false
    if (activeTab.value === 'my' && item.publisherId !== currentUserId) return false
    
    // 2. 类别过滤
    if (filter.category && item.category !== filter.category) return false
    
    // 3. 关键字过滤
    if (filter.keyword) {
      const kw = filter.keyword.toLowerCase()
      const matchTitle = item.title && item.title.toLowerCase().includes(kw)
      const matchLoc = item.location && item.location.toLowerCase().includes(kw)
      if (!matchTitle && !matchLoc) return false
    }
    
    return true
  })
})

const handleFilter = () => { /* 触发 computed 更新 */ }

const openPublishDialog = () => {
  if(publishFormRef.value) publishFormRef.value.resetFields()
  fileList.value = []
  publishVisible.value = true
}

const submitPublish = () => {
  publishFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const imagesToSubmit = fileList.value.map(file => file.url || (file.response && file.response.data))
        
        // 【核心修复：字段名称翻译与重组】
        const submitData = {
          type: form.type,
          // 将分类和标题拼在一起传给后端的 itemName 字段
          itemName: `[${form.category}] ${form.title}`, 
          location: form.location,
          description: form.description,
          // 将前端的 contact 翻译成后端需要的 contactInfo 字段
          contactInfo: form.contact, 
          images: JSON.stringify(imagesToSubmit.filter(Boolean)),
          publisherId: currentUserId
        }
        
        await request.post('/lost-found/publish', submitData)
        ElMessage.success('发布成功！愿物品早日物归原主。')
        publishVisible.value = false
        fetchList()
      } finally {
        submitting.value = false
      }
    }
  })
}

// 点击联系发布者（务实做法：直接弹窗显示联系方式）
const viewContact = (item) => {
  ElMessageBox.alert(
    `<strong>联系方式：</strong><br/><span style="font-size:18px; color:#409EFF;">${item.contact || '发布者未留下联系方式'}</span><br/><br/><span style="font-size:12px;color:#999;">请备注：在校园失物招领大厅看到的</span>`, 
    '联系发布者', 
    { dangerouslyUseHTMLString: true, confirmButtonText: '我知道了' }
  )
}

// 我的发布：标记已结案（需要后端支持更新状态）
const handleResolve = async (id) => {
  try {
    await ElMessageBox.confirm('确认该物品已经找到/归还了吗？', '结案确认')
    // 如果后端没有专门的 resolve 接口，我们可以用通用的 update 接口，这里预留了专门的语意化接口
    await request.post(`/lost-found/resolve/${id}`)
    ElMessage.success('已标记为结案，太棒了！')
    fetchList()
  } catch (e) {}
}

// 我的发布：删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条发布记录吗？', '删除确认', { type: 'warning' })
    await request.post(`/lost-found/delete/${id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {}
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.lost-found-page { padding-bottom: 20px; }
.main-card { border-radius: 12px; border: none; min-height: 600px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; font-weight: bold; font-size: 16px; color: #333; }
.header-left .emoji { font-size: 20px; margin-right: 8px; }
.header-right { display: flex; align-items: center; }

:deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #f0f0f0; }
:deep(.el-tabs__item) { font-size: 15px; padding: 0 20px; height: 50px; line-height: 50px; }

/* 瀑布流卡片网格布局 */
.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.item-card {
  border-radius: 10px;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.item-card:hover { transform: translateY(-5px); box-shadow: 0 8px 16px rgba(0,0,0,0.08); }

.item-image-wrapper { position: relative; height: 160px; width: 100%; background: #f5f7fa; }
.item-image { width: 100%; height: 100%; }

/* 左上角寻物/招领角标 */
.type-badge {
  position: absolute; top: 0; left: 0; padding: 4px 10px; font-size: 12px; color: white; font-weight: bold; border-bottom-right-radius: 10px;
}
.badge-lost { background: rgba(245, 108, 108, 0.9); } /* 红色代表丢东西，比较着急 */
.badge-found { background: rgba(103, 194, 58, 0.9); } /* 绿色代表捡到东西，带来希望 */

/* 右上角结案角标 */
.status-badge {
  position: absolute; top: 10px; right: 10px; padding: 2px 8px; font-size: 12px; border-radius: 12px;
}
.resolved { background: rgba(0,0,0,0.6); color: white; }

.item-content { padding: 15px; flex: 1; display: flex; flex-direction: column; }
.item-title { font-weight: bold; font-size: 15px; margin-bottom: 8px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-desc { font-size: 13px; color: #666; margin-bottom: 8px; display: flex; align-items: center; }
.location-text { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-time { font-size: 12px; color: #999; margin-bottom: 15px; display: flex; align-items: center; gap: 4px; }
.item-footer { margin-top: auto; } /* 把按钮挤到最底部 */
</style>