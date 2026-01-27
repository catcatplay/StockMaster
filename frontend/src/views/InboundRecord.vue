<template>
  <div class="card-container">
    <div class="toolbar">
      <el-button type="success" @click="showInboundDialog">新增入库</el-button>
      <el-button type="primary" @click="handleExport" :loading="exportLoading">导出记录</el-button>
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        style="margin-left: 10px;"
        @change="handleDateChange"
      />
    </div>

    <el-table :data="recordList" stripe style="margin-top: 20px; width: 100%">
<!--      <el-table-column prop="id" label="ID" width="80" />-->
      <el-table-column prop="goodsName" label="货物名称" min-width="100" show-overflow-tooltip />
      <el-table-column prop="brand" label="品牌" min-width="100" show-overflow-tooltip />
      <el-table-column prop="model" label="型号" min-width="150" show-overflow-tooltip />
      <el-table-column prop="quantity" label="入库数量" width="120">
        <template #default="scope">
          <el-tag type="success">{{ scope.row.quantity }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="inboundUser" label="入库人" width="120" />
      <el-table-column prop="settlementStatus" label="结算状态" width="120">
        <template #default="scope">
          <el-tag 
            :type="scope.row.settlementStatus === '全部结算' ? 'success' : scope.row.settlementStatus === '部分结算' ? 'warning' : 'info'"
          >
            {{ scope.row.settlementStatus || '未结算' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="inboundTime" label="入库时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.inboundTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleSettlement(scope.row)">结算</el-button>
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

    <!-- 入库对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="新增入库"
      width="500px"
    >
      <el-form :model="inboundForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="选择货物" prop="goodsId">
          <el-select 
            v-model="inboundForm.goodsId" 
            placeholder="请选择货物"
            style="width: 100%;"
            filterable
            @change="handleGoodsChange"
          >
            <el-option
              v-for="item in goodsList"
              :key="item.id"
              :label="`${item.name} - ${item.brand} - ${item.model}`"
              :value="item.id"
            >
              <span style="float: left">{{ item.name }} - {{ item.brand }} - {{ item.model }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                当前剩余库存: {{ item.remainingStock }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag type="info">{{ currentStock }}</el-tag>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number 
            v-model="inboundForm.quantity" 
            :min="1" 
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="入库时间" prop="inboundTime">
          <el-date-picker
            v-model="inboundForm.inboundTime"
            type="datetime"
            placeholder="请选择入库时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="inboundForm.remark" 
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleSubmit">确定入库</el-button>
      </template>
    </el-dialog>
    
    <!-- 结算对话框 -->
    <el-dialog 
      v-model="settlementVisible" 
      title="修改结算状态"
      width="500px"
    >
      <el-form :model="settlementForm" ref="settlementFormRef" label-width="100px">
        <el-form-item label="货物名称">
          <el-input v-model="settlementForm.goodsName" disabled />
        </el-form-item>
        <el-form-item label="结算状态" prop="settlementStatus">
          <el-radio-group v-model="settlementForm.settlementStatus">
            <el-radio label="未结算">未结算</el-radio>
            <el-radio label="部分结算">部分结算</el-radio>
            <el-radio label="全部结算">全部结算</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="settlementForm.remark" 
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settlementVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSettlementSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getInboundList, createInbound, updateSettlement } from '@/api/inbound'
import { getGoodsList } from '@/api/goods'
import request from '@/utils/request'

const route = useRoute()
const currentType = computed(() => route.meta.type || 'device')

const recordList = ref([])
const goodsList = ref([])
const dateRange = ref([])
const dialogVisible = ref(false)
const settlementVisible = ref(false)
const formRef = ref(null)
const settlementFormRef = ref(null)
const currentStock = ref(0)
const exportLoading = ref(false)

// 分页参数
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const inboundForm = ref({
  goodsId: null,
  quantity: 1,
  type: currentType.value,
  inboundTime: null,
  remark: ''
})

const settlementForm = ref({
  id: null,
  goodsName: '',
  settlementStatus: '未结算',
  remark: ''
})

const rules = {
  goodsId: [{ required: true, message: '请选择货物', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
  inboundTime: [{ required: true, message: '请选择入库时间', trigger: 'change' }]
}

const loadRecordList = async () => {
  try {
    const params = {
      type: currentType.value,
      current: pagination.value.currentPage,
      size: pagination.value.pageSize
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = formatDateForApi(dateRange.value[0])
      params.endTime = formatDateForApi(dateRange.value[1])
    }

    const res = await getInboundList(params)
    if (res.data.records) {
      // 后端分页返回
      recordList.value = res.data.records
      pagination.value.total = res.data.total
    } else {
      // 兼容旧接口
      recordList.value = res.data
      pagination.value.total = res.data.length
    }
  } catch (error) {
    console.error('加载入库记录失败', error)
  }
}

const loadGoodsList = async () => {
  try {
    const res = await getGoodsList({ type: currentType.value })
    goodsList.value = res.data
  } catch (error) {
    console.error('加载货物列表失败', error)
  }
}

// 监听路由变化，重新加载数据
watch(() => route.path, () => {
  pagination.value.currentPage = 1
  loadRecordList()
  loadGoodsList()
}, { immediate: true })

const handleDateChange = async () => {
  pagination.value.currentPage = 1
  loadRecordList()
}

const showInboundDialog = () => {
  inboundForm.value = {
    goodsId: null,
    quantity: 1,
    type: currentType.value,
    inboundTime: formatDateForApi(new Date()),
    remark: ''
  }
  currentStock.value = 0
  dialogVisible.value = true
}

const handleGoodsChange = (goodsId) => {
  const goods = goodsList.value.find(g => g.id === goodsId)
  if (goods) {
    currentStock.value = goods.remainingStock
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await createInbound(inboundForm.value)
        ElMessage.success('入库成功')
        dialogVisible.value = false
        loadRecordList()
        loadGoodsList() // 刷新货物列表以更新库存
      } catch (error) {
        console.error('入库失败', error)
      }
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

const formatDateForApi = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    let url = '/inbound/export'
    const params = []
    // 添加type参数
    params.push(`type=${encodeURIComponent(currentType.value)}`)
    if (dateRange.value && dateRange.value.length === 2) {
      const startTime = formatDateForApi(dateRange.value[0])
      const endTime = formatDateForApi(dateRange.value[1])
      params.push(`startTime=${encodeURIComponent(startTime)}`)
      params.push(`endTime=${encodeURIComponent(endTime)}`)
    }
    if (params.length > 0) {
      url += '?' + params.join('&')
    }
    
    // 使用axios发起请求，获取blob数据
    const response = await request({
      url: url,
      method: 'get',
      responseType: 'blob'
    })
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `入库记录_${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const handleSettlement = (row) => {
  settlementForm.value = {
    id: row.id,
    goodsName: row.goodsName,
    settlementStatus: row.settlementStatus || '未结算',
    remark: row.remark || ''
  }
  settlementVisible.value = true
}

const handleSettlementSubmit = async () => {
  try {
    await updateSettlement(settlementForm.value)
    ElMessage.success('更新成功')
    settlementVisible.value = false
    loadRecordList()
  } catch (error) {
    console.error('更新失败', error)
    ElMessage.error('更新失败')
  }
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.currentPage = 1
  loadRecordList()
}

const handleCurrentChange = (val) => {
  pagination.value.currentPage = val
  loadRecordList()
}
</script>

<style scoped>
.inbound-record {
  padding: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
}
</style>
