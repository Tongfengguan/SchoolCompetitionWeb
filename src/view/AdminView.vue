<script setup>
import { ref, reactive, onMounted } from "vue";
import axios from "axios";

const competitions = ref([]);
const showForm = ref(false);
const showListModal = ref(false);
const registrationList = ref([]);
const currentTitle = ref("");

// 发布表单
const form = reactive({
  title: "",
  description: "",
  startTime: "",
  endTime: "",
});

const fetchCompetitions = async () => {
  try {
    const res = await axios.get("http://localhost:8080/api/competitions");
    competitions.value = res.data;
  } catch (e) {
    console.error(e);
  }
};

const submitCompetition = async () => {
  if (!form.title) return alert("标题必填");
  await axios.post("http://localhost:8080/api/competitions", form);
  alert("发布成功");
  showForm.value = false;
  fetchCompetitions();
};

const viewRegistrations = async (item) => {
  currentTitle.value = item.title;
  const res = await axios.get(
    `http://localhost:8080/api/registrations?competitionId=${item.id}`,
  );
  registrationList.value = res.data;
  showListModal.value = true;
};

// 取消某个学生的报名
const removeStudent = async (registrationId) => {
  if (!confirm("确定要取消该学生的报名资格吗？")) return;

  try {
    await axios.delete(
      `http://localhost:8080/api/registrations/${registrationId}`,
    );
    // 成功后，从当前的 registrationList 数组里把这个人移除，不用重新请求后端
    registrationList.value = registrationList.value.filter(
      (item) => item.id !== registrationId,
    );
    alert("已取消资格");
  } catch (error) {
    alert("操作失败");
  }
};

onMounted(() => fetchCompetitions());
</script>

<template>
  <div class="admin-container">
    <div class="header">
      <h2>👮‍♂️ 教务后台管理</h2>
      <button @click="showForm = !showForm" class="add-btn">
        {{ showForm ? "收起" : "发布新比赛" }}
      </button>
    </div>

    <div v-if="showForm" class="form-box">
      <input v-model="form.title" placeholder="比赛名称" />
      <textarea v-model="form.description" placeholder="描述"></textarea>
      <div class="row">
        Start: <input type="datetime-local" v-model="form.startTime" /> End:
        <input type="datetime-local" v-model="form.endTime" />
      </div>
      <button @click="submitCompetition" class="save-btn">保存并发布</button>
    </div>

    <table class="admin-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>名称</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in competitions" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.title }}</td>
          <td>{{ item.status === 1 ? "进行中" : "结束" }}</td>
          <td>
            <button @click="viewRegistrations(item)" class="view-btn">
              查看名单
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div
      v-if="showListModal"
      class="modal-overlay"
      @click.self="showListModal = false"
    >
      <div class="modal-content wide">
        <h3>{{ currentTitle }} - 报名名单</h3>
        <table class="admin-table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>学号</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="stu in registrationList" :key="stu.id">
              <td>{{ stu.studentName }}</td>
              <td>{{ stu.studentId }}</td>
              <td>
                <button @click="removeStudent(stu.id)" class="del-btn">
                  取消资格
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <button @click="showListModal = false">关闭</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.add-btn {
  background: #34495e;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.form-box {
  background: #f9f9f9;
  padding: 15px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  border: 1px solid #ddd;
}
.form-box input,
.form-box textarea {
  padding: 8px;
  border: 1px solid #ccc;
}
.save-btn {
  background: #27ae60;
  color: white;
  border: none;
  padding: 10px;
  cursor: pointer;
}
.admin-table {
  width: 100%;
  border-collapse: collapse;
}
.admin-table th,
.admin-table td {
  padding: 10px;
  border-bottom: 1px solid #eee;
  text-align: left;
}
.view-btn {
  background: #f39c12;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 3px;
  cursor: pointer;
}

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
.modal-content.wide {
  background: white;
  padding: 20px;
  width: 500px;
  max-height: 400px;
  overflow-y: auto;
  border-radius: 8px;
}
</style>
