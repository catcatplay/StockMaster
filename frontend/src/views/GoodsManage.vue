<template>
  <div class="card-container">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button v-if="!isOutboundStaff" type="primary" :icon="Plus" @click="showAddDialog">添加货物</el-button>
        <span class="stock-count-text">所有货物库存总数：<span class="count-number">{{ totalStockCount }}</span></span>
      </div>
      <div class="search-area">
        <el-input
          v-model="searchName"
          placeholder="搜索货物名称"
          class="search-input"
          clearable
          @clear="loadGoodsList"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <el-table :data="goodsList" stripe style="margin-top: 20px; width: 100%">
      <el-table-column prop="name" label="货物名称" min-width="100" show-overflow-tooltip />
      <el-table-column prop="brand" label="品牌" min-width="100" show-overflow-tooltip />
      <el-table-column prop="model" label="型号" min-width="150" show-overflow-tooltip />
      <el-table-column prop="totalStock" label="总库存" width="100">
        <template #default="scope">
          <el-tag type="info" effect="plain">
            {{ scope.row.totalStock || 0 }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remainingStock" label="剩余库存" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.remainingStock > 0 ? 'success' : 'danger'" effect="light">
            {{ scope.row.remainingStock || 0 }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column v-if="!isOutboundStaff" label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.currentPage"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination-container"
    />

    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑货物' : '添加货物'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="goodsForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="货物名称" prop="name">
          <el-input v-model="goodsForm.name" placeholder="请输入货物名称" />
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="goodsForm.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="goodsForm.model" placeholder="请输入型号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getGoodsList, addGoods, updateGoods, deleteGoods, getGoodsStockCount } from '@/api/goods'

const route = useRoute()
const currentType = computed(() => route.meta.type || 'device')

// 状态定义
const goodsList = ref([])
const searchName = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const totalStockCount = ref(0)

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const goodsForm = reactive({
  id: '',
  name: '',
  brand: '',
  model: '',
  batch: '',
  unitPrice: 0,
  remainingStock: 0,
  totalStock: 0
})

const rules = {
  name: [{ required: true, message: '请输入货物名称', trigger: 'blur' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }]
}

// 权限控制
const isOutboundStaff = computed(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    const userInfo = JSON.parse(userInfoStr)
    return userInfo.roleName === '出库员' || userInfo.roleName === '销售'
  }
  return false
})

// 获取列表
const loadGoodsList = async () => {
  loading.value = true
  try {
    const res = await getGoodsList({
      type: currentType.value,
      current: pagination.currentPage,
      size: pagination.pageSize,
      name: searchName.value
    })
    if (res.data.records) {
      // 后端分页返回
      goodsList.value = res.data.records
      pagination.total = res.data.total
    } else {
      // 兼容旧接口
      goodsList.value = res.data
      pagination.total = res.data.length
    }
  } catch (error) {
    console.error('获取货物列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取总库存数量
const loadStockCount = async () => {
  try {
    const res = await getGoodsStockCount(currentType.value)
    if (res.code === 200) {
      totalStockCount.value = res.data
    }
  } catch (error) {
    console.error('获取库存统计失败', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadGoodsList()
}

// 分页
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadGoodsList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadGoodsList()
}

// 监听路由变化，重新加载数据
watch(() => route.path, () => {
  pagination.currentPage = 1
  loadGoodsList()
  loadStockCount()
}, { immediate: true })

// 添加
const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(goodsForm, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该货物吗？', '警告', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res = await deleteGoods(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadGoodsList()
        loadStockCount()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      // error
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const api = isEdit.value ? updateGoods : addGoods
        // 确保传入type
        const payload = { ...goodsForm, type: currentType.value }
        const res = await api(payload)
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadGoodsList()
          loadStockCount()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        // error
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  goodsForm.id = ''
  goodsForm.name = ''
  goodsForm.brand = ''
  goodsForm.model = ''
  goodsForm.batch = ''
  goodsForm.unitPrice = 0
  goodsForm.remainingStock = 0
  goodsForm.totalStock = 0
}

// 日期格式化
const formatDate = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

onMounted(() => {
  // watch会自动加载，无需重复调用
  // loadGoodsList()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stock-count-text {
  font-size: 14px;
  color: #606266;
}

.count-number {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

.search-area {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
