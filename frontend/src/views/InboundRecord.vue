<template>
  <div class="card-container">
    <div class="toolbar">
      <el-button type="success" :icon="Plus" @click="showInboundDialog">新增入库</el-button>
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
        <el-table-column label="操作" width="120" :fixed="isMobile ? false : 'right'">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleSettlement(scope.row)">结算</el-button>
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
      title="新增入库"
      :width="isMobile ? undefined : '500px'"
      :fullscreen="isMobile"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="inboundForm"
        :rules="rules"
        :label-width="isMobile ? undefined : '100px'"
        :label-position="isMobile ? 'top' : 'right'"
      >
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
              <div class="goods-option">
                <span class="goods-option__main">{{ item.name }} - {{ item.brand }} - {{ item.model }}</span>
                <span class="goods-option__stock">当前剩余库存: {{ item.remainingStock }}</span>
              </div>
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

    <el-dialog
      v-model="settlementVisible"
      title="修改结算状态"
      :width="isMobile ? undefined : '500px'"
      :fullscreen="isMobile"
      append-to-body
    >
      <el-form
        ref="settlementFormRef"
        :model="settlementForm"
        :label-width="isMobile ? undefined : '100px'"
        :label-position="isMobile ? 'top' : 'right'"
      >
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
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, Plus } from '@element-plus/icons-vue'
import { createInbound, exportInboundRecords, getInboundList, updateSettlement } from '@/api/inbound'
import { getGoodsList } from '@/api/goods'
import { useExport } from '@/composables/useExport'
import { usePagination } from '@/composables/usePagination'
import { useViewport } from '@/composables/useViewport'
import { formatDate, formatDateForApi } from '@/utils/date'

const route = useRoute()
const currentType = computed(() => route.meta.type || 'device')
const { isMobile } = useViewport()
const paginationLayout = computed(() => (isMobile.value ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'))

const recordList = ref([])
const goodsList = ref([])
const dateRange = ref([])
const mobileStartTime = ref('')
const mobileEndTime = ref('')
const dialogVisible = ref(false)
const settlementVisible = ref(false)
const formRef = ref(null)
const settlementFormRef = ref(null)
const currentStock = ref(0)

const { exportLoading, handleExport: doExport } = useExport()
const { pagination, handleSizeChange, handleCurrentChange, resetPage } = usePagination(loadRecordList)
const tableIndex = (index) => (pagination.currentPage - 1) * pagination.pageSize + index + 1

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

async function loadRecordList() {
  try {
    const params = {
      type: currentType.value,
      current: pagination.currentPage,
      size: pagination.pageSize
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = formatDateForApi(dateRange.value[0])
      params.endTime = formatDateForApi(dateRange.value[1])
    }

    const res = await getInboundList(params)
    if (res.data.records) {
      recordList.value = res.data.records
      pagination.total = res.data.total
    } else {
      recordList.value = res.data
      pagination.total = res.data.length
    }
  } catch (error) {
    console.error('加载入库记录失败', error)
  }
}

async function loadGoodsList() {
  try {
    const res = await getGoodsList({ type: currentType.value })
    goodsList.value = res.data
  } catch (error) {
    console.error('加载货物列表失败', error)
  }
}

watch(() => route.path, () => {
  resetPage()
  loadRecordList()
  loadGoodsList()
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

function showInboundDialog() {
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

function handleGoodsChange(goodsId) {
  const goods = goodsList.value.find(g => g.id === goodsId)
  if (goods) {
    currentStock.value = goods.remainingStock
  }
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await createInbound(inboundForm.value)
        ElMessage.success('入库成功')
        dialogVisible.value = false
        loadRecordList()
        loadGoodsList()
      } catch (error) {
        console.error('入库失败', error)
      }
    }
  })
}

async function handleExport() {
  const params = {
    type: currentType.value
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = formatDateForApi(dateRange.value[0])
    params.endTime = formatDateForApi(dateRange.value[1])
  }
  await doExport(exportInboundRecords, '入库记录', params)
}

function handleSettlement(row) {
  settlementForm.value = {
    id: row.id,
    goodsName: row.goodsName,
    settlementStatus: row.settlementStatus || '未结算',
    remark: row.remark || ''
  }
  settlementVisible.value = true
}

async function handleSettlementSubmit() {
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
</script>

<style scoped>
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
