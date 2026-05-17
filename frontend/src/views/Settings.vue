<template>
  <div class="settings-page">
    <div class="settings-card">
      <h2>个人设置</h2>

      <el-tabs>
        <el-tab-pane label="个人资料">
          <el-form :model="profileForm" label-position="top">
            <el-form-item label="个人简介">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="介绍一下自己..."
              />
            </el-form-item>
            <el-form-item label="头像">
              <el-upload
                :show-file-list="false"
                :before-upload="handleAvatarUpload"
                accept="image/*"
              >
                <el-avatar :size="80" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <span class="upload-hint">点击更换头像</span>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveProfile">
                保存
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码">
          <el-form :model="passwordForm" label-position="top">
            <el-form-item label="旧密码">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少6个字符" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userAPI } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const profileForm = ref({
  bio: userStore.userInfo?.bio || '',
})
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const saving = ref(false)
const changingPwd = ref(false)

async function handleSaveProfile() {
  saving.value = true
  try {
    await userAPI.updateProfile({ bio: profileForm.value.bio })
    if (userStore.userInfo) {
      userStore.userInfo.bio = profileForm.value.bio
      userStore.setUserInfo(userStore.userInfo)
    }
    ElMessage.success('保存成功')
  } catch {} finally {
    saving.value = false
  }
}

async function handleAvatarUpload(file: File) {
  const formData = new FormData()
  formData.append('avatar', file)
  try {
    const result = await userAPI.uploadAvatar(formData) as any
    if (userStore.userInfo) {
      userStore.userInfo.avatar = result.avatar
      userStore.setUserInfo(userStore.userInfo)
    }
    ElMessage.success('头像更新成功')
  } catch {}
  return false
}

async function handleChangePassword() {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写密码')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少需要6个字符')
    return
  }

  changingPwd.value = true
  try {
    await userAPI.changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch {} finally {
    changingPwd.value = false
  }
}
</script>

<style scoped>
.settings-page {
  max-width: 700px;
  margin: 0 auto;
}

.settings-card {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
}

.settings-card h2 {
  margin-bottom: 24px;
  color: #303133;
}

.upload-hint {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
</style>
