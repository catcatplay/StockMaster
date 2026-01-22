<template>
  <div class="outbound-record">
    <div class="toolbar">
      <el-button type="primary" @click="showOutboundDialog">新增出库</el-button>
      <el-button type="success" @click="handleExport" :loading="exportLoading">导出记录</el-button>
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

    <el-table :data="recordList" border style="margin-top: 20px;">
<!--      <el-table-column prop="id" label="ID" width="80" />-->
      <el-table-column prop="goodsName" label="货物名称" />
      <el-table-column prop="brand" label="品牌" />
      <el-table-column prop="model" label="型号" />
      <el-table-column prop="quantity" label="出库数量" width="120">
        <template #default="scope">
          <el-tag type="warning">{{ scope.row.quantity }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="department" label="使用部门" width="150" />
      <el-table-column prop="outboundUser" label="出库人" width="120" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="outboundTime" label="出库时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.outboundTime) }}
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

    <!-- 出库对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="新增出库"
      width="500px"
    >
      <el-form :model="outboundForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="选择货物" prop="goodsId">
          <el-select 
            v-model="outboundForm.goodsId" 
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
              <span style="float: left">{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                库存: {{ item.stockQuantity }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag :type="currentStock > 0 ? 'success' : 'danger'">
            {{ currentStock }}
          </el-tag>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number 
            v-model="outboundForm.quantity" 
            :min="1" 
            :max="currentStock > 0 ? currentStock : 1"
            :disabled="currentStock <= 0"
            style="width: 100%;"
            @change="handleQuantityChange"
          />
        </el-form-item>
        <el-form-item label="使用部门" prop="department">
          <el-input 
            v-model="outboundForm.department" 
            placeholder="请输入使用部门"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="出库时间" prop="outboundTime">
          <el-date-picker
            v-model="outboundForm.outboundTime"
            type="datetime"
            placeholder="请选择出库时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="outboundForm.remark" 
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOutboundList, createOutbound, getOutboundByTime } from '@/api/outbound'
import { getGoodsList } from '@/api/goods'
import request from '@/utils/request'

const route = useRoute()
const currentType = computed(() => route.meta.type || 'device')

const recordList = ref([])
const goodsList = ref([])
const dateRange = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const currentStock = ref(0)
const exportLoading = ref(false)

// 分页参数
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const outboundForm = ref({
  goodsId: null,
  quantity: 1,
  type: currentType.value,
  department: '',
  outboundTime: null,
  remark: ''
})

const rules = {
  goodsId: [{ required: true, message: '请选择货物', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入出库数量', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value <= 0) {
          callback(new Error('出库数量必须大于0'))
        } else if (value > currentStock.value) {
          callback(new Error(`出库数量不能超过库存数量(${currentStock.value})`))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  department: [{ required: true, message: '请输入使用部门', trigger: 'blur' }],
  outboundTime: [{ required: true, message: '请选择出库时间', trigger: 'change' }]
}

const loadRecordList = async () => {
  try {
    const res = await getOutboundList({ 
      type: currentType.value,
      current: pagination.value.currentPage,
      size: pagination.value.pageSize
    })
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
    console.error('加载出库记录失败', error)
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
  if (dateRange.value && dateRange.value.length === 2) {
    try {
      const startTime = formatDateForApi(dateRange.value[0])
      const endTime = formatDateForApi(dateRange.value[1])
      const res = await getOutboundByTime(startTime, endTime)
      recordList.value = res.data
      pagination.value.total = res.data.length
      pagination.value.currentPage = 1
    } catch (error) {
      console.error('查询失败', error)
    }
  } else {
    loadRecordList()
  }
}

const showOutboundDialog = () => {
  outboundForm.value = {
    goodsId: null,
    quantity: 1,
    type: currentType.value,
    department: '',
    outboundTime: formatDateForApi(new Date()),
    remark: ''
  }
  currentStock.value = 0
  dialogVisible.value = true
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleGoodsChange = (goodsId) => {
  const goods = goodsList.value.find(g => g.id === goodsId)
  if (goods) {
    currentStock.value = goods.stockQuantity
    // 如果当前数量超过库存，重置为1
    if (outboundForm.value.quantity > goods.stockQuantity) {
      outboundForm.value.quantity = 1
    }
    // 触发数量字段的验证
    if (formRef.value) {
      formRef.value.validateField('quantity')
    }
  }
}

const handleQuantityChange = () => {
  // 触发数量字段的验证
  if (formRef.value) {
    formRef.value.validateField('quantity')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  // 验证库存
  if (outboundForm.value.quantity > currentStock.value) {
    ElMessage.error(`出库数量不能超过库存数量(${currentStock.value})`)
    return
  }
  
  if (currentStock.value <= 0) {
    ElMessage.error('当前库存不足，无法出库')
    return
  }
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await createOutbound(outboundForm.value)
        ElMessage.success('出库成功')
        dialogVisible.value = false
        loadRecordList()
        loadGoodsList() // 刷新货物列表以更新库存
      } catch (error) {
        console.error('出库失败', error)
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
    let url = '/outbound/export'
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
    link.download = `出库记录_${Date.now()}.xlsx`
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
.outbound-record {
  padding: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
}
</style>
