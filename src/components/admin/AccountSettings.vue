<script setup>
import { reactive, ref } from "vue";
import { useUserStore } from "@/stores/user";
import { competitionApi } from "@/api/competition";
import { useRouter } from "vue-router";

const userStore = useUserStore();
const router = useRouter();
const loading = ref(false);
const activeSubTab = ref("profile"); // 'profile' or 'password'

const profileForm = reactive({
  id: userStore.userInfo?.id,
  name: userStore.userInfo?.name || "",
  phone: userStore.userInfo?.phone || "",
});

const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const updateProfile = async () => {
  if (!profileForm.name) return alert("姓名不能为空");
  try {
    loading.value = true;
    await competitionApi.updateProfile(profileForm);
    alert("✅ 个人信息更新成功");
    // 更新本地 store
    userStore.userInfo.name = profileForm.name;
    userStore.userInfo.phone = profileForm.phone;
  } finally {
    loading.value = false;
  }
};

const updatePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) return alert("请填写完整");
  if (pwdForm.newPassword !== pwdForm.confirmPassword)
    return alert("两次新密码不一致");

  try {
    loading.value = true;
    await competitionApi.updatePassword({
        id: userStore.userInfo.id,
        ...pwdForm
    });
    alert("🔒 密码修改成功，请重新登录");
    userStore.clearUser();
    router.push("/login");
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="view-section narrow-view-content">
    <div class="tabs-header">
      <div 
        class="tab-item" 
        :class="{ active: activeSubTab === 'profile' }"
        @click="activeSubTab = 'profile'"
      >个人信息维护</div>
      <div 
        class="tab-item" 
        :class="{ active: activeSubTab === 'password' }"
        @click="activeSubTab = 'password'"
      >安全密码修改</div>
    </div>

    <div v-if="activeSubTab === 'profile'" class="settings-card glass-effect">
      <div class="form-list">
        <div class="form-item">
          <label>登录账号 (不可修改)</label>
          <input :value="userStore.userInfo?.username" disabled class="readonly-input" />
        </div>
        <div class="form-item">
          <label>真实姓名 <span class="required-star">*</span></label>
          <input v-model="profileForm.name" placeholder="请输入您的姓名" />
        </div>
        <div class="form-item">
          <label>联系电话</label>
          <input v-model="profileForm.phone" placeholder="请输入手机号" />
        </div>
        <button @click="updateProfile" class="btn-submit" :disabled="loading">
          {{ loading ? "正在保存..." : "保存更新" }}
        </button>
      </div>
    </div>

    <div v-if="activeSubTab === 'password'" class="settings-card glass-effect">
      <div class="form-list">
        <div class="form-item">
          <label>原始密码</label>
          <input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前旧密码" />
        </div>
        <div class="form-item">
          <label>设置新密码</label>
          <input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" />
        </div>
        <div class="form-item">
          <label>确认新密码</label>
          <input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </div>
        <button @click="updatePassword" class="btn-submit danger-btn" :disabled="loading">
          确认重置密码
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.narrow-view-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.tabs-header {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  border-bottom: 1px solid #e2e8f0;
  width: 100%;
  max-width: 500px;
}
.tab-item {
  padding: 10px 20px;
  cursor: pointer;
  color: #64748b;
}
.tab-item.active {
  color: #3b82f6;
  border-bottom: 2px solid #3b82f6;
}
.settings-card {
  width: 100%;
  max-width: 500px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.form-list { display: flex; flex-direction: column; gap: 20px; }
.form-item { display: flex; flex-direction: column; gap: 8px; }
.form-item label { font-size: 14px; font-weight: 600; color: #475569; }
.form-item input { padding: 10px; border: 1px solid #e2e8f0; border-radius: 8px; }
.readonly-input { background: #f1f5f9; color: #94a3b8; cursor: not-allowed; }
.btn-submit {
  padding: 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}
.danger-btn { background: #ef4444; }
.required-star { color: #ef4444; }
</style>
