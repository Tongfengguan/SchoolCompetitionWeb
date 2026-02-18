<script setup>
import { ref, reactive, onMounted } from "vue";
import { useUserStore } from "@/stores/user";
import { useRouter } from "vue-router";
import { exportToExcel } from "@/utils/excel";

// 引入图标
import {
  Menu as IconMenu,
  Trophy,
  UserFilled,
  Plus,
  List,
  SwitchButton,
} from "@element-plus/icons-vue";
import { competitionApi } from "@/api/competition";

const userStore = useUserStore();
const router = useRouter();

// 状态控制
const competitions = ref([]);
const showForm = ref(false);
const showListModal = ref(false);
const registrationList = ref([]);
const currentTitle = ref("");
const loading = ref(false);

const form = reactive({
  title: "",
  description: "",
  startTime: "",
  endTime: "",
});

const handleExport = () => {
  if (registrationList.value.length === 0) {
    alert("当前名单为空，无法导出");
    return;
  }

  // 定义表头映射：将后端字段名映射为中文 Excel 表头
  const headerMap = {
    studentName: "学生姓名",
    studentId: "学号",
    className: "班级",
    phone: "联系电话",
  };

  // 执行导出：文件名会自动加上当前竞赛标题
  exportToExcel(
    registrationList.value,
    `${currentTitle.value}_报名名单`,
    headerMap,
  );
};

// 逻辑方法
const fetchCompetitions = async () => {
  loading.value = true;
  try {
    competitions.value = await competitionApi.getList();
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  userStore.clearUser();
  router.push("/login");
};

const submitCompetition = async () => {
  if (!form.title) return alert("标题必填");
  if (!form.startTime || !form.endTime) return alert("请选择完整的比赛时间");

  await competitionApi.create(form);
  showForm.value = false;
  fetchCompetitions();
};

const viewRegistrations = async (item) => {
  currentTitle.value = item.title;
  registrationList.value = await competitionApi.getRegistrations(item.id);
  showListModal.value = true;
};

const removeStudent = async (regId) => {
  if (!confirm("确定要取消资格吗？")) return;
  await competitionApi.deleteRegistration(regId);
  registrationList.value = registrationList.value.filter((s) => s.id !== regId);
};

onMounted(() => fetchCompetitions());
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">
        <el-icon :size="24"><Trophy /></el-icon>
        <span>竞赛后台</span>
      </div>
      <nav class="menu">
        <div class="menu-item active">
          <el-icon><List /></el-icon> 竞赛管理
        </div>
        <div class="menu-item">
          <el-icon><UserFilled /></el-icon> 学生审核
        </div>
      </nav>
      <div class="logout-box" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon> 退出登录
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="breadcrumb">后台管理 / 竞赛列表</div>
        <div class="user-info">
          欢迎您，管理员 {{ userStore.userInfo?.username }}
        </div>
      </header>

      <div class="content-body">
        <div class="action-bar">
          <h2>竞赛发布与管理</h2>
          <button @click="showForm = true" class="add-btn">
            <el-icon><Plus /></el-icon> 发布新比赛
          </button>
        </div>

        <div class="table-card">
          <table class="modern-table">
            <thead>
              <tr>
                <th>竞赛名称</th>
                <th>发布时间</th>
                <th>状态</th>
                <th style="text-align: right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in competitions" :key="item.id">
                <td class="title-cell">{{ item.title }}</td>
                <td>{{ item.startTime }}</td>
                <td>
                  <span
                    :class="[
                      'status-tag',
                      item.status === 1 ? 'active' : 'end',
                    ]"
                  >
                    {{ item.status === 1 ? "进行中" : "已结束" }}
                  </span>
                </td>
                <td style="text-align: right">
                  <button @click="viewRegistrations(item)" class="text-btn">
                    查看名单
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>

    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div
        class="modal-content glass-effect"
        style="width: 550px; padding: 30px"
      >
        <div
          class="modal-header-modern"
          style="padding: 0 0 20px 0; border-bottom: none"
        >
          <div class="header-titles">
            <h3>✨ 发布新竞赛</h3>
            <span class="sub-text">请填写竞赛的详细信息与时间节点</span>
          </div>
        </div>

        <div class="form-list">
          <div class="form-item">
            <label>竞赛名称 <span class="required-star">*</span></label>
            <input
              v-model="form.title"
              placeholder="例如：2026年春季编程挑战赛"
            />
          </div>

          <div class="form-row">
            <div class="form-item half">
              <label>
                <i-lucide-calendar class="btn-icon-tiny" /> 开始时间
                <span class="required-star">*</span>
              </label>
              <input type="datetime-local" v-model="form.startTime" />
            </div>
            <div class="form-item half">
              <label>
                <i-lucide-calendar-off class="btn-icon-tiny" /> 结束时间
                <span class="required-star">*</span>
              </label>
              <input type="datetime-local" v-model="form.endTime" />
            </div>
          </div>

          <div class="form-item">
            <label>竞赛描述</label>
            <textarea
              v-model="form.description"
              placeholder="请输入竞赛的规则、对象、奖励等详情..."
              rows="4"
            ></textarea>
          </div>

          <div class="modal-actions">
            <button @click="showForm = false" class="btn-cancel">
              取消发布
            </button>
            <button @click="submitCompetition" class="btn-submit">
              确认发布
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showListModal"
      class="modal-overlay"
      @click.self="showListModal = false"
    >
      <div class="modal-content wide glass-effect">
        <div class="modal-header-modern">
          <div class="header-left">
            <div class="icon-box">
              <i-lucide-users class="header-icon" />
            </div>
            <div class="header-titles">
              <h3>{{ currentTitle }}</h3>
              <span class="sub-text">报名名单概览</span>
            </div>
          </div>
          <button @click="handleExport" class="export-btn-modern">
            <i-lucide-download class="btn-icon-small" />
            <span>导出 Excel</span>
          </button>
        </div>

        <div class="table-wrapper-modern">
          <table class="modern-table-clean">
            <thead>
              <tr>
                <th style="width: 35%">报名学生</th>
                <th style="width: 35%">学号</th>
                <th style="text-align: right">数据操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="stu in registrationList" :key="stu.id">
                <td class="name-cell">
                  <div class="avatar-placeholder">
                    {{ stu.studentName.charAt(0) }}
                  </div>
                  <span>{{ stu.studentName }}</span>
                </td>
                <td class="id-cell">{{ stu.studentId }}</td>
                <td style="text-align: right">
                  <button
                    @click="removeStudent(stu.id)"
                    class="action-btn-danger"
                  >
                    <i-lucide-user-x class="btn-icon-tiny" />
                    取消资格
                  </button>
                </td>
              </tr>
              <tr v-if="registrationList.length === 0">
                <td colspan="3">
                  <div class="empty-state">
                    <i-lucide-inbox class="empty-icon" />
                    <p>暂无学生报名该竞赛</p>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="modal-footer-modern">
          <button @click="showListModal = false" class="close-btn-simple">
            关闭窗口
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ---------------- 全局框架样式 ---------------- */
.admin-layout {
  display: flex;
  height: 100vh;
  background: #f4f7f9;
}

.sidebar {
  width: 240px;
  background: #001529;
  color: white;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 12px;
  font-size: 18px;
  font-weight: bold;
  background: #002140;
}

.menu {
  flex: 1;
  padding: 16px 0;
}

.menu-item {
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s;
  color: #a6adb4;
}

.menu-item.active {
  background: #1890ff;
  color: white;
}

.menu-item:hover {
  color: white;
}

.logout-box {
  padding: 20px 24px;
  border-top: 1px solid #ffffff1a;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #ff4d4f;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.top-header {
  height: 64px;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.content-body {
  padding: 24px;
  overflow-y: auto;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.table-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.modern-table {
  width: 100%;
  border-collapse: collapse;
}

.modern-table th {
  background: #fafafa;
  padding: 16px;
  text-align: left;
  font-weight: 600;
  color: #262626;
  border-bottom: 1px solid #f0f0f0;
}

.modern-table td {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.active {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}
.status-tag.end {
  background: #f5f5f5;
  color: #8c8c8c;
  border: 1px solid #d9d9d9;
}

.add-btn {
  background: #1890ff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.text-btn {
  background: none;
  border: none;
  color: #1890ff;
  cursor: pointer;
}

/* ---------------- 弹窗通用基础设置 ---------------- */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.glass-effect {
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  padding: 0;
  overflow: hidden;
}

.modal-header-modern {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
}

.header-titles h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.sub-text {
  color: #64748b;
  font-size: 13px;
  margin-top: 4px;
  display: block;
}

/* ---------------- ✨ 发布竞赛表单专属美化 ---------------- */
.form-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.half {
  flex: 1;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  display: flex;
  align-items: center;
  gap: 4px;
}

.required-star {
  color: #ef4444;
}

.form-item input,
.form-item textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  outline: none;
  font-family: inherit;
  font-size: 14px;
  color: #1e293b;
  transition: all 0.2s ease;
  background: #f8fafc;
}

.form-item textarea {
  resize: vertical;
}

.form-item input:focus,
.form-item textarea:focus {
  background: #fff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

input[type="datetime-local"] {
  color: #334155;
  font-family: inherit;
}

/* 表单底部按钮 */
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.btn-cancel {
  padding: 10px 24px;
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #64748b;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.btn-submit {
  padding: 10px 24px;
  background: #3b82f6;
  border: none;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-submit:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

/* ---------------- ✨ 查看名单专属样式 ---------------- */
.modal-content.wide {
  width: 600px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.icon-box {
  width: 40px;
  height: 40px;
  background: #eff6ff;
  color: #3b82f6;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon {
  width: 22px;
  height: 22px;
}

.export-btn-modern {
  background: #f8fafc;
  color: #0f172a;
  border: 1px solid #e2e8f0;
  padding: 8px 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s;
}

.export-btn-modern:hover {
  background: #10b981;
  color: white;
  border-color: #10b981;
}

.btn-icon-small {
  width: 16px;
  height: 16px;
}

.table-wrapper-modern {
  padding: 15px 30px 20px;
  max-height: 450px;
  overflow-y: auto;
  background: #f8fafc;
}

.modern-table-clean {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 10px;
}

.modern-table-clean th {
  text-align: left;
  padding: 0 16px 8px;
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
  border: none;
}

.modern-table-clean tbody tr {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  border-radius: 10px;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.modern-table-clean tbody tr:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.05);
}

.modern-table-clean td {
  padding: 16px;
  border: none;
  vertical-align: middle;
}

.modern-table-clean td:first-child {
  border-radius: 10px 0 0 10px;
}
.modern-table-clean td:last-child {
  border-radius: 0 10px 10px 0;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  color: #334155;
}

.avatar-placeholder {
  width: 32px;
  height: 32px;
  background: #e0f2fe;
  color: #0284c7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  text-transform: uppercase;
}

.id-cell {
  color: #64748b;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 14px;
}

.action-btn-danger {
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fee2e2;
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.action-btn-danger:hover {
  background: #fee2e2;
  border-color: #fecaca;
}

.btn-icon-tiny {
  width: 14px;
  height: 14px;
}

.empty-state {
  padding: 50px 0;
  text-align: center;
  color: #94a3b8;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}
.empty-icon {
  width: 40px;
  height: 40px;
  opacity: 0.5;
}

.modal-footer-modern {
  padding: 16px 30px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  border-top: 1px solid #f1f5f9;
}

.close-btn-simple {
  padding: 10px 24px;
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #475569;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.2s;
}

.close-btn-simple:hover {
  background: #f1f5f9;
  color: #0f172a;
}
</style>
