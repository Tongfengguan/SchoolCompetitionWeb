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
  EditPen,
  Lock,
} from "@element-plus/icons-vue";
import { competitionApi } from "@/api/competition";

const userStore = useUserStore();
const router = useRouter();

// ✨ 1. 新增：文件上传引用
const fileInput = ref(null);

// ✨ 视图切换控制
const currentTab = ref("competition");

// 账号维护表单状态
const profileForm = reactive({
  id: userStore.userInfo?.id,
  name: userStore.userInfo?.username || "",
  phone: "",
});

const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

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

// ✨ 2. 新增：处理 Excel 上传导入
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  try {
    loading.value = true;
    await competitionApi.importStudents(file);
    alert("✅ 学生账号导入成功！系统已自动以手机号生成初始账号密码。");
    // 导入成功后清空输入框，方便下次操作
    event.target.value = "";
  } catch (error) {
    console.error("导入失败:", error);
    alert("❌ 导入失败，请检查文件格式或网络连接");
  } finally {
    loading.value = false;
  }
};

// ✨ 3. 新增：处理模板下载
const handleDownloadTemplate = async () => {
  try {
    const response = await competitionApi.downloadStudentTemplate();
    const url = window.URL.createObjectURL(new Blob([response]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "学生账号导入模板.xlsx");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    alert("下载模板失败");
  }
};

const updateProfile = async () => {
  if (!profileForm.name) return alert("姓名不能为空");
  try {
    loading.value = true;
    await competitionApi.updateProfile(profileForm);
    alert("✅ 个人信息更新成功");
  } catch (error) {
    alert("更新失败");
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
    await competitionApi.updatePassword(pwdForm);
    alert("🔒 密码修改成功，请重新登录");
    handleLogout();
  } catch (error) {
    alert("原密码错误或修改失败");
  } finally {
    loading.value = false;
  }
};

const handleExport = () => {
  if (registrationList.value.length === 0) {
    alert("当前名单为空，无法导出");
    return;
  }
  const headerMap = {
    studentName: "学生姓名",
    studentId: "学号",
    className: "班级",
    phone: "联系电话",
  };
  exportToExcel(
    registrationList.value,
    `${currentTitle.value}_报名名单`,
    headerMap,
  );
};

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

const handleAudit = async (stuId, targetStatus) => {
  const actionName = targetStatus === 1 ? "通过" : "拒绝";
  if (!confirm(`确认要 ${actionName} 该学生的报名吗？`)) return;
  try {
    await competitionApi.auditStudent(stuId, targetStatus);
    const student = registrationList.value.find((s) => s.id === stuId);
    if (student) {
      student.status = targetStatus;
    }
  } catch (error) {
    console.error("审核失败:", error);
    alert("审核操作失败，请重试");
  }
};

const showDeleteModal = ref(false);
const compToDelete = ref(null);
const deleteConfirmText = ref("");
const openDeleteModal = (item) => {
  compToDelete.value = item;
  deleteConfirmText.value = "";
  showDeleteModal.value = true;
};

const executeDelete = async () => {
  if (deleteConfirmText.value !== compToDelete.value.title) {
    return alert("输入的竞赛名称不匹配");
  }
  try {
    loading.value = true;
    await competitionApi.delete(compToDelete.value.id);
    showDeleteModal.value = false;
    alert("🗑️ 竞赛已成功删除");
    fetchCompetitions();
  } finally {
    loading.value = false;
  }
};

onMounted(() => fetchCompetitions());
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">
        <el-icon :size="24"><Trophy /></el-icon>
        <span>竞赛后台系统</span>
      </div>

      <nav class="menu">
        <div class="menu-group-title">竞赛管理业务</div>
        <div
          class="menu-item"
          :class="{ active: currentTab === 'competition' }"
          @click="currentTab = 'competition'"
        >
          <el-icon><List /></el-icon> 竞赛列表管理
        </div>
        <div
          class="menu-item"
          :class="{ active: currentTab === 'audit' }"
          @click="currentTab = 'audit'"
        >
          <el-icon><UserFilled /></el-icon> 学生池维护
        </div>

        <div class="menu-group-title">账号信息维护</div>
        <div
          class="menu-item"
          :class="{ active: currentTab === 'profile' }"
          @click="currentTab = 'profile'"
        >
          <el-icon><EditPen /></el-icon> 修改个人信息
        </div>
        <div
          class="menu-item"
          :class="{ active: currentTab === 'password' }"
          @click="currentTab = 'password'"
        >
          <el-icon><Lock /></el-icon> 修改个人密码
        </div>
      </nav>

      <div class="logout-box" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon> 退出系统
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="breadcrumb">
          后台管理 /
          {{ currentTab === "competition" ? "竞赛列表" : "管理中心" }}
        </div>
        <div class="user-info">
          欢迎，{{ userStore.userInfo?.username }} (管理员)
        </div>
      </header>

      <div class="content-body">
        <div v-if="currentTab === 'competition'" class="view-section">
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
                    <div class="table-actions">
                      <button @click="viewRegistrations(item)" class="text-btn">
                        查看名单
                      </button>
                      <button
                        @click="openDeleteModal(item)"
                        class="text-btn danger-text"
                      >
                        删除
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="currentTab === 'audit'" class="view-section narrow-view">
          <div class="settings-card glass-effect">
            <div class="settings-header">
              <i-lucide-users-2 class="header-icon-large" />
              <h3>学生池维护</h3>
              <p>通过 Excel 快速生成学生登录账号</p>
            </div>

            <div class="import-area">
              <div class="template-download-bar">
                <button
                  @click="handleDownloadTemplate"
                  class="download-link-btn"
                >
                  <i-lucide-download class="btn-icon-tiny" />
                  下载 Excel 标准模板
                </button>
              </div>

              <div class="upload-zone" @click="$refs.fileInput.click()">
                <i-lucide-file-up class="upload-main-icon" />
                <p>点击此处上传学生信息表</p>
                <span>支持 .xlsx 或 .xls 格式</span>
                <input
                  type="file"
                  ref="fileInput"
                  hidden
                  accept=".xlsx, .xls"
                  @change="handleFileUpload"
                />
              </div>

              <div class="import-notice">
                <strong>💡 注意事项：</strong>
                <ul>
                  <li>请务必使用上方提供的标准模板进行填写。</li>
                  <li>系统将自动以【手机号】作为登录账号和初始密码。</li>
                  <li>如果手机号已存在，系统将自动跳过该行，不会重复生成。</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentTab === 'profile'" class="view-section narrow-view">
          <div class="settings-card glass-effect">
            <div class="settings-header">
              <i-lucide-user-cog class="header-icon-large" />
              <h3>修改个人信息</h3>
              <p>维护您的账户基本资料</p>
            </div>

            <div class="form-list">
              <div class="form-item">
                <label>登录账号 (不可修改)</label>
                <input
                  :value="userStore.userInfo?.username"
                  disabled
                  class="readonly-input"
                />
              </div>
              <div class="form-item">
                <label>真实姓名 <span class="required-star">*</span></label>
                <input
                  v-model="profileForm.name"
                  placeholder="请输入您的姓名"
                />
              </div>
              <div class="form-item">
                <label>联系电话</label>
                <input v-model="profileForm.phone" placeholder="请输入手机号" />
              </div>
              <button
                @click="updateProfile"
                class="btn-submit"
                :disabled="loading"
              >
                {{ loading ? "正在保存..." : "保存更新" }}
              </button>
            </div>
          </div>
        </div>

        <div v-if="currentTab === 'password'" class="view-section narrow-view">
          <div class="settings-card glass-effect">
            <div class="settings-header">
              <i-lucide-shield-check class="header-icon-large danger-color" />
              <h3>修改个人密码</h3>
              <p>为了您的账号安全，请定期更换密码</p>
            </div>

            <div class="form-list">
              <div class="form-item">
                <label>原始密码</label>
                <input
                  v-model="pwdForm.oldPassword"
                  type="password"
                  placeholder="请输入当前旧密码"
                />
              </div>
              <div class="form-item password-divider"></div>
              <div class="form-item">
                <label>设置新密码</label>
                <input
                  v-model="pwdForm.newPassword"
                  type="password"
                  placeholder="请输入 6-18 位新密码"
                />
              </div>
              <div class="form-item">
                <label>确认新密码</label>
                <input
                  v-model="pwdForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                />
              </div>
              <button
                @click="updatePassword"
                class="btn-submit danger-btn"
                :disabled="loading"
              >
                确认重置密码
              </button>
            </div>
          </div>
        </div>
      </div>
      <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
        <div
          class="modal-content glass-effect"
          style="width: 500px; padding: 30px"
        >
          <div class="modal-header-modern">
            <div class="header-titles">
              <h3>发布新竞赛项目</h3>
              <span class="sub-text"
                >请填写完整的比赛信息，发布后学生即可看到</span
              >
            </div>
          </div>

          <div class="form-list" style="margin-top: 20px">
            <div class="form-item">
              <label>竞赛名称 <span class="required-star">*</span></label>
              <input
                v-model="form.title"
                placeholder="例如：2026年春季数学奥林匹克"
              />
            </div>
            <div class="form-item">
              <label>竞赛描述</label>
              <textarea
                v-model="form.description"
                rows="3"
                placeholder="请输入比赛详情说明..."
              ></textarea>
            </div>
            <div class="form-row">
              <div class="form-item half">
                <label>开始时间</label>
                <input v-model="form.startTime" type="datetime-local" />
              </div>
              <div class="form-item half">
                <label>截止时间</label>
                <input v-model="form.endTime" type="datetime-local" />
              </div>
            </div>
            <div class="modal-actions">
              <button @click="showForm = false" class="btn-cancel">取消</button>
              <button @click="submitCompetition" class="btn-submit">
                确认发布项目
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
        <div class="modal-content glass-effect" style="width: 700px">
          <div class="modal-header-modern">
            <div class="header-left">
              <div class="icon-box"><i-lucide-users class="header-icon" /></div>
              <div class="header-titles">
                <h3>{{ currentTitle }} - 报名名单</h3>
                <span class="sub-text"
                  >当前共有 {{ registrationList.length }} 名学生报名</span
                >
              </div>
            </div>
            <button @click="handleExport" class="export-btn-modern">
              <i-lucide-file-spreadsheet class="btn-icon-small" /> 导出 Excel
              名单
            </button>
          </div>

          <div class="table-wrapper-modern">
            <table class="modern-table-clean">
              <thead>
                <tr>
                  <th>学生信息</th>
                  <th>学号/账号</th>
                  <th>班级</th>
                  <th style="text-align: right">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="reg in registrationList" :key="reg.id">
                  <td>
                    <div class="name-cell">
                      <div class="avatar-placeholder">
                        {{ reg.studentName?.charAt(0) }}
                      </div>
                      {{ reg.studentName }}
                    </div>
                  </td>
                  <td class="id-cell">{{ reg.studentId }}</td>
                  <td>{{ reg.className }}</td>
                  <td style="text-align: right">
                    <button
                      @click="removeStudent(reg.id)"
                      class="action-btn-danger"
                    >
                      <i-lucide-user-x class="btn-icon-tiny" /> 移除
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="registrationList.length === 0" class="empty-state">
              <i-lucide-inbox class="empty-icon" />
              <p>暂无学生报名此竞赛</p>
            </div>
          </div>

          <div class="modal-footer-modern">
            <button @click="showListModal = false" class="close-btn-simple">
              关闭窗口
            </button>
          </div>
        </div>
      </div>

      <div
        v-if="showDeleteModal"
        class="modal-overlay"
        @click.self="showDeleteModal = false"
      >
        <div class="modal-content glass-effect delete-modal">
          <div class="delete-warning-header">
            <div class="warning-icon-box">
              <i-lucide-alert-triangle class="warning-icon" />
            </div>
            <h3>确认删除该竞赛？</h3>
          </div>
          <div class="warning-text">
            此操作将永久删除
            <strong>{{ compToDelete?.title }}</strong>
            及其所有的报名记录，且无法恢复。
          </div>
          <div class="confirm-input-group">
            <label>请输入竞赛全名以确认删除：</label>
            <input
              v-model="deleteConfirmText"
              class="danger-input"
              :placeholder="compToDelete?.title"
            />
          </div>
          <div class="modal-actions delete-actions">
            <button @click="showDeleteModal = false" class="btn-cancel">
              取消
            </button>
            <button
              @click="executeDelete"
              class="btn-delete-confirm"
              :disabled="deleteConfirmText !== compToDelete?.title"
            >
              我已确认，执行删除
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 1. 基础框架布局 */
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
  padding: 8px 0;
}
.menu-group-title {
  padding: 24px 24px 8px;
  font-size: 11px;
  color: #596780;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  font-weight: 700;
}
.menu-item {
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: 0.3s;
  color: #a6adb4;
}
.menu-item.active {
  background: #1890ff;
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

/* 2. 竞赛列表管理 */
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
  border-bottom: 1px solid #f0f0f0;
}
.modern-table td {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
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
  transition: 0.2s;
  font-weight: 500;
}
.add-btn:hover {
  background: #40a9ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}
.text-btn {
  background: none;
  border: none;
  color: #1890ff;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 8px;
}
.text-btn:hover {
  text-decoration: underline;
}
.danger-text {
  color: #ff4d4f;
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

/* 3. 学生池维护 (Excel 导入) */
.upload-zone {
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  background: #f8fafc;
  cursor: pointer;
  transition: 0.3s;
}
.upload-zone:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}
.template-download-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 15px;
}
.download-link-btn {
  background: none;
  border: none;
  color: #3b82f6;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}
.import-notice {
  margin-top: 20px;
  padding: 15px;
  background: #fffbeb;
  border-radius: 8px;
  font-size: 13px;
  color: #d97706;
}

/* 4. 账号维护卡片 (个人信息/密码) */
.narrow-view {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}
.settings-card {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
}
.settings-header {
  text-align: center;
  margin-bottom: 30px;
}
.header-icon-large {
  width: 48px;
  height: 48px;
  color: #3b82f6;
  margin-bottom: 15px;
}
.header-icon-large.danger-color {
  color: #ef4444;
}
.form-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
}
.form-item input,
.form-item textarea {
  padding: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  outline: none;
  background: #f8fafc;
}
.btn-submit {
  float: right;
  padding: 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-cancel {
  float: left;
  padding: 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-submit:hover,
.btn-cancel:hover {
  background: #2563eb;
}
.readonly-input {
  background: #f1f5f9 !important;
  color: #94a3b8;
  cursor: not-allowed;
}

/* 5. 弹窗基础与玻璃拟态 */
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
  background: white;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  border-radius: 16px;
  overflow: hidden;
}
.modal-header-modern {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
}

/* 6. 查看名单弹窗专项 (导出/关闭按钮) */
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
  transition: 0.2s;
}
.export-btn-modern:hover {
  background: #10b981;
  color: white;
  border-color: #10b981;
}
.table-wrapper-modern {
  padding: 20px 30px;
  max-height: 400px;
  overflow-y: auto;
  background: #fcfdfe;
}
.modern-table-clean {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 8px;
}
.modern-table-clean td {
  background: #fff;
  padding: 12px 15px;
  border-top: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
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
  margin-right: 10px;
  font-weight: bold;
}
.action-btn-danger {
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fee2e2;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
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
  transition: 0.2s;
}
.close-btn-simple:hover {
  background: #f1f5f9;
  color: #0f172a;
}

/* 7. 删除确认弹窗专项 */
.delete-modal {
  padding: 30px;
  max-width: 450px;
}
.warning-text {
  background: #fff8f8;
  padding: 15px;
  border-left: 4px solid #ef4444;
  margin: 15px 0;
  color: #475569;
  font-size: 14px;
}
.btn-delete-confirm {
  background: #ef4444;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
.btn-delete-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 8. 其他组件 */
.btn-icon-tiny {
  width: 14px;
  height: 14px;
}
.btn-icon-small {
  width: 16px;
  height: 16px;
}
.required-star {
  color: #ef4444;
}
.password-divider {
  height: 1px;
  background: #f1f5f9;
  margin: 10px 0;
}
</style>
