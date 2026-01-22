<template>
  <div>
    <div class="toolbar">
      <el-input 
        v-model="searchName" 
        placeholder="请输入角色名称" 
        style="width: 200px; margin-right: 10px"
        clearable
        @clear="loadData">
      </el-input>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="handleAdd">新增角色</el-button>
    </div>

    <el-table :data="tableData" stripe style="margin-top: 20px; width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="roleName" label="角色名称" width="150"></el-table-column>
      <el-table-column prop="roleCode" label="角色代码" width="180"></el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column label="操作" width="200">
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
      style="margin-top: 20px; justify-content: center;">
    </el-pagination>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle"
      width="600px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称"></el-input>
        </el-form-item>
        <el-form-item label="角色代码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色代码"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述"></el-input>
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
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
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色代码', trigger: 'blur' }
  ]
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
  
  // 解析权限
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
    if (valid) {
      // 将权限数组转为JSON字符串
      form.permissions = JSON.stringify(selectedPermissions.value)
      
      const res = form.id ? await updateRole(form) : await addRole(form)
      
      if (res.code === 200) {
        ElMessage.success(res.msg || '操作成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该角色吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await deleteRole(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>
