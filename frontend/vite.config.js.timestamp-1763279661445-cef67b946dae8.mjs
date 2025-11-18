// vite.config.js
import { fileURLToPath, URL } from "node:url";
import path from "node:path";
import { defineConfig } from "file:///D:/minho/frontend/node_modules/vite/dist/node/index.js";
import vue from "file:///D:/minho/frontend/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import vueDevTools from "file:///D:/minho/frontend/node_modules/vite-plugin-vue-devtools/dist/vite.mjs";
import { VitePWA } from "file:///D:/minho/frontend/node_modules/vite-plugin-pwa/dist/index.js";
var __vite_injected_original_dirname = "D:\\minho\\frontend";
var __vite_injected_original_import_meta_url = "file:///D:/minho/frontend/vite.config.js";
var springBootStatic = path.resolve(__vite_injected_original_dirname, "../backend/springboot/src/main/resources/static");
var vite_config_default = defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    VitePWA({
      strategies: "injectManifest",
      injectManifest: {
        maximumFileSizeToCacheInBytes: 6 * 1024 * 1024
        // 6MB로 올림
      },
      // 🚨 추가: Service Worker 소스 파일 지정
      srcDir: "public",
      filename: "firebase-messaging-sw.js",
      includeAssets: ["favicon.ico", "apple-touch-icon.png", "masked-icon.svg"],
      manifest: {
        name: "ISeaU PWA App",
        short_name: "ISeaU PWA",
        theme_color: "#ffffff",
        icons: [
          {
            src: "iseau-192.png",
            sizes: "192x192",
            type: "image/png",
            // 📌 icons의 purpose 속성 설정: maskable 아이콘을 반드시 포함해야 합니다.
            purpose: "maskable"
          },
          {
            src: "iseau-512.png",
            sizes: "512x512",
            type: "image/png"
          }
        ]
      },
      // 🚨 [핵심] SW 등록 방식 설정 (개발 시 알림을 최소화)
      registerType: "autoUpdate",
      workbox: {
        // 개발 환경에서 로그를 줄이는 데 도움을 줍니다.
        // 이 옵션만으로 알림창이 사라지지 않을 수 있습니다.
        // skipWaiting: true,
        // clientsClaim: true
      }
      // 💡 개발 중에는 SW 업데이트 시 사용자에게 팝업을 띄우지 않도록 설정하는 것이 편리합니다.
    })
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", __vite_injected_original_import_meta_url))
    }
  },
  optimizeDeps: {
    include: ["quill", "vue3-naver-maps"]
  },
  server: {
    port: 5173,
    host: true,
    // 외부에서도 접근 가능하게
    allowedHosts: [
      "hellokiyo.ngrok.io",
      // ngrok 도메인 허용
      "subterete-reedy-dillon.ngrok-free.dev"
    ],
    proxy: {
      // Vue에서 '/api/'로 시작하는 모든 요청을 Spring Boot 서버 (8080)로 전달
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: springBootStatic,
    // 빌드 결과 Spring Boot static으로
    emptyOutDir: true
    // 기존 내용 삭제 후 빌드
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcuanMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJEOlxcXFxtaW5ob1xcXFxmcm9udGVuZFwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRDpcXFxcbWluaG9cXFxcZnJvbnRlbmRcXFxcdml0ZS5jb25maWcuanNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0Q6L21pbmhvL2Zyb250ZW5kL3ZpdGUuY29uZmlnLmpzXCI7aW1wb3J0IHsgZmlsZVVSTFRvUGF0aCwgVVJMIH0gZnJvbSAnbm9kZTp1cmwnXHJcbmltcG9ydCBwYXRoIGZyb20gJ25vZGU6cGF0aCdcclxuaW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZSdcclxuaW1wb3J0IHZ1ZSBmcm9tICdAdml0ZWpzL3BsdWdpbi12dWUnXHJcbmltcG9ydCB2dWVEZXZUb29scyBmcm9tICd2aXRlLXBsdWdpbi12dWUtZGV2dG9vbHMnXHJcbmltcG9ydCB7IFZpdGVQV0EgfSBmcm9tICd2aXRlLXBsdWdpbi1wd2EnXHJcblxyXG4vLyBTcHJpbmcgQm9vdCBzdGF0aWMgXHVEM0Y0XHVCMzU0IChmcm9udGVuZCBcdUFFMzBcdUM5MDAgXHVDMEMxXHVCMzAwXHVBQ0JEXHVCODVDKVxyXG5jb25zdCBzcHJpbmdCb290U3RhdGljID0gcGF0aC5yZXNvbHZlKF9fZGlybmFtZSwgJy4uL2JhY2tlbmQvc3ByaW5nYm9vdC9zcmMvbWFpbi9yZXNvdXJjZXMvc3RhdGljJylcclxuXHJcbi8vIGh0dHBzOi8vdml0ZS5kZXYvY29uZmlnL1xyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoe1xyXG4gIHBsdWdpbnM6IFtcclxuICAgIHZ1ZSgpLFxyXG4gICAgdnVlRGV2VG9vbHMoKSxcclxuICAgIFZpdGVQV0Eoe1xyXG4gICAgICAgICAgc3RyYXRlZ2llczogJ2luamVjdE1hbmlmZXN0JyxcclxuICAgICAgICAgIGluamVjdE1hbmlmZXN0OiB7XHJcbiAgICAgICAgICAgIG1heGltdW1GaWxlU2l6ZVRvQ2FjaGVJbkJ5dGVzOiA2ICogMTAyNCAqIDEwMjQsIC8vIDZNQlx1Qjg1QyBcdUM2MkNcdUI5QkNcclxuICAgICAgICAgIH0sXHJcblxyXG4gICAgICAgICAgLy8gXHVEODNEXHVERUE4IFx1Q0Q5NFx1QUMwMDogU2VydmljZSBXb3JrZXIgXHVDMThDXHVDMkE0IFx1RDMwQ1x1Qzc3QyBcdUM5QzBcdUM4MTVcclxuICAgICAgICAgIHNyY0RpcjogJ3B1YmxpYycsXHJcbiAgICAgICAgICBmaWxlbmFtZTogJ2ZpcmViYXNlLW1lc3NhZ2luZy1zdy5qcycsXHJcblxyXG4gICAgICAgICAgaW5jbHVkZUFzc2V0czogWydmYXZpY29uLmljbycsICdhcHBsZS10b3VjaC1pY29uLnBuZycsICdtYXNrZWQtaWNvbi5zdmcnXSxcclxuICAgICAgICAgIG1hbmlmZXN0OiB7XHJcbiAgICAgICAgICAgICAgbmFtZTogJ0lTZWFVIFBXQSBBcHAnLFxyXG4gICAgICAgICAgICAgIHNob3J0X25hbWU6ICdJU2VhVSBQV0EnLFxyXG4gICAgICAgICAgICAgIHRoZW1lX2NvbG9yOiAnI2ZmZmZmZicsXHJcbiAgICAgICAgICAgICAgaWNvbnM6IFtcclxuICAgICAgICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgc3JjOiAnaXNlYXUtMTkyLnBuZycsXHJcbiAgICAgICAgICAgICAgICAgICAgICBzaXplczogJzE5MngxOTInLFxyXG4gICAgICAgICAgICAgICAgICAgICAgdHlwZTogJ2ltYWdlL3BuZycsXHJcbiAgICAgICAgICAgICAgICAgICAgICAvLyBcdUQ4M0RcdURDQ0MgaWNvbnNcdUM3NTggcHVycG9zZSBcdUMxOERcdUMxMzEgXHVDMTI0XHVDODE1OiBtYXNrYWJsZSBcdUM1NDRcdUM3NzRcdUNGNThcdUM3NDQgXHVCQzE4XHVCNERDXHVDMkRDIFx1RDNFQ1x1RDU2OFx1RDU3NFx1QzU3QyBcdUQ1NjlcdUIyQzhcdUIyRTQuXHJcbiAgICAgICAgICAgICAgICAgICAgICBwdXJwb3NlOiAnbWFza2FibGUnXHJcbiAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgICAgICAgIHNyYzogJ2lzZWF1LTUxMi5wbmcnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgc2l6ZXM6ICc1MTJ4NTEyJyxcclxuICAgICAgICAgICAgICAgICAgICAgIHR5cGU6ICdpbWFnZS9wbmcnXHJcbiAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICBdXHJcblxyXG4gICAgICAgICAgfSxcclxuXHJcbiAgICAgICAgICAvLyBcdUQ4M0RcdURFQTggW1x1RDU3NVx1QzJFQ10gU1cgXHVCNEYxXHVCODVEIFx1QkMyOVx1QzJERCBcdUMxMjRcdUM4MTUgKFx1QUMxQ1x1QkMxQyBcdUMyREMgXHVDNTRDXHVCOUJDXHVDNzQ0IFx1Q0Q1Q1x1QzE4Q1x1RDY1NClcclxuICAgICAgICAgIHJlZ2lzdGVyVHlwZTogJ2F1dG9VcGRhdGUnLFxyXG4gICAgICAgICAgICB3b3JrYm94OiB7XHJcbiAgICAgICAgICAgICAgICAvLyBcdUFDMUNcdUJDMUMgXHVENjU4XHVBQ0JEXHVDNUQwXHVDMTFDIFx1Qjg1Q1x1QURGOFx1Qjk3QyBcdUM5MDRcdUM3NzRcdUIyOTQgXHVCMzcwIFx1QjNDNFx1QzZDMFx1Qzc0NCBcdUM5MERcdUIyQzhcdUIyRTQuXHJcbiAgICAgICAgICAgICAgICAvLyBcdUM3NzQgXHVDNjM1XHVDMTU4XHVCOUNDXHVDNzNDXHVCODVDIFx1QzU0Q1x1QjlCQ1x1Q0MzRFx1Qzc3NCBcdUMwQUNcdUI3N0NcdUM5QzBcdUM5QzAgXHVDNTRBXHVDNzQ0IFx1QzIxOCBcdUM3ODhcdUMyQjVcdUIyQzhcdUIyRTQuXHJcbiAgICAgICAgICAgICAgICAvLyBza2lwV2FpdGluZzogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIC8vIGNsaWVudHNDbGFpbTogdHJ1ZVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgLy8gXHVEODNEXHVEQ0ExIFx1QUMxQ1x1QkMxQyBcdUM5MTFcdUM1RDBcdUIyOTQgU1cgXHVDNUM1XHVCMzcwXHVDNzc0XHVEMkI4IFx1QzJEQyBcdUMwQUNcdUM2QTlcdUM3OTBcdUM1RDBcdUFDOEMgXHVEMzFEXHVDNUM1XHVDNzQ0IFx1Qjc0NFx1QzZCMFx1QzlDMCBcdUM1NEFcdUIzQzRcdUI4NUQgXHVDMTI0XHVDODE1XHVENTU4XHVCMjk0IFx1QUM4M1x1Qzc3NCBcdUQzQjhcdUI5QUNcdUQ1NjlcdUIyQzhcdUIyRTQuXHJcbiAgICAgIH0pXHJcbiAgXSxcclxuICByZXNvbHZlOiB7XHJcbiAgICBhbGlhczoge1xyXG4gICAgICAnQCc6IGZpbGVVUkxUb1BhdGgobmV3IFVSTCgnLi9zcmMnLCBpbXBvcnQubWV0YS51cmwpKVxyXG4gICAgfSxcclxuICB9LFxyXG4gIG9wdGltaXplRGVwczoge1xyXG4gICAgaW5jbHVkZTogWydxdWlsbCcsICd2dWUzLW5hdmVyLW1hcHMnXSxcclxuICB9LFxyXG4gICAgc2VydmVyOiB7XHJcbiAgICAgICAgcG9ydDogNTE3MyxcclxuICAgICAgICBob3N0OiB0cnVlLCAgICAgICAgLy8gXHVDNjc4XHVCRDgwXHVDNUQwXHVDMTFDXHVCM0M0IFx1QzgxMVx1QURGQyBcdUFDMDBcdUIyQTVcdUQ1NThcdUFDOENcclxuICAgICAgICBhbGxvd2VkSG9zdHM6IFtcclxuICAgICAgICAgICAgJ2hlbGxva2l5by5uZ3Jvay5pbycsICAgLy8gbmdyb2sgXHVCM0M0XHVCQTU0XHVDNzc4IFx1RDVDOFx1QzZBOVxyXG4gICAgICAgICAgICAnc3VidGVyZXRlLXJlZWR5LWRpbGxvbi5uZ3Jvay1mcmVlLmRldidcclxuICAgICAgICBdLFxyXG4gICAgICAgIHByb3h5OiB7XHJcbiAgICAgICAgICAgIC8vIFZ1ZVx1QzVEMFx1QzExQyAnL2FwaS8nXHVCODVDIFx1QzJEQ1x1Qzc5MVx1RDU1OFx1QjI5NCBcdUJBQThcdUI0RTAgXHVDNjk0XHVDQ0FEXHVDNzQ0IFNwcmluZyBCb290IFx1QzExQ1x1QkM4NCAoODA4MClcdUI4NUMgXHVDODA0XHVCMkVDXHJcbiAgICAgICAgICAgICcvYXBpJzoge1xyXG4gICAgICAgICAgICAgICAgdGFyZ2V0OiAnaHR0cDovL2xvY2FsaG9zdDo4MDgwJyxcclxuICAgICAgICAgICAgICAgIGNoYW5nZU9yaWdpbjogdHJ1ZVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfSxcclxuICAgIGJ1aWxkOiB7XHJcbiAgICAgICAgb3V0RGlyOiBzcHJpbmdCb290U3RhdGljLCAvLyBcdUJFNENcdUI0REMgXHVBQ0IwXHVBQ0ZDIFNwcmluZyBCb290IHN0YXRpY1x1QzczQ1x1Qjg1Q1xyXG4gICAgICAgIGVtcHR5T3V0RGlyOiB0cnVlLCAgICAgICAgIC8vIFx1QUUzMFx1Qzg3NCBcdUIwQjRcdUM2QTkgXHVDMEFEXHVDODFDIFx1RDZDNCBcdUJFNENcdUI0RENcclxuICAgIH0sXHJcbn0pXHJcbiJdLAogICJtYXBwaW5ncyI6ICI7QUFBMk8sU0FBUyxlQUFlLFdBQVc7QUFDOVEsT0FBTyxVQUFVO0FBQ2pCLFNBQVMsb0JBQW9CO0FBQzdCLE9BQU8sU0FBUztBQUNoQixPQUFPLGlCQUFpQjtBQUN4QixTQUFTLGVBQWU7QUFMeEIsSUFBTSxtQ0FBbUM7QUFBc0csSUFBTSwyQ0FBMkM7QUFRaE0sSUFBTSxtQkFBbUIsS0FBSyxRQUFRLGtDQUFXLGlEQUFpRDtBQUdsRyxJQUFPLHNCQUFRLGFBQWE7QUFBQSxFQUMxQixTQUFTO0FBQUEsSUFDUCxJQUFJO0FBQUEsSUFDSixZQUFZO0FBQUEsSUFDWixRQUFRO0FBQUEsTUFDRixZQUFZO0FBQUEsTUFDWixnQkFBZ0I7QUFBQSxRQUNkLCtCQUErQixJQUFJLE9BQU87QUFBQTtBQUFBLE1BQzVDO0FBQUE7QUFBQSxNQUdBLFFBQVE7QUFBQSxNQUNSLFVBQVU7QUFBQSxNQUVWLGVBQWUsQ0FBQyxlQUFlLHdCQUF3QixpQkFBaUI7QUFBQSxNQUN4RSxVQUFVO0FBQUEsUUFDTixNQUFNO0FBQUEsUUFDTixZQUFZO0FBQUEsUUFDWixhQUFhO0FBQUEsUUFDYixPQUFPO0FBQUEsVUFDSDtBQUFBLFlBQ0ksS0FBSztBQUFBLFlBQ0wsT0FBTztBQUFBLFlBQ1AsTUFBTTtBQUFBO0FBQUEsWUFFTixTQUFTO0FBQUEsVUFDYjtBQUFBLFVBQ0E7QUFBQSxZQUNJLEtBQUs7QUFBQSxZQUNMLE9BQU87QUFBQSxZQUNQLE1BQU07QUFBQSxVQUNWO0FBQUEsUUFDSjtBQUFBLE1BRUo7QUFBQTtBQUFBLE1BR0EsY0FBYztBQUFBLE1BQ1osU0FBUztBQUFBO0FBQUE7QUFBQTtBQUFBO0FBQUEsTUFLVDtBQUFBO0FBQUEsSUFFTixDQUFDO0FBQUEsRUFDTDtBQUFBLEVBQ0EsU0FBUztBQUFBLElBQ1AsT0FBTztBQUFBLE1BQ0wsS0FBSyxjQUFjLElBQUksSUFBSSxTQUFTLHdDQUFlLENBQUM7QUFBQSxJQUN0RDtBQUFBLEVBQ0Y7QUFBQSxFQUNBLGNBQWM7QUFBQSxJQUNaLFNBQVMsQ0FBQyxTQUFTLGlCQUFpQjtBQUFBLEVBQ3RDO0FBQUEsRUFDRSxRQUFRO0FBQUEsSUFDSixNQUFNO0FBQUEsSUFDTixNQUFNO0FBQUE7QUFBQSxJQUNOLGNBQWM7QUFBQSxNQUNWO0FBQUE7QUFBQSxNQUNBO0FBQUEsSUFDSjtBQUFBLElBQ0EsT0FBTztBQUFBO0FBQUEsTUFFSCxRQUFRO0FBQUEsUUFDSixRQUFRO0FBQUEsUUFDUixjQUFjO0FBQUEsTUFDbEI7QUFBQSxJQUNKO0FBQUEsRUFDSjtBQUFBLEVBQ0EsT0FBTztBQUFBLElBQ0gsUUFBUTtBQUFBO0FBQUEsSUFDUixhQUFhO0FBQUE7QUFBQSxFQUNqQjtBQUNKLENBQUM7IiwKICAibmFtZXMiOiBbXQp9Cg==
