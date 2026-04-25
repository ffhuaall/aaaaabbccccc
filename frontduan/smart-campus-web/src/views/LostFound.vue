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
        <el-tab-pane name="finished" label="已完成" />
      </el-tabs>

      <div v-loading="loading" class="items-grid">
        <el-empty v-if="filteredList.length === 0" description="暂无相关物品信息" style="width: 100%; grid-column: 1 / -1;" />
        
        <el-card v-for="item in filteredList" :key="item.id" class="item-card" @click="showDetails(item)">
          <div class="item-img-box">
            <el-image v-if="getFirstImage(item.images)" :src="getFirstImage(item.images)" fit="cover" />
            
            <div v-else :class="['placeholder-box', item.type === 0 ? 'p-lost' : 'p-found']">
              <div class="p-text">{{ item.type === 0 ? '寻物启事' : '招领启事' }}</div>
              <el-icon class="p-icon">
                <component :is="item.type === 0 ? 'Search' : 'Present'" />
              </el-icon>
            </div>

            <div :class="['type-tag', item.type === 0 ? 'lost' : 'found']">
              {{ item.type === 0 ? '寻物' : '招领' }}
            </div>
          </div>

          <div class="item-info">
            <div class="top-info">
              <h4 class="title">{{ item.itemName }}</h4>
              <p class="loc"><el-icon><Location /></el-icon> {{ item.location }}</p>
              <p class="desc-snippet">{{ item.description || '暂无详细描述' }}</p>
            </div>
            
            <div class="footer">
              <span class="time">{{ item.createTime?.split(' ')[0] }}</span>
              <el-tag 
                size="small" 
                :type="item.status === 1 ? 'info' : (item.status === -1 ? 'danger' : 'success')"
              >
                {{ item.status === 1 ? '已结案' : (item.status === -1 ? '已作废' : '寻找中') }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="物品详细信息" width="600px" destroy-on-close :lock-scroll="false">
      <div v-if="selectedItem" class="detail-container">
        <div class="detail-images">
          <el-carousel height="220px" v-if="parseImages(selectedItem.images).length > 0">
            <el-carousel-item v-for="img in parseImages(selectedItem.images)" :key="img">
              <el-image :src="img" fit="contain" style="width:100%; height:100%" :preview-src-list="parseImages(selectedItem.images)" />
            </el-carousel-item>
          </el-carousel>
          <div v-else :class="['detail-placeholder', selectedItem.type === 0 ? 'p-lost' : 'p-found']">
             <el-icon size="50"><component :is="selectedItem.type === 0 ? 'Search' : 'Present'" /></el-icon>
             <p>{{ selectedItem.type === 0 ? '暂无现场图片' : '拾获者未上传图片' }}</p>
          </div>
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
             <el-empty v-if="comments.length === 0" description="暂无留言" :image-size="40" />
          </div>
          <div class="comment-input">
            <el-input v-model="newComment" placeholder="询问物品细节..." size="small">
              <template #append><el-button @click="sendComment">发送</el-button></template>
            </el-input>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="detail-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button v-if="selectedItem?.status === 0 && selectedItem?.publisherId !== currentUserId" type="success" @click="handleClaim">
            确认认领并查看联系方式
          </el-button>
          <span v-if="selectedItem?.publisherId === currentUserId" class="tip">这是你发布的物品</span>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="publishVisible" title="发布信息" width="500px" append-to-body :lock-scroll="false">
      <el-form ref="publishFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="信息类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button :label="0">寻物启事</el-radio-button>
            <el-radio-button :label="1">失物招领</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="物品名称" prop="title">
          <el-input v-model="form.title" placeholder="如：黑色联想电源线" />
        </el-form-item>
        <el-form-item label="物品类别" prop="category">
          <el-select v-model="form.category" style="width: 100%;">
            <el-option label="证件" value="证件" />
            <el-option label="电子产品" value="电子产品" />
            <el-option label="学习用品" value="学习用品" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="地点" prop="location"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="详细特征" prop="description"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="联系方式" prop="contact"><el-input v-model="form.contact" /></el-form-item>
        <el-form-item label="照片"><el-upload action="http://localhost:8080/file/upload" :headers="uploadHeaders" list-type="picture-card" v-model:file-list="fileList" :limit="2"><el-icon><Plus /></el-icon></el-upload></el-form-item>
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

const parseImages = (str) => { try { return JSON.parse(str) || [] } catch { return [] } }
const getFirstImage = (str) => parseImages(str)[0]

const loading = ref(false)
const allItems = ref([])
const activeTab = ref('all')
const filter = reactive({ keyword: '', category: '' })

const detailVisible = ref(false)
const selectedItem = ref(null)
const isClaimed = ref(false) 
const comments = ref([])
const newComment = ref('')

const publishVisible = ref(false)
const submitting = ref(false)
const publishFormRef = ref(null)
const fileList = ref([])
const form = reactive({ type: 0, title: '', category: '', location: '', description: '', contact: '', images: '[]' })
const rules = {
  type: [{ required: true }], title: [{ required: true, message: '请输入名称' }],
  category: [{ required: true }], location: [{ required: true }], contact: [{ required: true }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/lost-found/list')
    allItems.value = res || []
  } finally { loading.value = false }
}

// ================== 核心改进：上帝视角的五维过滤逻辑 ==================
const filteredList = computed(() => {
  return allItems.value.filter(i => {
    // 1. “已完成”专区逻辑：同时包含 已结案(1) 和 已作废(-1) 的个人物品
    if (activeTab.value === 'finished') {
      return (i.status === 1 || i.status === -1) && i.publisherId === currentUserId;
    }

    // 2. 公共展示区逻辑：坚决不显示已结案(1)和已作废(-1)的物品
    if (i.status === 1 || i.status === -1) return false;

    // 3. 标签分类导航
    if (activeTab.value === 'lost' && i.type !== 0) return false;
    if (activeTab.value === 'found' && i.type !== 1) return false;
    if (activeTab.value === 'my' && i.publisherId !== currentUserId) return false;
    
    // 4. 搜索与类别过滤
    if (filter.category && !i.itemName.includes(`[${filter.category}]`)) return false;
    if (filter.keyword && !i.itemName.includes(filter.keyword)) return false;
    
    return true;
  })
})

const openPublishDialog = () => {
  if(publishFormRef.value) publishFormRef.value.resetFields()
  fileList.value = []; publishVisible.value = true;
}

const submitPublish = () => {
  publishFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const imagesToSubmit = fileList.value.map(file => file.response ? file.response.data : file.url)
        const submitData = {
          type: form.type, itemName: `[${form.category}] ${form.title}`, 
          location: form.location, description: form.description,
          contactInfo: form.contact, images: JSON.stringify(imagesToSubmit.filter(Boolean)),
          publisherId: currentUserId
        }
        await request.post('/lost-found/publish', submitData)
        ElMessage.success('发布成功！'); publishVisible.value = false; fetchList();
      } finally { submitting.value = false }
    }
  })
}

const showDetails = (item) => {
  selectedItem.value = item; isClaimed.value = item.status === 1;
  detailVisible.value = true; loadComments(item.id);
}

const loadComments = async (itemId) => {
  try { const res = await request.get(`/lost-found/comments/${itemId}`); comments.value = res || []; } 
  catch { comments.value = []; }
}

const sendComment = async () => {
  if(!newComment.value) return
  await request.post('/lost-found/comment/add', { itemId: selectedItem.value.id, content: newComment.value, userId: currentUserId })
  ElMessage.success('留言成功'); newComment.value = ''; loadComments(selectedItem.value.id);
}

const handleClaim = async () => {
  try {
    await ElMessageBox.confirm('确认认领后发布者将收到通知', '认领确认')
    await request.post(`/lost-found/claim/${selectedItem.value.id}?claimerId=${currentUserId}`)
    ElMessage.success('操作成功'); isClaimed.value = true; fetchList();
  } catch (e) {}
}

onMounted(fetchList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.items-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; margin-top: 20px; }
.item-card { cursor: pointer; transition: transform 0.2s; border-radius: 12px; overflow: hidden; display: flex; flex-direction: column; height: 380px; border: 1px solid #f0f0f0; }
.item-card:hover { transform: translateY(-6px); box-shadow: 0 12px 24px rgba(0,0,0,0.1); }

.item-img-box { position: relative; flex: 0 0 170px; background-color: #f9f9f9; }
.item-img-box .el-image { width: 100%; height: 100%; }

/* 占位符设计 */
.placeholder-box { width: 100%; height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; color: white; }
.p-lost { background: linear-gradient(135deg, #FF9A9E 0%, #FAD0C4 100%); }
.p-found { background: linear-gradient(135deg, #84FAB0 0%, #8FD3F4 100%); }
.p-text { font-size: 16px; font-weight: bold; margin-bottom: 5px; }
.p-icon { font-size: 30px; opacity: 0.5; }

.detail-placeholder { height: 220px; display: flex; flex-direction: column; justify-content: center; align-items: center; color: white; border-radius: 8px; }

.type-tag { position: absolute; top: 0; left: 0; padding: 4px 12px; color: white; font-size: 12px; border-bottom-right-radius: 12px; font-weight: bold; z-index: 2; }
.type-tag.lost { background: #F56C6C; }
.type-tag.found { background: #67C23A; }

.item-info { padding: 15px; flex: 1; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden; }
.title { margin: 0 0 6px; font-size: 16px; color: #2c3e50; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.loc { margin: 0 0 10px; font-size: 13px; color: #909399; display: flex; align-items: center; gap: 4px; }
.desc-snippet { margin: 0; font-size: 13px; color: #606266; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; }

.footer { display: flex; justify-content: space-between; align-items: center; padding-top: 10px; border-top: 1px solid #f5f7fa; }
.time { font-size: 12px; color: #b0b0b0; }

.detail-container { display: flex; flex-direction: column; gap: 15px; }
.contact-highlight { font-size: 18px; color: #E6A23C; font-weight: bold; }
.comment-section { background: #f8f9fa; padding: 12px; border-radius: 8px; }
.comment-list { max-height: 120px; overflow-y: auto; margin-bottom: 10px; }
.comment-item { font-size: 13px; margin-bottom: 5px; }
</style>