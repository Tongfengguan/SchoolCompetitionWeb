<script setup>
import { ref, computed, reactive, onMounted } from "vue"; // 1. 引入 computed
import { useUserStore } from "@/stores/user";
import { competitionApi } from "@/api/competition";

const userStore = useUserStore();
const competitions = ref([]); // 存放从后端获取的【所有】原始数据
const showRegister = ref(false);
const loading = ref(false);

const searchQuery = ref("");

const regForm = reactive({
  competitionId: null,
  competitionTitle: "",
  studentName: "",
  studentId: "",
  className: "",
  phone: "",
});

// ✨ 核心逻辑：利用 computed 实现纯前端零延迟搜索
const filteredCompetitions = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase();
  if (!keyword) return competitions.value; // 如果没搜，返回全部

  return competitions.value.filter((item) => {
    // 同时匹配标题和描述（注意 description 可能是 null 的防御性处理）
    const matchTitle = item.title?.toLowerCase().includes(keyword);
    const matchDesc = item.description?.toLowerCase().includes(keyword);
    return matchTitle || matchDesc;
  });
});

// 只需要在页面加载时请求一次全部数据
const fetchCompetitions = async () => {
  loading.value = true;
  try {
    competitions.value = await competitionApi.getList(); // 不传参，拿全部
  } catch (error) {
    console.error("加载竞赛列表失败:", error);
  } finally {
    loading.value = false;
  }
};

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
        <p>探索属于你的学科舞台</p>
      </div>
      <div class="user-info-tag">
        <i-lucide-user class="svg-icon" />
        <span>{{ userStore.userInfo?.username }}</span>
      </div>
    </header>

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

      <div v-if="filteredCompetitions.length === 0" class="empty-state">
        <i-lucide-inbox
          class="empty-icon"
          style="margin-bottom: 10px; opacity: 0.5"
        />
        <div>暂无匹配的竞赛数据</div>
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
</style>
