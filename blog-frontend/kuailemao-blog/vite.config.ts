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
    
    // 2. 打印日志进行调试 (启动时请看控制台输出！)
    console.log('---------------------------------');
    console.log('当前运行模式:', mode);
    console.log('后端 API 地址 (VITE_SERVE):', env.VITE_SERVE);
    console.log('音乐 API 地址 (VITE_MUSIC_SERVE):', env.VITE_MUSIC_SERVE);
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
                    if (id.includes('node_modules')) {
                        // 增加简单的错误捕获防止 split 失败（虽然主要是为了修复 proxy）
                        try {
                            return id.toString().split('node_modules/')[1].split('/')[0].toString();
                        } catch (e) {
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
                    // 核心修复：如果 env.VITE_SERVE 是空的，回退到 127.0.0.1，防止 split 报错
                    target: env.VITE_SERVE || 'http://127.0.0.1:8088',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, '')
                },
                '/wapi': {
                    // 核心修复：如果 env.VITE_MUSIC_SERVE 是空的，给一个占位地址
                    target: env.VITE_MUSIC_SERVE || 'http://127.0.0.1:3000',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/wapi/, '')
                }
            }
        }
    }
})