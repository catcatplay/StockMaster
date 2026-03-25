<template>
  <div class="card-container">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button v-if="!isOutboundStaff" type="success" :icon="Plus" @click="showAddDialog">添加货物</el-button>
        <el-button type="primary" :icon="Download" @click="handleExport" :loading="exportLoading">导出记录</el-button>
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
        <el-input
          v-model="searchBrand"
          placeholder="搜索品牌"
          class="search-input"
          clearable
          @clear="loadGoodsList"
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model="searchModel"
          placeholder="搜索型号"
          class="search-input"
          clearable
          @clear="loadGoodsList"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="table-scroll">
      <el-table :data="goodsList" stripe style="margin-top: 20px; width: 100%">
        <el-table-column type="index" label="序号" width="80" :index="tableIndex" />
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
        <el-table-column v-if="!isOutboundStaff" label="操作" width="200" :fixed="isMobile ? false : 'right'">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-pagination
      v-model:current-page="pagination.currentPage"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      :layout="paginationLayout"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination-container"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑货物' : '添加货物'"
      :width="isMobile ? undefined : '500px'"
      :fullscreen="isMobile"
      append-to-body
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="goodsForm"
        :rules="rules"
        :label-width="isMobile ? undefined : '80px'"
        :label-position="isMobile ? 'top' : 'right'"
      >
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
import { ref, reactive, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import { addGoods, deleteGoods, exportGoods, getGoodsList, getGoodsStockCount, updateGoods } from '@/api/goods'
import { useExport } from '@/composables/useExport'
import { usePagination } from '@/composables/usePagination'
import { useViewport } from '@/composables/useViewport'
import { formatDate } from '@/utils/date'

const route = useRoute()
const currentType = computed(() => route.meta.type || 'device')
const { isMobile } = useViewport()
const paginationLayout = computed(() => (isMobile.value ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'))

const goodsList = ref([])
const searchName = ref('')
const searchBrand = ref('')
const searchModel = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const totalStockCount = ref(0)

const { exportLoading, handleExport: doExport } = useExport()
const { pagination, handleSizeChange, handleCurrentChange, resetPage } = usePagination(loadGoodsList)
const tableIndex = (index) => (pagination.currentPage - 1) * pagination.pageSize + index + 1

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

const isOutboundStaff = computed(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    const userInfo = JSON.parse(userInfoStr)
    return userInfo.roleName === '出库员' || userInfo.roleName === '销售'
  }
  return false
})

async function handleExport() {
  await doExport(exportGoods, '货物列表', {
    type: currentType.value,
    name: searchName.value || undefined,
    brand: searchBrand.value || undefined,
    model: searchModel.value || undefined
  })
}

async function loadGoodsList() {
  loading.value = true
  try {
    const res = await getGoodsList({
      type: currentType.value,
      current: pagination.currentPage,
      size: pagination.pageSize,
      name: searchName.value,
      brand: searchBrand.value,
      model: searchModel.value
    })
    if (res.data.records) {
      goodsList.value = res.data.records
      pagination.total = res.data.total
    } else {
      goodsList.value = res.data
      pagination.total = res.data.length
    }
  } catch (error) {
    console.error('获取货物列表失败', error)
  } finally {
    loading.value = false
  }
}

async function loadStockCount() {
  try {
    const res = await getGoodsStockCount(currentType.value)
    if (res.code === 200) {
      totalStockCount.value = res.data
    }
  } catch (error) {
    console.error('获取库存统计失败', error)
  }
}

function handleSearch() {
  resetPage()
  loadGoodsList()
}

function handleReset() {
  searchName.value = ''
  searchBrand.value = ''
  searchModel.value = ''
  resetPage()
  loadGoodsList()
}

watch(() => route.path, () => {
  resetPage()
  loadGoodsList()
  loadStockCount()
}, { immediate: true })

function showAddDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(goodsForm, row)
  dialogVisible.value = true
}

function handleDelete(row) {
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
      // ignore
    }
  })
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const api = isEdit.value ? updateGoods : addGoods
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
        // ignore
      }
    }
  })
}

function resetForm() {
  goodsForm.id = ''
  goodsForm.name = ''
  goodsForm.brand = ''
  goodsForm.model = ''
  goodsForm.batch = ''
  goodsForm.unitPrice = 0
  goodsForm.remainingStock = 0
  goodsForm.totalStock = 0
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 24px;
}

.toolbar-left {
  gap: 14px;
}

.stock-count-text {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  height: 40px;
  font-size: 13px;
  color: #647694;
  border-radius: 14px;
  background: rgba(244, 247, 253, 0.82);
  box-shadow: inset 0 0 0 1px rgba(208, 218, 236, 0.72);
}

.count-number {
  font-size: 16px;
  font-weight: 700;
  color: #4b63c5;
}

.search-area {
  gap: 12px;
}

.search-input {
  width: 200px;
}

.pagination-container {
  margin-top: 24px;
}
</style>
