<script setup>
import { ref } from "vue";
import { useUserStore } from "@/stores/user";
import { useRouter } from "vue-router";
// 引入图标
import { Trophy, List, Menu as IconMenu, EditPen, SwitchButton, DataBoard } from "@element-plus/icons-vue";

// 引入拆分后的子组件
import Dashboard from "@/components/admin/Dashboard.vue";
import CompetitionList from "@/components/admin/CompetitionList.vue";
import UserManagement from "@/components/admin/UserManagement.vue";
import AccountSettings from "@/components/admin/AccountSettings.vue";

const userStore = useUserStore();
const router = useRouter();
const currentTab = ref("dashboard"); // 默认展示看板

const handleLogout = () => {
  userStore.clearUser();
  router.push("/login");
};

const getTabTitle = () => {
  switch(currentTab.value) {
    case 'dashboard': return '系统运行概览';
    case 'competition': return '竞赛列表管理';
    case 'users': return '系统账号管理';
    case 'settings': return '账号设置';
    default: return '管理中心';
  }
};
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">
        <el-icon :size="24"><Trophy /></el-icon>
        <span>竞赛后台系统</span>
      </div>

      <nav class="menu">
        <div class="menu-group-title">数据概览</div>
        <div 
          class="menu-item" 
          :class="{ active: currentTab === 'dashboard' }"
          @click="currentTab = 'dashboard'"
        >
          <el-icon><DataBoard /></el-icon> 管理控制台
        </div>

        <div class="menu-group-title">业务管理</div>
        <div 
          class="menu-item" 
          :class="{ active: currentTab === 'competition' }"
          @click="currentTab = 'competition'"
        >
          <el-icon><List /></el-icon> 竞赛项目管理
        </div>
        <div 
          class="menu-item" 
          :class="{ active: currentTab === 'users' }"
          @click="currentTab = 'users'"
        >
          <el-icon><IconMenu /></el-icon> 系统账号管理
        </div>

        <div class="menu-group-title">个人中心</div>
        <div 
          class="menu-item" 
          :class="{ active: currentTab === 'settings' }"
          @click="currentTab = 'settings'"
        >
          <el-icon><EditPen /></el-icon> 账号设置
        </div>
      </nav>

      <div class="logout-box" @click="handleLogout">
        <el-icon size="16"><SwitchButton /></el-icon> 退出登录
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="breadcrumb">
          后台管理 / {{ getTabTitle() }}
        </div>
        <div class="user-info">
          欢迎，{{ userStore.userInfo?.username }} (管理员)
        </div>
      </header>

      <div class="content-body">
        <!-- 根据当前 Tab 渲染对应的子组件 -->
        <Dashboard v-if="currentTab === 'dashboard'" />
        <CompetitionList v-if="currentTab === 'competition'" />
        <UserManagement v-if="currentTab === 'users'" />
        <AccountSettings v-if="currentTab === 'settings'" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-layout { display: flex; height: 100vh; background: #f4f7f9; }
.sidebar { width: 240px; background: #001529; color: white; display: flex; flex-direction: column; }
.logo { height: 64px; display: flex; align-items: center; padding: 0 24px; gap: 12px; font-size: 18px; font-weight: bold; background: #002140; }
.menu { flex: 1; padding: 8px 0; }
.menu-group-title { padding: 24px 24px 8px; font-size: 11px; color: #596780; text-transform: uppercase; letter-spacing: 1.5px; font-weight: 700; }
.menu-item { padding: 12px 24px; display: flex; align-items: center; gap: 10px; cursor: pointer; transition: 0.3s; color: #a6adb4; }
.menu-item:hover { color: white; background: rgba(255,255,255,0.05); }
.menu-item.active { background: #3b82f6; color: white; }
.logout-box { padding: 20px 24px; border-top: 1px solid #ffffff1a; display: flex; align-items: center; gap: 10px; color: #a6adb4; cursor: pointer; }
.logout-box:hover { color: #ff4d4f; }
.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.top-header { height: 64px; background: white; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; box-shadow: 0 1px 4px rgba(0,21,41,0.08); flex-shrink: 0; }
.content-body { padding: 24px; overflow-y: auto; flex: 1; }
</style>
