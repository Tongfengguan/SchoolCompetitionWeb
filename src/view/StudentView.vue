<script setup>
import { ref, computed, reactive, onMounted } from "vue";
import { useUserStore } from "@/stores/user";
import { competitionApi } from "@/api/competition";
import { useRouter } from "vue-router";

const userStore = useUserStore();
const router = useRouter();

// ✨ 视图控制：'list' 为竞赛列表，'settings' 为个人设置
const activeTab = ref("list");

const competitions = ref([]);
const showRegister = ref(false);
const loading = ref(false);
const searchQuery = ref("");

// --- 报名表单 ---
const regForm = reactive({
  competitionId: null,
  competitionTitle: "",
  studentName: "",
  studentId: "",
  className: "",
  phone: "",
});

// --- ✨ 账号维护表单 ---
const profileForm = reactive({
  id: userStore.userInfo?.id,
  username: userStore.userInfo?.username || "",
});

const pwdForm = reactive({
  id: userStore.userInfo?.id,
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const filteredCompetitions = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase();
  if (!keyword) return competitions.value;
  return competitions.value.filter((item) => {
    const matchTitle = item.title?.toLowerCase().includes(keyword);
    const matchDesc = item.description?.toLowerCase().includes(keyword);
    return matchTitle || matchDesc;
  });
});

const fetchCompetitions = async () => {
  loading.value = true;
  try {
    competitions.value = await competitionApi.getList();
  } catch (error) {
    console.error("加载竞赛列表失败:", error);
  } finally {
    loading.value = false;
  }
};

// --- ✨ 修改用户名逻辑 ---
const handleUpdateUsername = async () => {
  if (!profileForm.username) return alert("账号名不能为空");
  try {
    loading.value = true;
    await competitionApi.updateProfile(profileForm);
    alert("✅ 账号名修改成功，请重新登录");
    handleLogout();
  } catch (error) {
    alert("修改失败：账号可能已存在");
  } finally {
    loading.value = false;
  }
};

// --- ✨ 修改密码逻辑 ---
const handleUpdatePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) return alert("请填写完整");
  if (pwdForm.newPassword !== pwdForm.confirmPassword)
    return alert("两次新密码不一致");

  try {
    loading.value = true;
    await competitionApi.updatePassword(pwdForm);
    alert("🔒 密码修改成功，请重新登录");
    handleLogout();
  } catch (error) {
    alert("修改失败：原密码错误");
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  userStore.clearUser();
  router.push("/login");
};

// 原有报名逻辑保持不变
const openRegisterModal = (item) => {
  if (item.status === 0) return;
  const user = userStore.userInfo;
  if (!user) return alert("请先登录！");
  regForm.competitionId = item.id;
  regForm.competitionTitle = item.title;
  regForm.studentName = user.name || user.username;
  regForm.studentId = user.username;
  showRegister.value = true;
};

const submitRegistration = async () => {
  if (!regForm.className || !regForm.phone) return alert("请填写完整信息");
  try {
    await competitionApi.register(regForm);
    alert("🎉 报名成功！");
    showRegister.value = false;
    regForm.className = "";
    regForm.phone = "";
  } catch (error) {
    alert("报名失败，请重试");
  }
};

onMounted(() => fetchCompetitions());
</script>

<template>
  <div class="student-container">
    <header class="page-header">
      <div class="welcome">
        <h1>👋 同学，你好</h1>
        <p>
          {{
            activeTab === "list" ? "探索属于你的学科舞台" : "管理你的账号安全"
          }}
        </p>
      </div>

      <div class="header-actions">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'list' }"
          @click="activeTab = 'list'"
        >
          <i-lucide-layout-grid class="btn-icon-s" /> 竞赛列表
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'settings' }"
          @click="activeTab = 'settings'"
        >
          <i-lucide-settings class="btn-icon-s" /> 账号维护
        </button>
        <div class="user-info-tag" @click="handleLogout" title="点击退出">
          <i-lucide-log-out class="svg-icon-s" />
          <span>退出登录</span>
        </div>
      </div>
    </header>

    <div v-if="activeTab === 'list'">
      <div class="search-section">
        <div class="search-bar">
          <i-lucide-search class="search-icon" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索竞赛名称或描述..."
          />
          <i-lucide-x
            v-if="searchQuery"
            class="clear-icon"
            @click="searchQuery = ''"
          />
        </div>
      </div>

      <div v-if="loading" class="loading-box">
        <i-lucide-loader-2 class="spinner" /> 正在搜寻竞赛...
      </div>

      <div v-else class="card-grid">
        <div
          v-for="item in filteredCompetitions"
          :key="item.id"
          class="comp-card"
          :class="{ 'is-ended': item.status === 0 }"
        >
          <div class="card-icon-header">
            <i-lucide-award
              :class="[
                'svg-icon',
                item.status === 1 ? 'main-color' : 'gray-color',
              ]"
            />
            <span
              class="status-tag"
              :class="item.status === 1 ? 'active' : 'ended'"
            >
              <i-lucide-dot /> {{ item.status === 1 ? "报名中" : "已截止" }}
            </span>
          </div>
          <h3>{{ item.title }}</h3>
          <p class="card-desc">{{ item.description || "暂无比赛描述" }}</p>
          <div class="card-footer">
            <div class="meta">
              <i-lucide-calendar-days class="small-icon" />
              <span>{{ item.startTime?.substring(0, 10) || "时间待定" }}</span>
            </div>
            <button
              @click="openRegisterModal(item)"
              class="join-btn"
              :disabled="item.status === 0"
            >
              {{ item.status === 1 ? "立即报名" : "已截止" }}
              <i-lucide-arrow-right v-if="item.status === 1" class="btn-icon" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'settings'" class="settings-view">
      <div class="settings-card">
        <section class="settings-section">
          <h3><i-lucide-user-cog class="section-icon" /> 修改登录账号</h3>
          <div class="form-item">
            <label>新用户名</label>
            <input
              v-model="profileForm.username"
              placeholder="请输入新账号名称"
            />
          </div>
          <button
            @click="handleUpdateUsername"
            class="btn-submit-s"
            :disabled="loading"
          >
            保存账号修改
          </button>
        </section>

        <div class="divider"></div>

        <section class="settings-section">
          <h3><i-lucide-shield-check class="section-icon" /> 修改登录密码</h3>
          <div class="form-item">
            <label>当前密码</label>
            <input
              v-model="pwdForm.oldPassword"
              type="password"
              placeholder="验证原密码"
            />
          </div>
          <div class="form-item">
            <label>设置新密码</label>
            <input
              v-model="pwdForm.newPassword"
              type="password"
              placeholder="请输入新密码"
            />
          </div>
          <div class="form-item">
            <label>确认新密码</label>
            <input
              v-model="pwdForm.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
            />
          </div>
          <button
            @click="handleUpdatePassword"
            class="btn-submit-s danger"
            :disabled="loading"
          >
            更新登录密码
          </button>
        </section>
      </div>
    </div>

    <div
      v-if="showRegister"
      class="modal-overlay"
      @click.self="showRegister = false"
    >
      <div class="modal-content">
        <h3>确认报名：{{ regForm.competitionTitle }}</h3>
        <div class="form-item">
          <label><i-lucide-user-check class="small-icon" /> 姓名</label>
          <input
            v-model="regForm.studentName"
            disabled
            class="readonly-input"
          />
        </div>
        <div class="form-item">
          <label><i-lucide-graduation-cap class="small-icon" /> 班级</label>
          <input v-model="regForm.className" placeholder="例如：计算机2301" />
        </div>
        <div class="form-item">
          <label><i-lucide-smartphone class="small-icon" /> 手机号</label>
          <input v-model="regForm.phone" placeholder="请输入联系方式" />
        </div>
        <div class="modal-actions">
          <button @click="showRegister = false" class="btn-cancel">返回</button>
          <button @click="submitRegistration" class="btn-submit">
            提交报名
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 容器与栅格布局 */
.student-container {
  padding: 40px 20px;
  max-width: 1100px;
  margin: 0 auto;
  background: #f9fbff;
  min-height: 100vh;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}
.user-info-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
  font-weight: 500;
  color: #334155;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

/* 卡片样式 */
.comp-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}
.comp-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
}
.comp-card.is-ended {
  opacity: 0.7;
}

/* 搜索框美化样式 */
.search-section {
  margin-bottom: 30px;
}
.search-bar {
  display: flex;
  align-items: center;
  background: white;
  padding: 12px 20px;
  border-radius: 12px;
  border: 2px solid #edf2f7;
  transition: all 0.3s ease;
  max-width: 500px;
}
.search-bar:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}
.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  margin-left: 12px;
  font-size: 16px;
  background: transparent;
  color: #334155;
}
.search-icon {
  color: #94a3b8;
  width: 20px;
}
.clear-icon {
  color: #cbd5e1;
  width: 18px;
  cursor: pointer;
  transition: color 0.2s;
}
.clear-icon:hover {
  color: #94a3b8;
}

/* ✨ 加载动画与居中 */
.loading-box {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  color: #64748b;
  font-size: 15px;
}
.spinner {
  animation: rotate 2s linear infinite;
  margin-right: 8px;
  color: #3b82f6;
}
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
  font-size: 15px;
}

/* SVG 图标控制 */
.svg-icon {
  width: 28px;
  height: 28px;
}
.main-color {
  color: #3b82f6;
}
.gray-color {
  color: #94a3b8;
}
.small-icon {
  width: 16px;
  height: 16px;
  margin-right: 6px;
}

.card-icon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-tag {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}
.status-tag.active {
  background: #e0f2fe;
  color: #0284c7;
}
.status-tag.ended {
  background: #f1f5f9;
  color: #64748b;
}

.comp-card h3 {
  margin: 0;
  font-size: 18px;
  color: #1e293b;
}

.card-desc {
  font-size: 14px;
  color: #64748b;
  margin: 12px 0 20px;
  min-height: 40px;
  line-height: 1.5;
}

/* 底部 Meta 信息 */
.card-footer {
  margin-top: auto;
}
.meta {
  display: flex;
  align-items: center;
  color: #94a3b8;
  font-size: 13px;
  margin-bottom: 12px;
}

/* 按钮动画 */
.join-btn {
  width: 100%;
  padding: 12px;
  border-radius: 10px;
  border: none;
  background: #3b82f6;
  color: white;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}
.join-btn:hover:not(:disabled) {
  background: #2563eb;
}
.join-btn:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
}

.btn-icon {
  width: 18px;
  margin-left: 8px;
  transition: transform 0.3s;
}
.join-btn:hover:not(:disabled) .btn-icon {
  transform: translateX(5px);
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}
.modal-content {
  background: white;
  padding: 30px;
  border-radius: 20px;
  width: 400px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}
.modal-content h3 {
  margin-top: 0;
  color: #1e293b;
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 16px;
}
.form-item label {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #475569;
  margin-bottom: 8px;
}
.form-item input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.2s;
}
.form-item input:focus {
  border-color: #3b82f6;
}
.readonly-input {
  background: #f8fafc;
  color: #94a3b8;
  cursor: not-allowed;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
.btn-cancel {
  flex: 1;
  padding: 10px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #475569;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
}
.btn-submit {
  flex: 1;
  padding: 10px;
  border: none;
  background: #3b82f6;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
}
.btn-submit:hover {
  background: #2563eb;
}
.btn-cancel:hover {
  background: #f8fafc;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.tab-btn {
  background: white;
  border: 1px solid #edf2f7;
  padding: 8px 16px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #64748b;
  transition: all 0.2s;
}

.tab-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.btn-icon-s {
  width: 16px;
  height: 16px;
}

.settings-view {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  animation: slideUp 0.4s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.settings-card {
  background: white;
  width: 100%;
  max-width: 500px;
  padding: 40px;
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.settings-section h3 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  margin-bottom: 20px;
  color: #1e293b;
}

.section-icon {
  width: 20px;
  color: #3b82f6;
}

.divider {
  height: 1px;
  background: #f1f5f9;
  margin: 30px 0;
}

.btn-submit-s {
  width: 100%;
  padding: 12px;
  border: none;
  background: #3b82f6;
  color: white;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 10px;
}

.btn-submit-s.danger {
  background: #64748b;
}

.btn-submit-s:hover {
  filter: brightness(0.9);
}

.user-info-tag {
  cursor: pointer;
}

.user-info-tag:hover {
  background: #fee2e2;
  color: #ef4444;
}
</style>
