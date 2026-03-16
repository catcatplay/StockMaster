<template>
  <div class="card-container user-manage">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchUsername"
          placeholder="请输入用户名"
          class="toolbar-field"
          clearable
          @clear="loadData"
        />
        <el-select
          v-model="searchRoleId"
          placeholder="请选择角色"
          class="toolbar-field"
          clearable
        >
          <el-option
            v-for="role in roleList"
            :key="role.id"
            :label="role.roleName"
            :value="role.id"
          />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <div class="toolbar-actions">
        <el-button type="success" @click="handleAdd">新增用户</el-button>
      </div>
    </div>

    <el-table :data="tableData" stripe style="margin-top: 20px; width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="160" />
      <el-table-column prop="realName" label="真实姓名" min-width="160" />
      <el-table-column prop="roleName" label="角色" min-width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="190" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" :prop="form.id ? '' : 'password'">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="form.id ? '不填写则不修改密码' : '请输入密码'"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%" @change="handleRoleChange">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
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
import { getUserList, addUser, updateUser, deleteUser, updateUserStatus } from '../api/user'
import { getAllRoles } from '../api/role'

const searchUsername = ref('')
const searchRoleId = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const roleList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  roleId: null,
  roleName: ''
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadRoles = async () => {
  const res = await getAllRoles()
  if (res.code === 200) {
    roleList.value = res.data
  }
}

const loadData = async () => {
  const params = {
    page: page.value,
    size: size.value,
    username: searchUsername.value,
    roleId: searchRoleId.value
  }

  const res = await getUserList(params)
  if (res.code === 200) {
    tableData.value = res.data.records
    total.value = res.data.total
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.roleId = null
  form.roleName = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  form.id = row.id
  form.username = row.username
  form.password = ''
  form.realName = row.realName
  form.roleId = row.roleId
  form.roleName = row.roleName
  dialogVisible.value = true
}

const handleRoleChange = (roleId) => {
  const selectedRole = roleList.value.find((role) => role.id === roleId)
  if (selectedRole) {
    form.roleName = selectedRole.roleName
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    const res = form.id ? await updateUser(form) : await addUser(form)
    if (res.code === 200) {
      ElMessage.success(res.msg || '操作成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  })
}

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await updateUserStatus(row.id, newStatus)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        loadData()
      } else {
        ElMessage.error(res.msg || `${action}失败`)
      }
    })
    .catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await deleteUser(row.id)
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
  loadRoles()
  loadData()
})
</script>

<style scoped>
.user-manage {
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
