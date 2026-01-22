<template>
  <div class="goods-manage">
    <div class="toolbar">
      <el-button v-if="!isOutboundStaff" type="primary" @click="showAddDialog">添加货物</el-button>
      <el-input
        v-model="searchName"
        placeholder="搜索货物名称"
        style="width: 300px; margin-left: 10px;"
        clearable
        @clear="loadGoodsList"
      >
        <template #append>
          <el-button @click="handleSearch" :icon="Search">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-table :data="goodsList" border style="margin-top: 20px;">
<!--      <el-table-column prop="id" label="ID" width="80" />-->
      <el-table-column prop="name" label="货物名称" />
      <el-table-column prop="brand" label="品牌" />
      <el-table-column prop="model" label="型号" />
      <el-table-column prop="batch" label="批次" />
      <el-table-column v-if="!isOutboundStaff" prop="unitPrice" label="单价" width="120">
        <template #default="scope">
          {{ scope.row.unitPrice ? '￥' + scope.row.unitPrice : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="stockQuantity" label="库存数量" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.stockQuantity > 0 ? 'success' : 'danger'">
            {{ scope.row.stockQuantity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column v-if="!isOutboundStaff" label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      v-model:current-page="pagination.currentPage"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 20px; justify-content: flex-end;"
    />

    <!-- 添加/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑货物' : '添加货物'"
      width="500px"
    >
      <el-form :model="goodsForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="货物名称" prop="name">
          <el-input v-model="goodsForm.name" placeholder="请输入货物名称" />
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="goodsForm.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="goodsForm.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="批次" prop="batch">
          <el-input v-model="goodsForm.batch" placeholder="请输入批次" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="goodsForm.unitPrice" :precision="2" :min="0" placeholder="请输入单价" />
        </el-form-item>
<!--        <el-form-item label="库存数量" prop="stockQuantity" v-if="isEdit">-->
<!--          <el-input-number v-model="goodsForm.stockQuantity" :min="0" />-->
<!--        </el-form-item>-->
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getGoodsList, addGoods, updateGoods, deleteGoods, searchGoods } from '@/api/goods'

const route = useRoute()
const currentType = computed(() => route.meta.type || 'device')

// 判断当前用户是否为出库员
const isOutboundStaff = computed(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    const user = JSON.parse(userInfo)
    return user.roleName === '出库员'
  }
  return false
})

const goodsList = ref([])
const searchName = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

// 分页参数
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const goodsForm = ref({
  id: null,
  name: '',
  brand: '',
  model: '',
  batch: '',
  unitPrice: null,
  stockQuantity: 0
})

const rules = {
  name: [{ required: true, message: '请输入货物名称', trigger: 'blur' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }]
}

const loadGoodsList = async () => {
  try {
    const res = await getGoodsList({ 
      type: currentType.value,
      current: pagination.value.currentPage,
      size: pagination.value.pageSize
    })
    if (res.data.records) {
      // 后端分页返回
      goodsList.value = res.data.records
      pagination.value.total = res.data.total
    } else {
      // 兼容旧接口
      goodsList.value = res.data
      pagination.value.total = res.data.length
    }
  } catch (error) {
    console.error('加载货物列表失败', error)
  }
}

// 监听路由变化，重新加载数据
watch(() => route.path, () => {
  pagination.value.currentPage = 1
  loadGoodsList()
}, { immediate: true })

const handleSearch = async () => {
  if (!searchName.value) {
    pagination.value.currentPage = 1
    loadGoodsList()
    return
  }
  try {
    const res = await searchGoods(searchName.value)
    goodsList.value = res.data
    pagination.value.total = res.data.length
    pagination.value.currentPage = 1
  } catch (error) {
    console.error('搜索失败', error)
  }
}

const showAddDialog = () => {
  isEdit.value = false
  goodsForm.value = {
    id: null,
    name: '',
    brand: '',
    model: '',
    batch: '',
    unitPrice: null,
    type: currentType.value,
    stockQuantity: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  goodsForm.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateGoods(goodsForm.value)
          ElMessage.success('更新成功')
        } else {
          await addGoods(goodsForm.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadGoodsList()
      } catch (error) {
        console.error('提交失败', error)
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该货物吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteGoods(row.id)
      ElMessage.success('删除成功')
      loadGoodsList()
    } catch (error) {
      console.error('删除失败', error)
    }
  })
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.currentPage = 1
  loadGoodsList()
}

const handleCurrentChange = (val) => {
  pagination.value.currentPage = val
  loadGoodsList()
}
</script>

<style scoped>
.goods-manage {
  padding: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
}
</style>
