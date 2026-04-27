## 项目介绍

<p align="center">
  基于 SpringBoot3 + Vue3 开发前后端分离个人博客系统
</p>

## 运行环境

### 后端：

|   名称   | 环境  |
| :------: | :---: |
|  MySQL   |  8.0  |
|  Redis   | 7.2.3 |
| RabbitMQ | 最新  |
|  minio   | 最新  |
|   JDK    |  17   |

### 前端：

| 名称 |  环境   |
| :--: | :-----: |
| pnpm | 8.12.0  |
| node | 16.17.0 |

## 项目部署

**部署文档地址：** 项目 Wiki

## 技术介绍

**前台前端（博客）：** Vue3 + Pinia + Vue Router + TypeScript + Axios + Element Plus + Echarts……

**后台前端（管理）：** Vue3 + Pinia + Vue Router + TypeScript + Axios + Antdv Pro + Ant Design Vue……

**后端：** JDK17 + SpringBoot3 + SpringSecurity + Mysql + Redis + Quartz + RabbitMQ + Minio + Mybatis-Plus + Nginx + Docker……

## 项目特点

* 前后端分离，Docker Compose 一键部署
* RABC 权限模型 + SpringSecurity 动态权限管理
* 文章、相册、音乐、树洞、留言板、友链等完整博客功能
* 评论 Markdown + 表情包、代码高亮、图片预览
* 第三方登录（Gitee/Github）、邮件自动提醒
* 自建 MinIO 图片存储、Redis 接口限流、操作日志审计
* **AI 智能增强（规划中）**

## 后续计划

### AI 智能增强计划（核心）

#### 1. RAG 向量库集成

将博客文章内容向量化和存储，支持基于语义的智能搜索和问答。

**技术方案：**
- 向量数据库：Milvus / Qdrant / Chroma
- Embedding 模型：text-embedding-ada-002 / M3E / BGE
- LLM 接口：OpenAI API / 硅基流动 / 本地模型

**实现流程：**
```
文章发布 → 文档解析 → 文本切分 → Embedding → 向量存储
                                    ↓
用户提问 → Query Embedding → 向量检索 → Context 组装 → LLM 生成 → 返回
```

**涉及改动：**
- 后端：新增 VectorService、RagService，向量数据库集成
- 前端：新增 "AI 问答" 页面，支持语义搜索

#### 2. AI 辅助写作功能

在线使用 AI 进行文档编写，降低创作门槛。

**Phase 1 - AI 模板生成**
- 用户选择文章类型（教程、评测、科普等）
- AI 生成大纲或初稿，用户在此基础上修改

**Phase 2 - 实时辅助写作**
- AI 自动补全、续写、润色、翻译
- 流式输出（SSE/WebSocket），实时渲染

**涉及改动：**
- 后端：流式 LLM 调用（WebFlux / SseEmitter）
- 前端：Markdown 编辑器集成 AI 能力

#### 3. IDE 远程连接支持

支持 Trae/VSCode 连接服务器操作文件。

**方案 A - WebDAV/SFTP API**
- 后端提供文件操作接口（list、read、write、delete）
- IDE 通过协议连接远程服务器

**方案 B - Monaco Editor 在线编辑**
- 集成 Monaco Editor（VSCode 编辑器内核）
- 提供在线代码查看和编辑体验

**涉及改动：**
- 后端：文件管理 API 开发
- 前端：Monaco Editor 集成

### 待优化功能

- [ ] 实现后台导入导出
- [ ] 后台图片资源管理模块
- [ ] 后台首页数据大屏
- [ ] 博客 app 版本
- [ ] 博客小程序版本

### 已完成功能

- [x] 持续优化前台响应式
- [x] 新增用户设置、支持修改邮箱、头像、昵称...
- [x] 重构移动端首页/文章页面
- [x] 重构各种功能邮箱提醒
- [x] 实现前台搜索
- [x] 内置图片上传压缩
- [x] 相册管理
- [x] 前台音乐播放器
- [x] 实现黑名单管理机制
- [x] 前端响应速度优化到 5 秒以内
- [x] 找出并修复隐藏的 bug

## 鸣谢项目

* [mrzym-blog](https://gitee.com/mrzym/stable-version-of-blog)
* [掐指yi算'逢考必过-Blog](https://gitee.com/wu_shengdong/blog)
* [hexo-theme-butterfly](https://github.com/jerryc127/hexo-theme-butterfly)
* [Antdv Pro](https://docs.antdv-pro.com/)
* [md-editor-v3](https://imzbf.github.io/md-editor-v3/zh-CN/index)
* [vue-danmaku](https://github.com/hellodigua/vue-danmaku)
* [Heo表情包](https://github.com/zhheo/Sticker-Heo)
