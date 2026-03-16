<template>
  <div class="card-container role-manage">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchName"
          placeholder="请输入角色名称"
          class="toolbar-field"
          clearable
          @clear="loadData"
        />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <div class="toolbar-actions">
        <el-button type="success" @click="handleAdd">新增角色</el-button>
      </div>
    </div>

    <el-table :data="tableData" stripe style="margin-top: 20px; width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" min-width="160" />
      <el-table-column prop="roleCode" label="角色代码" min-width="190" />
      <el-table-column prop="description" label="描述" min-width="320" />
      <el-table-column prop="createTime" label="创建时间" min-width="190" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadData"
      @current-change="loadData"
      class="pagination-container"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色代码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色代码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="selectedPermissions">
            <el-checkbox label="goods">货物管理</el-checkbox>
            <el-checkbox label="inbound">入库记录</el-checkbox>
            <el-checkbox label="outbound">出库记录</el-checkbox>
            <el-checkbox label="user">用户管理</el-checkbox>
            <el-checkbox label="role">角色管理</el-checkbox>
          </el-checkbox-group>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole } from '../api/role'

const searchName = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  permissions: ''
})

const selectedPermissions = ref([])

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色代码', trigger: 'blur' }]
}

const loadData = async () => {
  const params = {
    page: page.value,
    size: size.value,
    roleName: searchName.value
  }

  const res = await getRoleList(params)
  if (res.code === 200) {
    tableData.value = res.data.records
    total.value = res.data.total
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.permissions = ''
  selectedPermissions.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  form.id = row.id
  form.roleName = row.roleName
  form.roleCode = row.roleCode
  form.description = row.description
  form.permissions = row.permissions

  try {
    selectedPermissions.value = JSON.parse(row.permissions || '[]')
  } catch {
    selectedPermissions.value = []
  }

  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    form.permissions = JSON.stringify(selectedPermissions.value)
    const res = form.id ? await updateRole(form) : await addRole(form)

    if (res.code === 200) {
      ElMessage.success(res.msg || '操作成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await deleteRole(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.role-manage {
  min-height: 100%;
}

.toolbar {
  margin-bottom: 22px;
}

.toolbar-left {
  gap: 12px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
}

.toolbar-field {
  width: 220px;
}
</style>
