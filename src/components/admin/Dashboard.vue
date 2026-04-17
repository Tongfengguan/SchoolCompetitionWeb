<script setup>
import { ref, onMounted, nextTick } from "vue";
import * as echarts from "echarts";
import { competitionApi } from "@/api/competition";
import { Trophy, User, Histogram } from "@element-plus/icons-vue";

const stats = ref({
  totalUsers: 0,
  totalCompetitions: 0,
  totalRegistrations: 0,
});

const pieChartRef = ref(null);
const barChartRef = ref(null);
let pieChart = null;
let barChart = null;

const initCharts = (data) => {
  // 1. 各竞赛报名人数比例 (饼图)
  const pieData = Object.entries(data.competitionDistribution).map(([name, value]) => ({
    name, value
  }));

  pieChart = echarts.init(pieChartRef.value);
  pieChart.setOption({
    title: { text: "竞赛报名分布", left: "center" },
    tooltip: { trigger: "item" },
    series: [
      {
        name: "报名人数",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: "#fff", borderWidth: 2 },
        label: { show: false, position: "center" },
        emphasis: { label: { show: true, fontSize: "20", fontWeight: "bold" } },
        data: pieData,
      },
    ],
  });

  // 2. 角色分布 (柱状图)
  const barDataX = Object.keys(data.userRoleDistribution).map(k => k === 'admin' ? '管理员' : '学生');
  const barDataY = Object.values(data.userRoleDistribution);

  barChart = echarts.init(barChartRef.value);
  barChart.setOption({
    title: { text: "用户角色概览", left: "center" },
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: barDataX },
    yAxis: { type: "value" },
    series: [
      {
        data: barDataY,
        type: "bar",
        showBackground: true,
        backgroundStyle: { color: "rgba(180, 180, 180, 0.2)" },
        itemStyle: { color: "#3b82f6" },
      },
    ],
  });
};

const fetchData = async () => {
  try {
    const res = await competitionApi.getStatistics();
    stats.value = res;
    await nextTick();
    initCharts(res);
  } catch (error) {
    console.error("加载面板数据失败:", error);
  }
};

onMounted(() => {
  fetchData();
  window.addEventListener("resize", () => {
    pieChart?.resize();
    barChart?.resize();
  });
});
</script>

<template>
  <div class="dashboard-view">
    <!-- 顶部统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card blue">
        <div class="stat-info">
          <span class="label">系统用户总数</span>
          <span class="value">{{ stats.totalUsers }}</span>
        </div>
        <el-icon class="stat-icon"><User /></el-icon>
      </div>
      <div class="stat-card purple">
        <div class="stat-info">
          <span class="label">发布竞赛项目</span>
          <span class="value">{{ stats.totalCompetitions }}</span>
        </div>
        <el-icon class="stat-icon"><Trophy /></el-icon>
      </div>
      <div class="stat-card orange">
        <div class="stat-info">
          <span class="label">累计报名人次</span>
          <span class="value">{{ stats.totalRegistrations }}</span>
        </div>
        <el-icon class="stat-icon"><Histogram /></el-icon>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-container glass-effect">
        <div ref="pieChartRef" style="height: 400px; width: 100%"></div>
      </div>
      <div class="chart-container glass-effect">
        <div ref="barChartRef" style="height: 400px; width: 100%"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.stat-card {
  padding: 24px;
  border-radius: 16px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.1);
}
.stat-card.blue { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); }
.stat-card.purple { background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%); }
.stat-card.orange { background: linear-gradient(135deg, #f59e0b 0%, #ea580c 100%); }

.stat-info { display: flex; flex-direction: column; }
.stat-info .label { font-size: 14px; opacity: 0.8; }
.stat-info .value { font-size: 32px; font-weight: bold; margin-top: 4px; }
.stat-icon { font-size: 48px; opacity: 0.3; }

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.chart-container {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}
.glass-effect {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.3);
}
</style>
