import request from "@/utils/request";

export const competitionApi = {
  // 获取所有竞赛
  getList: () => request.get("/competitions"),
  // 发布新竞赛
  create: (data) => request.post("/competitions", data),
  // 获取报名名单
  getRegistrations: (id) => request.get(`/registrations?competitionId=${id}`),
  // 取消报名
  deleteRegistration: (id) => request.delete(`/registrations/${id}`),
  // 提交报名
  register: (data) => request.post("/registrations", data),
};
