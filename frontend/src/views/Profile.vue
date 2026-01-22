<template>
  <div class="profile-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="roleName">
           <el-tag>{{ form.roleName || '暂无角色' }}</el-tag>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="不修改请留空" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getUserById, updateUser } from '../api/user'
import { ElMessage } from 'element-plus'

const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  realName: '',
  roleName: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback()
  } else {
    if (form.confirmPassword !== '') {
      if (!formRef.value) return
      formRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    if (form.password !== '') {
       callback(new Error('请再次输入密码'))
    } else {
       callback()
    }
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const loadUserInfo = async () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    const userInfo = JSON.parse(userInfoStr)
    form.id = userInfo.userId || userInfo.id // 兼容 userId 和 id
    try {
      const res = await getUserById(form.id)
      if (res.code === 200) {
        const data = res.data
        form.username = data.username
        form.realName = data.realName
        form.roleName = data.roleName
        // Store original data to check for changes or restore
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('获取用户信息失败')
    }
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const updateData = {
            id: form.id,
            realName: form.realName
        }
        if (form.password) {
            updateData.password = form.password
        }
        
        const res = await updateUser(updateData)
        if (res.code === 200) {
          ElMessage.success('修改成功')
          // Update local storage
          const userInfoStr = localStorage.getItem('userInfo')
          if (userInfoStr) {
             const userInfo = JSON.parse(userInfoStr)
             userInfo.realName = form.realName
             localStorage.setItem('userInfo', JSON.stringify(userInfo))
             // Force reload to update header info
             setTimeout(() => {
                 window.location.reload()
             }, 1000)
          }
        } else {
          ElMessage.error(res.msg || '修改失败')
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('修改失败')
      }
    }
  })
}

const resetForm = () => {
  loadUserInfo()
  form.password = ''
  form.confirmPassword = ''
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.box-card {
  max-width: 600px;
  margin: 0 auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
