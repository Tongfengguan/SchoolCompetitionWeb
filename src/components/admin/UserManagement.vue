<script setup>
import { ref, onMounted } from "vue";
import { useUserStore } from "@/stores/user";
import { competitionApi } from "@/api/competition";
import { Menu as IconMenu, UserFilled } from "@element-plus/icons-vue";

const userStore = useUserStore();
const userList = ref([]);
const loading = ref(false);
const fileInput = ref(null);
const activeSubTab = ref("list"); // 'list' or 'import'

const fetchUsers = async () => {
  loading.value = true;
  try {
    userList.value = await competitionApi.getUserList();
  } catch (error) {
    console.error("加载用户列表失败:", error);
  } finally {
    loading.value = false;
  }
};

const removeUser = async (user) => {
  if (user.id === userStore.userInfo.id) {
    return alert("❌ 无法删除当前登录的管理员账号");
  }
  if (!confirm(`确定要永久删除账号【${user.username}】吗？`)) return;

  try {
    loading.value = true;
    await competitionApi.deleteUser(user.id);
    userList.value = userList.value.filter((u) => u.id !== user.id);
    alert("✅ 账号已成功移除");
  } finally {
    loading.value = false;
  }
};

const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  try {
    loading.value = true;
    await competitionApi.importStudents(file);
    alert("✅ 学生账号导入成功！系统已自动以手机号生成初始密码。");
    event.target.value = "";
    if (activeSubTab.value === "list") fetchUsers();
  } finally {
    loading.value = false;
  }
};

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

onMounted(() => fetchUsers());
</script>

<template>
  <div class="view-section">
    <div class="tabs-header">
      <div 
        class="tab-item" 
        :class="{ active: activeSubTab === 'list' }"
        @click="activeSubTab = 'list'"
      >系统账号管理</div>
      <div 
        class="tab-item" 
        :class="{ active: activeSubTab === 'import' }"
        @click="activeSubTab = 'import'"
      >学生池维护</div>
    </div>

    <div v-if="activeSubTab === 'list'">
      <div class="action-bar">
        <h2>系统账号总览</h2>
      </div>
      <div class="table-card">
        <table class="modern-table">
          <thead>
            <tr>
              <th>真实姓名</th>
              <th>登录账号</th>
              <th>联系电话</th>
              <th>身份角色</th>
              <th style="text-align: right">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in userList" :key="user.id">
              <td>{{ user.name || "未填写" }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.phone || "未填写" }}</td>
              <td>
                <span :class="['status-tag', user.role === 'admin' ? 'active' : 'end']">
                  {{ user.role === "admin" ? "管理员" : "学生" }}
                </span>
              </td>
              <td style="text-align: right">
                <button
                  @click="removeUser(user)"
                  class="text-btn danger-text"
                  :disabled="user.id === userStore.userInfo.id"
                >删除账号</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="activeSubTab === 'import'" class="narrow-view-content">
      <div class="settings-card glass-effect">
        <div class="settings-header">
          <h3>学生池维护</h3>
          <p>通过 Excel 快速生成学生登录账号</p>
        </div>

        <div class="import-area">
          <div class="template-download-bar">
            <button @click="handleDownloadTemplate" class="download-link-btn">
              下载 Excel 标准模板
            </button>
          </div>

          <div class="upload-zone" @click="fileInput.click()">
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
              <li>请务必使用标准模板进行填写。</li>
              <li>系统将自动以【手机号】作为登录账号和初始密码。</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tabs-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}
.tab-item {
  padding: 10px 20px;
  cursor: pointer;
  color: #64748b;
  font-weight: 500;
}
.tab-item.active {
  color: #3b82f6;
  border-bottom: 2px solid #3b82f6;
}
.action-bar {
  margin-bottom: 20px;
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
.modern-table th, .modern-table td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}
.status-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}
.status-tag.active { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.status-tag.end { background: #f5f5f5; color: #8c8c8c; border: 1px solid #d9d9d9; }
.text-btn { background: none; border: none; color: #1890ff; cursor: pointer; }
.danger-text { color: #ff4d4f; }

.narrow-view-content {
  display: flex;
  justify-content: center;
}
.settings-card {
  width: 100%;
  max-width: 500px;
  padding: 30px;
}
.upload-zone {
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  background: #f8fafc;
  cursor: pointer;
}
.import-notice {
  margin-top: 20px;
  padding: 15px;
  background: #fffbeb;
  border-radius: 8px;
  font-size: 13px;
  color: #d97706;
}
</style>
