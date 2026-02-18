import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";

// 1. 引入自动化插件
import Icons from "unplugin-icons/vite";
import IconsResolver from "unplugin-icons/resolver";
import Components from "unplugin-vue-components/vite";

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    // 2. 配置自动按需引入组件
    Components({
      resolvers: [
        // 自动注册图标组件，使用前缀 'i'
        // 例如：<i-lucide-award /> 或 <i-ep-trophy />
        IconsResolver({
          prefix: "i",
          enabledCollections: ["lucide", "ep"], // 启用 Lucide 和 Element Plus 图标集
        }),
      ],
    }),
    // 3. 配置图标自动安装
    Icons({
      autoInstall: true,
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
});
