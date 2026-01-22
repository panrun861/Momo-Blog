import { ConfigEnv, defineConfig, loadEnv } from 'vite'
import AutoImport from 'unplugin-auto-import/vite'
import viteCompression from 'vite-plugin-compression';
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { visualizer } from 'rollup-plugin-visualizer'
import vue from '@vitejs/plugin-vue'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import path from 'path'
import tailwindcss from 'tailwindcss'
import autoprefixer from 'autoprefixer'

// https://vitejs.dev/config/
export default defineConfig(({ mode }: ConfigEnv) => {
    
    // 1. 显式加载环境变量
    const env = loadEnv(mode, process.cwd());
    
    // 2. 打印日志进行调试（修改：统一变量名，匹配.env文件）
    console.log('---------------------------------');
    console.log('当前运行模式:', mode);
    // 修改1：使用.env里的VITE_APP_BASE_URL，而非VITE_SERVE
    console.log('后端 API 地址 (VITE_APP_BASE_URL):', env.VITE_APP_BASE_URL);
    // 修改2：给音乐API也加默认变量名（若.env里是VITE_APP_MUSIC_URL，需对应）
    console.log('音乐 API 地址 (VITE_APP_MUSIC_URL):', env.VITE_APP_MUSIC_URL);
    console.log('---------------------------------');

    return {
        plugins: [
            viteCompression({
                verbose: true,
                disable: false,
                threshold: 1024,
                algorithm: 'gzip',
                ext: '.gz',
                deleteOriginFile: false
            }),
            vue(),
            createSvgIconsPlugin({
                iconDirs: [path.resolve(process.cwd(), 'src/assets/icons')],
                symbolId: 'icon-[dir]-[name]',
            }),
            AutoImport({
                imports: ['vue', 'vue-router', 'pinia'],
                resolvers: [ElementPlusResolver()],
                dts: "src/types/auto-imports.d.ts",
            }),
            Components({
                resolvers: [ElementPlusResolver()],
                dts: "src/types/components.d.ts",
            }),
            visualizer({
                open: true,
                filename: 'visualizer.html'
            })
        ],
        resolve: {
            alias: {
                "@": path.resolve("./src")
            }
        },
        css: {
            preprocessorOptions: {
                scss: {
                    javascriptEnabled: true,
                    additionalData: '@import "./src/styles/variable.scss";',
                    api: 'modern-compiler', 
                    silenceDeprecations: ['legacy-js-api', 'import', 'mixed-decls', 'color-functions'],
                },
            },
            postcss: {
                plugins: [
                    tailwindcss,
                    autoprefixer,
                ]
            }
        },
        build: {
            rollupOptions: {
                output: {
                    chunkFileNames: 'js/[name]-[hash].js',
                    entryFileNames: 'js/[name]-[hash].js',
                    assetFileNames: '[ext]/[name]-[hash].[ext]',
                },
                manualChunks(id) {
                    // 修改3：强化manualChunks的空值保护，避免split报错
                    if (id && id.includes('node_modules')) {
                        try {
                            const parts = id.toString().split('node_modules/');
                            // 先判断parts[1]是否存在，再split
                            if (parts.length > 1 && parts[1]) {
                                const pkgName = parts[1].split('/')[0];
                                return pkgName ? pkgName.toString() : 'vendor';
                            }
                            return 'vendor';
                        } catch (e) {
                            console.warn('manualChunks 处理失败:', e);
                            return 'vendor';
                        }
                    }
                }
            }
        },
        server: {
            port: 99,
            host: '0.0.0.0',
            proxy: {
                '/api': {
                    // 修改4：使用正确的环境变量名 + 兜底，确保target非空
                    target: env.VITE_APP_BASE_URL || 'http://127.0.0.1:8088',
                    changeOrigin: true,
                    // 修改5：rewrite加空值保护
                    rewrite: (path) => (path ? path.replace(/^\/api/, '') : '')
                },
                '/wapi': {
                    // 修改6：统一音乐API的变量名 + 兜底 + rewrite保护
                    target: env.VITE_APP_MUSIC_URL || 'http://127.0.0.1:13000',
                    changeOrigin: true,
                    rewrite: (path) => (path ? path.replace(/^\/wapi/, '') : '')
                }
            }
        }
    }
})