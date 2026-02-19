import request from "@/utils/request";

export const competitionApi = {
  // --- 竞赛管理 ---
  // 获取所有竞赛
  getList: () => request.get("/competitions"),
  // 发布新竞赛
  create: (data) => request.post("/competitions", data),
  // 删除竞赛
  delete: (id) => request.delete(`/competitions/${id}`),

  // --- 报名与审核 ---
  // 获取报名名单
  getRegistrations: (id) => request.get(`/registrations?competitionId=${id}`),
  // 提交报名
  register: (data) => request.post("/registrations", data),
  // 审核学生报名状态 (注意拼写修正: registrations)
  auditStudent: (id, status) =>
    request.put(`/registrations/${id}/audit?status=${status}`),
  // 取消报名
  deleteRegistration: (id) => request.delete(`/registrations/${id}`),

  // --- ✨ 新增：学生池维护 (对应后端 UserController) ---
  // 导入学生 Excel
  importStudents: (file) => {
    const formData = new FormData();
    formData.append("file", file);
    return request.post("/users/import", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
  // 下载学生导入模板
  downloadStudentTemplate: () => {
    return request.get("/users/template", {
      responseType: "blob",
    });
  },

  // --- ✨ 新增：账号信息维护 ---
  // 修改个人资料
  updateProfile: (data) => request.put("/users/profile", data),
  // 修改个人密码
  updatePassword: (data) => request.put("/users/password", data),
};
