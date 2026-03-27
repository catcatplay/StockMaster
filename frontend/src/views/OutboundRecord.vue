<template>
  <div class="card-container outbound-record">
    <div class="toolbar">
      <el-button v-if="!isCancelPage" type="success" :icon="Plus" @click="showOutboundDialog">新增出库</el-button>
      <el-button type="primary" :icon="Download" @click="handleExport" :loading="exportLoading">导出记录</el-button>
      <div v-if="isMobile" class="toolbar-date-range-mobile">
        <el-date-picker
          v-model="mobileStartTime"
          type="datetime"
          placeholder="开始时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="toolbar-date-range-mobile__field"
        />
        <el-date-picker
          v-model="mobileEndTime"
          type="datetime"
          placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="toolbar-date-range-mobile__field"
        />
      </div>
      <el-date-picker
        v-else
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        class="toolbar-date-range"
        @change="handleDateChange"
      />
    </div>

    <div class="table-scroll">
      <el-table :data="recordList" stripe style="margin-top: 20px; width: 100%">
        <el-table-column type="index" label="序号" width="80" :index="tableIndex" />
        <el-table-column prop="goodsName" label="货物名称" min-width="100" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" min-width="100" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" min-width="150" show-overflow-tooltip />
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
        <el-table-column prop="cancelTime" label="取消时间" width="180" v-if="isCancelPage">
          <template #default="scope">
            {{ scope.row.cancelTime ? formatDate(scope.row.cancelTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" v-if="isAdmin && !isCancelPage">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              :disabled="scope.row.status === OUTBOUND_STATUS.CANCELED"
              @click="handleCancel(scope.row)"
            >
              取消出库
            </el-button>
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
      v-if="!isCancelPage"
      v-model="dialogVisible"
      title="新增出库"
      :width="isMobile ? undefined : '500px'"
      :fullscreen="isMobile"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="outboundForm"
        :rules="rules"
        :label-width="isMobile ? undefined : '100px'"
        :label-position="isMobile ? 'top' : 'right'"
      >
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
              <div class="goods-option">
                <span class="goods-option__main">{{ item.name }} - {{ item.brand }} - {{ item.model }}</span>
                <span class="goods-option__stock">库存: {{ item.remainingStock }}</span>
              </div>
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
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus } from '@element-plus/icons-vue'
import { getGoodsList } from '@/api/goods'
import { createOutbound, exportOutboundRecords, getOutboundList, cancelOutbound } from '@/api/outbound'
import { useExport } from '@/composables/useExport'
import { usePagination } from '@/composables/usePagination'
import { useViewport } from '@/composables/useViewport'
import { formatDate, formatDateForApi } from '@/utils/date'

const OUTBOUND_STATUS = {
  NORMAL: 0,
  CANCELED: 1
}

const CANCEL_CONFIRM_TEXT = '确认取消出库'

const route = useRoute()
const currentType = computed(() => route.meta.type || 'device')
const currentStatus = computed(() => route.meta.outboundStatus)
const isCancelPage = computed(() => currentStatus.value === OUTBOUND_STATUS.CANCELED)
const { isMobile } = useViewport()
const paginationLayout = computed(() => (isMobile.value ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'))

const isAdmin = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.roleId === 1
})

const recordList = ref([])
const goodsList = ref([])
const dateRange = ref([])
const mobileStartTime = ref('')
const mobileEndTime = ref('')
const dialogVisible = ref(false)
const formRef = ref(null)
const currentStock = ref(0)

const { exportLoading, handleExport: doExport } = useExport()
const { pagination, handleSizeChange, handleCurrentChange, resetPage } = usePagination(loadRecordList)
const tableIndex = (index) => (pagination.currentPage - 1) * pagination.pageSize + index + 1

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

async function loadRecordList() {
  try {
    const params = {
      type: currentType.value,
      status: currentStatus.value,
      current: pagination.currentPage,
      size: pagination.pageSize
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = formatDateForApi(dateRange.value[0])
      params.endTime = formatDateForApi(dateRange.value[1])
    }

    const res = await getOutboundList(params)
    if (res.data.records) {
      recordList.value = res.data.records
      pagination.total = res.data.total
    } else {
      recordList.value = res.data
      pagination.total = res.data.length
    }
  } catch (error) {
    console.error('加载出库记录失败', error)
  }
}

async function loadGoodsList() {
  try {
    const res = await getGoodsList({ type: currentType.value })
    goodsList.value = res.data.filter(g => g.remainingStock > 0)
  } catch (error) {
    console.error('加载货物列表失败', error)
  }
}

watch(() => route.path, () => {
  resetPage()
  loadRecordList()
  if (!isCancelPage.value) {
    loadGoodsList()
  } else {
    goodsList.value = []
  }
}, { immediate: true })

watch([mobileStartTime, mobileEndTime], ([startTime, endTime]) => {
  if (!isMobile.value) {
    return
  }

  if ((startTime && !endTime) || (!startTime && endTime)) {
    return
  }

  dateRange.value = startTime && endTime ? [startTime, endTime] : []
  handleDateChange()
})

watch(isMobile, (mobile) => {
  if (!mobile) {
    mobileStartTime.value = ''
    mobileEndTime.value = ''
  } else if (dateRange.value.length === 2) {
    ;[mobileStartTime.value, mobileEndTime.value] = dateRange.value
  }
}, { immediate: true })

function handleDateChange() {
  resetPage()
  loadRecordList()
}

function showOutboundDialog() {
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
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

function handleGoodsChange(goodsId) {
  const goods = goodsList.value.find(g => g.id === goodsId)
  if (goods) {
    currentStock.value = goods.remainingStock
    if (outboundForm.value.quantity > goods.remainingStock) {
      outboundForm.value.quantity = 1
    }
    if (formRef.value) {
      formRef.value.validateField('quantity')
    }
  }
}

function handleQuantityChange() {
  if (formRef.value) {
    formRef.value.validateField('quantity')
  }
}

async function handleSubmit() {
  if (!formRef.value) return

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
        loadGoodsList()
      } catch (error) {
        console.error('出库失败', error)
      }
    }
  })
}

async function handleExport() {
  const params = {
    type: currentType.value,
    status: currentStatus.value
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = formatDateForApi(dateRange.value[0])
    params.endTime = formatDateForApi(dateRange.value[1])
  }
  await doExport(exportOutboundRecords, '出库记录', params)
}

async function handleCancel(row) {
  try {
    if (row.status === OUTBOUND_STATUS.CANCELED) {
      ElMessage.warning('该出库记录已取消，请勿重复操作')
      return
    }

    await ElMessageBox.prompt(
      `取消后将恢复 ${row.quantity} 件库存到货物“${row.goodsName}”。请输入“${CANCEL_CONFIRM_TEXT}”后继续。`,
      '取消出库',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: `请输入：${CANCEL_CONFIRM_TEXT}`,
        inputPattern: new RegExp(`^${CANCEL_CONFIRM_TEXT}$`),
        inputErrorMessage: `请输入正确的确认内容：${CANCEL_CONFIRM_TEXT}`,
        type: 'warning'
      }
    )

    await cancelOutbound(row.id)
    ElMessage.success('取消出库成功')
    loadRecordList()
    if (!isCancelPage.value) {
      loadGoodsList()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('取消出库失败', error)
    }
  }
}
</script>

<style scoped>
.outbound-record {
  min-height: 100%;
}

.toolbar {
  margin-bottom: 22px;
}

.goods-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.goods-option__main,
.goods-option__stock {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-option__stock {
  color: #8492a6;
  font-size: 13px;
  flex-shrink: 0;
}

.toolbar :deep(.el-date-editor) {
  min-width: 320px;
}

.toolbar-date-range {
  min-width: 320px;
}

.toolbar-date-range-mobile {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.toolbar-date-range-mobile__field {
  width: 100%;
}

@media (max-width: 767px) {
  .goods-option {
    flex-direction: column;
    align-items: flex-start;
  }

  .goods-option__main,
  .goods-option__stock {
    white-space: normal;
  }

  .toolbar-date-range-mobile {
    grid-template-columns: 1fr;
  }
}
</style>
