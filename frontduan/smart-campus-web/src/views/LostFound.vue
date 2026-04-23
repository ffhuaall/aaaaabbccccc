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
            <el-select v-model="filter.category" placeholder="物品类别" clearable style="width: 130px; margin-right: 12px;">
              <el-option label="证件" value="证件" />
              <el-option label="电子产品" value="电子产品" />
              <el-option label="学习用品" value="学习用品" />
              <el-option label="生活用品" value="生活用品" />
              <el-option label="其他" value="其他" />
            </el-select>
            <el-input v-model="filter.keyword" placeholder="搜索关键词..." clearable style="width: 200px; margin-right: 12px;" />
            <el-button type="primary" icon="Plus" @click="openPublishDialog">发布信息</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane name="all" label="全部" />
        <el-tab-pane name="lost" label="寻物启事" />
        <el-tab-pane name="found" label="失物招领" />
        <el-tab-pane name="my" label="我的发布" />
      </el-tabs>

      <div v-loading="loading" class="items-grid">
        <el-empty v-if="filteredList.length === 0" description="暂无相关物品信息" style="width: 100%; grid-column: 1 / -1;" />
        
        <el-card v-for="item in filteredList" :key="item.id" class="item-card" @click="showDetails(item)">
          <div class="item-img-box">
            <el-image :src="getFirstImage(item.images) || defaultImg" fit="cover" />
            <div :class="['type-tag', item.type === 0 ? 'lost' : 'found']">
              {{ item.type === 0 ? '寻物' : '招领' }}
            </div>
          </div>
          <div class="item-info">
            <h4 class="title">{{ item.itemName }}</h4>
            <p class="loc"><el-icon><Location /></el-icon> {{ item.location }}</p>
            <div class="footer">
              <span class="time">{{ item.createTime?.split(' ')[0] }}</span>
              <el-tag size="small" :type="item.status === 1 ? 'info' : 'success'">
                {{ item.status === 1 ? '已结案' : '寻找中' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="物品详细信息" width="600px" destroy-on-close>
      <div v-if="selectedItem" class="detail-container">
        <div class="detail-images">
          <el-carousel height="200px" v-if="parseImages(selectedItem.images).length > 0">
            <el-carousel-item v-for="img in parseImages(selectedItem.images)" :key="img">
              <el-image :src="img" fit="contain" style="width:100%; height:100%" :preview-src-list="parseImages(selectedItem.images)" />
            </el-carousel-item>
          </el-carousel>
          <el-empty v-else description="发布者未上传图片" :image-size="60" />
        </div>

        <div class="detail-text">
          <h2 class="d-title">{{ selectedItem.itemName }}</h2>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="相关地点">{{ selectedItem.location }}</el-descriptions-item>
            <el-descriptions-item label="详细描述">{{ selectedItem.description || '无详细描述' }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ selectedItem.createTime }}</el-descriptions-item>
            <el-descriptions-item label="联系方式" v-if="isClaimed">
              <span class="contact-highlight">{{ selectedItem.contactInfo }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="comment-section">
          <p class="section-title">💬 留言板</p>
          <div class="comment-list">
             <div v-for="c in comments" :key="c.id" class="comment-item">
                <span class="c-user">同学{{ c.userId.toString().slice(-4) }}:</span>
                <span class="c-content">{{ c.content }}</span>
             </div>
             <el-empty v-if="comments.length === 0" description="暂无留言，可询问物品细节" :image-size="40" />
          </div>
          <div class="comment-input">
            <el-input v-model="newComment" placeholder="询问物品细节..." size="small">
              <template #append>
                <el-button @click="sendComment">发送</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="detail-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button 
            v-if="selectedItem?.status === 0 && selectedItem?.publisherId !== currentUserId" 
            type="success" 
            @click="handleClaim"
          >
            确认认领并查看联系方式
          </el-button>
          <span v-if="selectedItem?.publisherId === currentUserId" class="tip">这是你发布的物品信息</span>
        </div>
      </template>
    </el-dialog>

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

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = userInfo.id || 1001
const defaultImg = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'
const uploadHeaders = { token: localStorage.getItem('Authorization') }

const loading = ref(false)
const allItems = ref([])
const activeTab = ref('all')
const filter = reactive({ keyword: '', category: '' })

// 发布逻辑相关变量
const publishVisible = ref(false)
const submitting = ref(false)
const publishFormRef = ref(null)
const fileList = ref([])

const form = reactive({
  type: 0,
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

// 详情与认领逻辑变量
const detailVisible = ref(false)
const selectedItem = ref(null)
const isClaimed = ref(false) 
const comments = ref([])
const newComment = ref('')

const parseImages = (str) => { try { return JSON.parse(str) || [] } catch { return [] } }
const getFirstImage = (str) => parseImages(str)[0]

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/lost-found/list')
    allItems.value = res || []
  } catch (e) { console.error(e) } finally {
    loading.value = false
  }
}

// 高效的前端过滤计算属性
const filteredList = computed(() => {
  return allItems.value.filter(i => {
    if (activeTab.value === 'lost' && i.type !== 0) return false
    if (activeTab.value === 'found' && i.type !== 1) return false
    if (activeTab.value === 'my' && i.publisherId !== currentUserId) return false
    
    // 类别过滤（解析中括号里的分类，例如 [电子产品] xxx）
    if (filter.category && !i.itemName.includes(`[${filter.category}]`)) return false
    
    // 关键字过滤
    if (filter.keyword && !i.itemName.includes(filter.keyword)) return false
    return true
  })
})

// === 打开与提交发布 ===
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
        
        // 将分类和标题拼在一起传给后端的 itemName 字段
        const submitData = {
          type: form.type,
          itemName: `[${form.category}] ${form.title}`, 
          location: form.location,
          description: form.description,
          contactInfo: form.contact, 
          images: JSON.stringify(imagesToSubmit.filter(Boolean)),
          publisherId: currentUserId
        }
        
        await request.post('/lost-found/publish', submitData)
        ElMessage.success('发布成功！愿物品早日物归原主。')
        publishVisible.value = false
        fetchList()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// === 详情与留言逻辑 ===
const showDetails = async (item) => {
  selectedItem.value = item
  isClaimed.value = item.status === 1 
  detailVisible.value = true
  loadComments(item.id)
}

const loadComments = async (itemId) => {
  try {
    const res = await request.get(`/lost-found/comments/${itemId}`)
    comments.value = res || []
  } catch (error) {
    comments.value = []
  }
}

const sendComment = async () => {
  if(!newComment.value) return
  try {
    await request.post('/lost-found/comment/add', { 
      itemId: selectedItem.value.id, 
      content: newComment.value, 
      userId: currentUserId 
    })
    ElMessage.success('留言成功！')
    newComment.value = ''
    loadComments(selectedItem.value.id) // 重新加载留言
  } catch (error) {}
}

const handleClaim = async () => {
  try {
    await ElMessageBox.confirm('确认要认领/找回该物品吗？确认后发布者将收到系统通知。', '认领确认')
    await request.post(`/lost-found/claim/${selectedItem.value.id}?claimerId=${currentUserId}`)
    ElMessage.success('认领请求已发送，发布者已收到提醒！')
    isClaimed.value = true 
    fetchList() 
  } catch (e) {}
}

onMounted(fetchList)
</script>

<style scoped>
.items-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; margin-top: 20px; }
.item-card { cursor: pointer; transition: transform 0.2s; border-radius: 10px; overflow: hidden; }
.item-card:hover { transform: translateY(-5px); box-shadow: 0 8px 16px rgba(0,0,0,0.08); }
.item-img-box { position: relative; height: 150px; }
.type-tag { position: absolute; top: 0; left: 0; padding: 4px 10px; color: white; font-size: 12px; border-bottom-right-radius: 10px; font-weight: bold; }
.type-tag.lost { background: #F56C6C; }
.type-tag.found { background: #67C23A; }
.item-info { padding: 12px; }
.item-info .title { margin: 0 0 8px; font-size: 15px; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-info .loc { font-size: 13px; color: #999; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-info .footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; }

.detail-container { display: flex; flex-direction: column; gap: 20px; }
.d-title { margin: 0; color: #409EFF; }
.contact-highlight { font-size: 18px; color: #E6A23C; font-weight: bold; }
.comment-section { background: #f8f9fa; padding: 15px; border-radius: 8px; }
.section-title { font-weight: bold; margin-bottom: 10px; display: block; border-bottom: 1px solid #eee; padding-bottom: 5px; }
.comment-list { max-height: 150px; overflow-y: auto; margin-bottom: 10px; }
.comment-item { font-size: 13px; margin-bottom: 8px; }
.c-user { color: #909399; margin-right: 5px; }
.detail-footer { display: flex; justify-content: flex-end; align-items: center; gap: 15px; }
.tip { color: #999; font-size: 13px; }
</style>