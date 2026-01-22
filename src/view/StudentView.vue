<script setup>
import { ref, reactive, onMounted } from "vue";
import axios from "axios";

const competitions = ref([]);
const showRegister = ref(false);

// 报名表单数据
const regForm = reactive({
  competitionId: null,
  competitionTitle: "",
  studentName: "",
  studentId: "",
  className: "",
  phone: "",
});

// 获取列表
const fetchCompetitions = async () => {
  try {
    const response = await axios.get("http://localhost:8080/api/competitions");
    competitions.value = response.data;
  } catch (error) {
    console.error(error);
  }
};

// 打开报名弹窗
const openRegisterModal = (competition) => {
  if (competition.status === 0) return alert("该比赛已结束！");

  // 1. 获取当前登录用户的信息
  const userStr = localStorage.getItem("user");
  if (!userStr) return alert("请先登录！");
  const user = JSON.parse(userStr);

  // 2. 自动填充表单
  regForm.competitionId = competition.id;
  regForm.competitionTitle = competition.title;

  // ✨ 这里是关键：直接用账号信息填入，不再让用户手写
  // 假设 user.name 是真实姓名，user.username 是学号
  regForm.studentName = user.name;
  regForm.studentId = user.username;

  // 班级和电话如果 User 表里没有，还是得让学生自己填
  regForm.className = "";
  regForm.phone = "";

  showRegister.value = true;
};

// 提交报名
const submitRegistration = async () => {
  if (!regForm.studentName || !regForm.studentId)
    return alert("请填写完整信息！");
  try {
    await axios.post("http://localhost:8080/api/registrations", regForm);
    alert("🎉 报名成功！");
    showRegister.value = false;
    Object.assign(regForm, {
      studentName: "",
      studentId: "",
      className: "",
      phone: "",
    });
  } catch (error) {
    if (error.response && error.response.status === 400)
      alert("❌ " + error.response.data);
    else alert("报名失败");
  }
};

onMounted(() => fetchCompetitions());
</script>

<template>
  <div class="page-container">
    <h2>👋 同学你好，欢迎报名</h2>

    <div class="card-grid">
      <div v-for="item in competitions" :key="item.id" class="comp-card">
        <h3>{{ item.title }}</h3>
        <p class="desc">{{ item.description }}</p>
        <div class="meta">
          <span
            >📅
            {{ item.startTime.replace("T", " ").substring(5, 16) }} 开始</span
          >
          <span v-if="item.status === 1" class="badge green">报名中</span>
          <span v-else class="badge red">已结束</span>
        </div>
        <button
          @click="openRegisterModal(item)"
          class="join-btn"
          :disabled="item.status === 0"
        >
          {{ item.status === 1 ? "我要报名" : "已截止" }}
        </button>
      </div>
    </div>

    <div v-if="showRegister" class="modal-overlay">
      <div class="modal-content">
        <h3>报名: {{ regForm.competitionTitle }}</h3>

        <div class="form-group">
          <label>姓名 (自动)</label>
          <input
            v-model="regForm.studentName"
            disabled
            style="background: #eee"
          />
        </div>

        <div class="form-group">
          <label>学号 (自动)</label>
          <input
            v-model="regForm.studentId"
            disabled
            style="background: #eee"
          />
        </div>

        <div class="form-group">
          <label>班级</label>
          <input v-model="regForm.className" placeholder="请输入班级" />
        </div>

        <div class="form-group">
          <label>电话</label>
          <input v-model="regForm.phone" placeholder="请输入电话" />
        </div>

        <div class="actions">
          <button @click="showRegister = false">取消</button>
          <button @click="submitRegistration" class="primary">确认提交</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 学生端卡片式设计，更现代一点 */
.page-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.comp-card {
  border: 1px solid #eee;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
}
.comp-card:hover {
  transform: translateY(-3px);
}
.desc {
  color: #666;
  font-size: 14px;
  margin: 10px 0;
  height: 40px;
  overflow: hidden;
}
.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 12px;
  color: #888;
}
.badge {
  padding: 2px 6px;
  border-radius: 4px;
  color: white;
}
.badge.green {
  background: #2ecc71;
}
.badge.red {
  background: #e74c3c;
}
.join-btn {
  width: 100%;
  padding: 10px;
  border: none;
  background: #3498db;
  color: white;
  border-radius: 5px;
  cursor: pointer;
}
.join-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 弹窗通用样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.modal-content input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.actions button {
  flex: 1;
  padding: 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.actions button.primary {
  background: #2ecc71;
  color: white;
}
</style>
