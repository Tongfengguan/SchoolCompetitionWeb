<script setup>
import { ref, reactive, onMounted } from "vue";
import { Plus, Document } from "@element-plus/icons-vue";
import { competitionApi } from "@/api/competition";
import { exportToExcel } from "@/utils/excel";

const competitions = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const showForm = ref(false);
const showListModal = ref(false);
const registrationList = ref([]);
const currentTitle = ref("");

const form = reactive({
  title: "",
  description: "",
  startTime: "",
  endTime: "",
  fileUrl: "",
});

const fetchCompetitions = async () => {
  loading.value = true;
  try {
    const res = await competitionApi.getList({
      page: currentPage.value - 1,
      size: pageSize.value
    });
    competitions.value = res.content || [];
    total.value = res.totalElements || 0;
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchCompetitions();
};

const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  if (file.type !== "application/pdf") {
    alert("目前仅支持上传 PDF 格式的规则附件");
    return;
  }
  
  try {
    loading.value = true;
    const url = await competitionApi.uploadFile(file);
    form.fileUrl = url;
    alert("✅ 附件上传成功");
  } catch (error) {
    alert("附件上传失败");
  } finally {
    loading.value = false;
  }
};

const triggerFileUpload = () => {
  document.getElementById('file-input-id').click();
};

const submitCompetition = async () => {
  if (!form.title) return alert("标题必填");
  if (!form.startTime || !form.endTime) return alert("请选择完整的比赛时间");
  await competitionApi.create(form);
  showForm.value = false;
  // 重置表单
  form.title = ""; form.description = ""; form.startTime = ""; form.endTime = ""; form.fileUrl = "";
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
  if (registrationList.value.length === 0) return alert("当前名单为空，无法导出");
  const headerMap = { studentName: "学生姓名", studentId: "学号", className: "班级", phone: "联系电话" };
  exportToExcel(registrationList.value, `${currentTitle.value}_报名名单`, headerMap);
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
  if (deleteConfirmText.value !== compToDelete.value.title) return alert("输入的竞赛名称不匹配");
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
            <td class="title-cell">
              {{ item.title }}
              <el-icon v-if="item.fileUrl" style="margin-left: 8px; color: #3b82f6" title="含有规则附件"><Document /></el-icon>
            </td>
            <td>{{ item.startTime }}</td>
            <td>
              <span :class="['status-tag', item.status === 1 ? 'active' : 'end']">
                {{ item.status === 1 ? "进行中" : "已结束" }}
              </span>
            </td>
            <td style="text-align: right">
              <div class="table-actions">
                <button @click="viewRegistrations(item)" class="text-btn">查看名单</button>
                <button @click="openDeleteModal(item)" class="text-btn danger-text">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 发布弹窗 -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-content glass-effect" style="width: 500px; padding: 30px">
        <div class="modal-header-modern">
          <h3>发布新竞赛项目</h3>
        </div>

        <div class="form-list" style="margin-top: 20px">
          <div class="form-item">
            <label>竞赛名称 <span class="required-star">*</span></label>
            <input v-model="form.title" placeholder="请输入竞赛名称" />
          </div>
          <div class="form-item">
            <label>竞赛描述</label>
            <textarea v-model="form.description" rows="3" placeholder="请输入比赛详情..."></textarea>
          </div>
          
          <div class="form-item">
            <label>竞赛规则附件 (仅限 PDF)</label>
            <div class="upload-custom-zone">
              <input type="file" @change="handleFileChange" accept=".pdf" id="file-input-id" hidden />
              <button class="custom-file-btn" @click="triggerFileUpload" type="button">
                {{ form.fileUrl ? "✅ 附件已上传" : "选择 PDF 文件" }}
              </button>
              <p v-if="form.fileUrl" class="file-path-hint">路径: {{ form.fileUrl }}</p>
            </div>
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
            <button @click="submitCompetition" class="btn-submit">确认发布</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 名单弹窗 -->
    <div v-if="showListModal" class="modal-overlay" @click.self="showListModal = false">
      <div class="modal-content glass-effect" style="width: 700px">
        <div class="modal-header-modern">
          <h3>{{ currentTitle }} - 报名名单 ({{ registrationList.length }}人)</h3>
          <button @click="handleExport" class="export-btn-modern">导出 Excel</button>
        </div>
        <div class="table-wrapper-modern">
          <table class="modern-table-clean">
            <thead>
              <tr><th>学生信息</th><th>学号/账号</th><th>班级</th><th style="text-align: right">操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="reg in registrationList" :key="reg.id">
                <td>{{ reg.studentName }}</td><td>{{ reg.studentId }}</td><td>{{ reg.className }}</td>
                <td style="text-align: right">
                  <button @click="removeStudent(reg.id)" class="action-btn-danger">移除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="modal-footer-modern">
          <button @click="showListModal = false" class="close-btn-simple">关闭</button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal-content glass-effect delete-modal">
        <h3>确认删除该竞赛？</h3>
        <p>此操作将永久删除 <strong>{{ compToDelete?.title }}</strong> 及其所有报名记录。</p>
        <input v-model="deleteConfirmText" class="danger-input" :placeholder="compToDelete?.title" />
        <div class="modal-actions delete-actions">
          <button @click="showDeleteModal = false" class="btn-cancel">取消</button>
          <button @click="executeDelete" class="btn-delete-confirm" :disabled="deleteConfirmText !== compToDelete?.title">执行删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.table-card { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.modern-table { width: 100%; border-collapse: collapse; }
.modern-table th, .modern-table td { padding: 16px; border-bottom: 1px solid #f0f0f0; text-align: left; }
.add-btn { background: #3b82f6; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; gap: 8px; }
.text-btn { background: none; border: none; color: #3b82f6; cursor: pointer; margin-right: 12px; }
.danger-text { color: #ef4444; }
.status-tag { padding: 4px 12px; border-radius: 4px; font-size: 12px; }
.status-tag.active { background: #dcfce7; color: #166534; }
.status-tag.end { background: #f1f5f9; color: #64748b; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.glass-effect { background: white; border-radius: 12px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); }
.form-list { display: flex; flex-direction: column; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-item label { font-weight: 600; font-size: 14px; }
.form-item input, .form-item textarea { padding: 10px; border: 1px solid #e2e8f0; border-radius: 6px; }
.upload-custom-zone { border: 2px dashed #e2e8f0; padding: 15px; border-radius: 8px; text-align: center; }
.custom-file-btn { background: #f8fafc; border: 1px solid #e2e8f0; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
.file-path-hint { font-size: 11px; color: #94a3b8; margin-top: 5px; word-break: break-all; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.btn-submit { background: #3b82f6; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
.btn-cancel { background: #f1f5f9; color: #64748b; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
.required-star { color: #ef4444; }
</style>
