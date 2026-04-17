<script setup>
import { ref, computed, reactive, onMounted } from "vue";
import { useUserStore } from "@/stores/user";
import { competitionApi } from "@/api/competition";
import { useRouter } from "vue-router";

const userStore = useUserStore();
const router = useRouter();

const activeTab = ref("list");
const competitions = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(12);
const showRegister = ref(false);
const showDetail = ref(false); 
const selectedComp = ref(null); 
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

// --- ✨ 完整恢复：账号维护表单 ---
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

const filteredCompetitions = computed(() => competitions.value);

const fetchCompetitions = async () => {
  loading.value = true;
  try {
    const res = await competitionApi.getList({
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchQuery.value
    });
    competitions.value = res.content || [];
    total.value = res.totalElements || 0;
  } catch (error) {
    console.error("加载竞赛列表失败:", error);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchCompetitions();
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchCompetitions();
};

const openDetailModal = (item) => {
  selectedComp.value = item;
  showDetail.value = true;
};

const handleRegisterFromDetail = () => {
  showDetail.value = false;
  openRegisterModal(selectedComp.value);
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

// --- ✨ 完整恢复：修改逻辑 ---
const handleUpdateUsername = async () => {
  if (!profileForm.username) return alert("账号名不能为空");
  try {
    loading.value = true;
    await competitionApi.updateStudentAccount(profileForm);
    alert("✅ 账号名修改成功，请重新登录");
    handleLogout();
  } catch (error) {
    alert("修改失败：账号可能已存在");
  } finally {
    loading.value = false;
  }
};

const handleUpdatePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) return alert("请填写完整");
  if (pwdForm.newPassword !== pwdForm.confirmPassword)
    return alert("两次新密码不一致");

  try {
    loading.value = true;
    await competitionApi.updateStudentAccount(pwdForm);
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

onMounted(() => fetchCompetitions());
</script>

<template>
  <div class="student-container">
    <header class="page-header">
      <div class="welcome">
        <h1>👋 同学，你好</h1>
        <p>{{ activeTab === "list" ? "探索属于你的学科舞台" : "管理你的账号安全" }}</p>
      </div>

      <div class="header-actions">
        <button class="tab-btn" :class="{ active: activeTab === 'list' }" @click="activeTab = 'list'">
          <i-lucide-layout-grid class="btn-icon-s" /> 竞赛列表
        </button>
        <button class="tab-btn" :class="{ active: activeTab === 'settings' }" @click="activeTab = 'settings'">
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
          <input v-model="searchQuery" type="text" placeholder="回车搜索竞赛名称..." @keyup.enter="handleSearch" />
          <i-lucide-x v-if="searchQuery" class="clear-icon" @click="searchQuery = ''; handleSearch();" />
        </div>
      </div>

      <div v-if="loading" class="loading-box">
        <i-lucide-loader-2 class="spinner" /> 正在搜寻竞赛...
      </div>

      <div v-else-if="filteredCompetitions.length > 0" class="card-grid">
        <div v-for="item in filteredCompetitions" :key="item.id" class="comp-card" :class="{ 'is-ended': item.status === 0 }">
          <div class="card-icon-header">
            <i-lucide-award :class="['svg-icon', item.status === 1 ? 'main-color' : 'gray-color']" />
            <span class="status-tag" :class="item.status === 1 ? 'active' : 'ended'">
              <i-lucide-dot /> {{ item.status === 1 ? "报名中" : "已截止" }}
            </span>
          </div>
          <h3>{{ item.title }}</h3>
          <p class="card-desc">{{ item.description || "暂无描述" }}</p>
          
          <div class="card-footer">
            <button class="detail-link-btn" @click="openDetailModal(item)">查看详情 & 规则</button>
            <div class="meta">
              <i-lucide-calendar-days class="small-icon" />
              <span>{{ item.startTime?.substring(0, 10) }}</span>
            </div>
            <button @click="openRegisterModal(item)" class="join-btn" :disabled="item.status === 0">
              立即报名 <i-lucide-arrow-right v-if="item.status === 1" class="btn-icon" />
            </button>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">暂时没有找到相关竞赛</div>

      <div class="pagination-wrapper" v-if="total > pageSize">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" @current-change="handlePageChange" />
      </div>
    </div>

    <!-- ✨ 竞赛详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal-content detail-modal">
        <div class="detail-header">
          <div class="title-row">
            <h2>{{ selectedComp?.title }}</h2>
            <span class="status-badge" :class="selectedComp?.status === 1 ? 'active' : 'ended'">
              {{ selectedComp?.status === 1 ? "进行中" : "已结束" }}
            </span>
          </div>
          <div class="time-info">
            <span><strong>开始：</strong>{{ selectedComp?.startTime }}</span>
            <span><strong>截止：</strong>{{ selectedComp?.endTime }}</span>
          </div>
        </div>
        
        <div class="detail-body">
          <label>竞赛详情介绍</label>
          <div class="full-desc">{{ selectedComp?.description || '管理员很懒，没有填写详细描述...' }}</div>
          
          <div v-if="selectedComp?.fileUrl" class="attachment-box">
            <label>规则附件</label>
            <a :href="'http://localhost:8080' + selectedComp.fileUrl" target="_blank" class="download-link">
              <i-lucide-file-text /> 点击下载/查看竞赛规则 (PDF)
            </a>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="showDetail = false" class="btn-cancel">返回列表</button>
          <button v-if="selectedComp?.status === 1" @click="handleRegisterFromDetail" class="btn-submit">立即报名</button>
        </div>
      </div>
    </div>

    <!-- 报名弹窗 -->
    <div v-if="showRegister" class="modal-overlay" @click.self="showRegister = false">
      <div class="modal-content">
        <h3>确认报名：{{ regForm.competitionTitle }}</h3>
        <div class="form-item">
          <label>姓名</label>
          <input v-model="regForm.studentName" disabled class="readonly-input" />
        </div>
        <div class="form-item">
          <label>班级</label>
          <input v-model="regForm.className" placeholder="例如：计算机2301" />
        </div>
        <div class="form-item">
          <label>手机号</label>
          <input v-model="regForm.phone" placeholder="联系电话" />
        </div>
        <div class="modal-actions">
          <button @click="showRegister = false" class="btn-cancel">取消</button>
          <button @click="submitRegistration" class="btn-submit">提交报名</button>
        </div>
      </div>
    </div>
    
    <!-- ✨ 完整恢复：个人设置视图 -->
    <div v-if="activeTab === 'settings'" class="settings-view">
      <div class="settings-card">
        <section class="settings-section">
          <h3><i-lucide-user-cog class="section-icon" /> 修改登录账号</h3>
          <div class="form-item">
            <label>新用户名</label>
            <input v-model="profileForm.username" placeholder="请输入新账号名称" />
          </div>
          <button @click="handleUpdateUsername" class="btn-submit-s" :disabled="loading">
            保存账号修改
          </button>
        </section>

        <div class="divider"></div>

        <section class="settings-section">
          <h3><i-lucide-shield-check class="section-icon" /> 修改登录密码</h3>
          <div class="form-item">
            <label>当前密码</label>
            <input v-model="pwdForm.oldPassword" type="password" placeholder="验证原密码" />
          </div>
          <div class="form-item">
            <label>设置新密码</label>
            <input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" />
          </div>
          <div class="form-item">
            <label>确认新密码</label>
            <input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" />
          </div>
          <button @click="handleUpdatePassword" class="btn-submit-s danger" :disabled="loading">
            更新登录密码
          </button>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.student-container { padding: 40px 20px; max-width: 1100px; margin: 0 auto; background: #f9fbff; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.header-actions { display: flex; gap: 12px; align-items: center; }
.user-info-tag { display: flex; align-items: center; gap: 8px; background: white; padding: 8px 16px; border-radius: 20px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02); cursor: pointer; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }
.comp-card { background: white; border-radius: 16px; padding: 24px; border: 1px solid #edf2f7; display: flex; flex-direction: column; transition: 0.3s; }
.comp-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.05); }
.card-desc { font-size: 14px; color: #64748b; margin: 12px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-footer { margin-top: auto; display: flex; flex-direction: column; gap: 10px; }
.detail-link-btn { background: none; border: none; color: #3b82f6; font-size: 13px; font-weight: bold; cursor: pointer; text-align: left; padding: 0; }
.meta { display: flex; align-items: center; font-size: 12px; color: #94a3b8; }
.join-btn { width: 100%; padding: 10px; border-radius: 8px; border: none; background: #3b82f6; color: white; cursor: pointer; }

/* 详情弹窗样式 */
.detail-modal { width: 600px !important; }
.title-row { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.status-badge { font-size: 12px; padding: 4px 12px; border-radius: 10px; }
.status-badge.active { background: #dcfce7; color: #166534; }
.status-badge.ended { background: #f1f5f9; color: #64748b; }
.time-info { font-size: 13px; color: #64748b; display: flex; gap: 20px; margin-bottom: 20px; }
.detail-body label { display: block; font-size: 14px; font-weight: bold; margin-bottom: 8px; color: #1e293b; }
.full-desc { background: #f8fafc; padding: 15px; border-radius: 10px; line-height: 1.6; color: #334155; font-size: 15px; max-height: 300px; overflow-y: auto; margin-bottom: 20px; white-space: pre-wrap; }
.attachment-box { border-top: 1px solid #f1f5f9; pt: 15px; }
.download-link { display: flex; align-items: center; gap: 8px; color: #3b82f6; text-decoration: none; font-weight: 600; font-size: 14px; padding: 10px; background: #eff6ff; border-radius: 8px; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); backdrop-filter: blur(4px); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; padding: 30px; border-radius: 20px; width: 400px; }
.form-item { margin-bottom: 16px; }
.form-item label { display: block; font-size: 14px; color: #475569; margin-bottom: 8px; }
.form-item input { width: 100%; box-sizing: border-box; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 8px; }
.readonly-input { background: #f8fafc; color: #94a3b8; }
.modal-actions { display: flex; gap: 12px; margin-top: 24px; }
.btn-submit { flex: 1; padding: 12px; border: none; background: #3b82f6; color: white; border-radius: 10px; cursor: pointer; font-weight: bold; }
.btn-cancel { flex: 1; padding: 12px; border: 1px solid #e2e8f0; background: white; border-radius: 10px; cursor: pointer; }
.search-bar { display: flex; align-items: center; background: white; padding: 10px 15px; border-radius: 10px; border: 1px solid #e2e8f0; width: 300px; }
.search-bar input { border: none; outline: none; margin-left: 10px; flex: 1; }
.pagination-wrapper { margin-top: 30px; display: center; display: flex; justify-content: center; }
.tab-btn { padding: 8px 16px; border-radius: 10px; border: 1px solid #e2e8f0; background: white; cursor: pointer; display: flex; align-items: center; gap: 5px; }
.tab-btn.active { background: #3b82f6; color: white; }
.settings-view { display: flex; justify-content: center; padding: 20px 0; }
.settings-card { background: white; width: 100%; max-width: 500px; padding: 40px; border-radius: 24px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04); }
.settings-section h3 { display: flex; align-items: center; gap: 10px; font-size: 18px; margin-bottom: 20px; color: #1e293b; }
.divider { height: 1px; background: #f1f5f9; margin: 30px 0; }
.btn-submit-s { width: 100%; padding: 12px; border: none; background: #3b82f6; color: white; border-radius: 10px; font-weight: 600; cursor: pointer; }
.btn-submit-s.danger { background: #64748b; }
.section-icon { width: 20px; height: 20px; color: #3b82f6; }
</style>
