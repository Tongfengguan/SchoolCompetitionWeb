<script setup>
import { ref, reactive, onMounted } from "vue";
import { Plus } from "@element-plus/icons-vue";
import { competitionApi } from "@/api/competition";
import { exportToExcel } from "@/utils/excel";

const competitions = ref([]);
const loading = ref(false);
const showForm = ref(false);
const showListModal = ref(false);
const registrationList = ref([]);
const currentTitle = ref("");

const form = reactive({
  title: "",
  description: "",
  startTime: "",
  endTime: "",
});

const fetchCompetitions = async () => {
  loading.value = true;
  try {
    competitions.value = await competitionApi.getList();
  } finally {
    loading.value = false;
  }
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
  <div class="view-section">
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
                :class="['status-tag', item.status === 1 ? 'active' : 'end']"
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

    <!-- 弹窗部分 -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div
        class="modal-content glass-effect"
        style="width: 500px; padding: 30px"
      >
        <div class="modal-header-modern">
          <div class="header-titles">
            <h3>发布新竞赛项目</h3>
            <span class="sub-text">请填写完整的比赛信息，发布后学生即可看到</span>
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
            <div class="icon-box">
              <el-icon><i-lucide-users class="header-icon" /></el-icon>
            </div>
            <div class="header-titles">
              <h3>{{ currentTitle }} - 报名名单</h3>
              <span class="sub-text">当前共有 {{ registrationList.length }} 名学生报名</span>
            </div>
          </div>
          <button @click="handleExport" class="export-btn-modern">
            <el-icon><i-lucide-file-spreadsheet class="btn-icon-small" /></el-icon>
            导出 Excel 名单
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
                    移除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="registrationList.length === 0" class="empty-state">
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
          <button @click="showDeleteModal = false" class="btn-cancel">取消</button>
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
  </div>
</template>

<style scoped>
/* 这里可以引入一些局部的样式，或者依赖全局样式 */
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
}
.text-btn {
  background: none;
  border: none;
  color: #1890ff;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 8px;
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

/* 弹窗通用样式 */
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

/* 其他样式根据需要从 AdminView 迁移 */
</style>
