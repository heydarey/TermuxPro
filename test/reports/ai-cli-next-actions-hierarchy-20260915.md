# AI 会话中心后续动作层级验收记录

## 切片目标

让手机端远程 AI 开发流程更像一个闭环：启动 Claude Code / Codex CLI、查看本地启动记录之后，马上看到
“AI 完成后”应做的 Git 检查、项目任务、快捷指令和工作区配置入口，而不是先被长篇命令说明和安全提示打断。

## 主动发现记录

| 观察 | 来源 | 影响旅程 | 增值服务准入 | 结论 |
| --- | --- | --- | --- | --- |
| “AI 完成后”入口位于 Claude/Codex 命令参考和 tmux 安全提示之后，用户需要继续滚动才能进入 Git 或测试闭环。 | 负责人代码走查 `activity_ai_cli_session_center.xml` | 手机 SSH 到服务器运行 AI CLI 后检查改动、运行测试 | AI CLI、Git 可视化、移动端工作流效率 | 本轮推进 |
| 启动区和本地记录区已经能回答“我要开哪个 AI / 最近做了什么”，下一步应回答“AI 做完后我该去哪检查”。 | 产品旅程复盘 | Claude/Codex 输出完成后的验收与回归 | 上下文工具箱 | 本轮推进 |
| Claude/Codex 命令预览仍有价值，但它是参考信息，不应压在高频下一步操作之前。 | UI/UX 信息层级审计 | 大字体和小屏下减少滚动负担 | 移动端交互体验 | 本轮下沉，不删除 |
| 首页工具箱已完成渐进展示，不应把 Git/任务入口重新塞回首页。 | Backlog 复核 | 保持首页轻量，同时让 AI 场景内入口可发现 | 远程工作区、上下文工具箱 | 暂缓首页改动 |

## 实现摘要

- 为 AI 会话中心内容容器增加稳定 ID，便于布局顺序回归。
- 将“AI 完成后”卡片从页面底部前移到“TermuxPro 本地启动记录”之后。
- 保留“启动前先确认”、Claude/Codex 命令参考和 tmux 安全提示，但下沉为低频参考信息。
- 新增 Robolectric 布局顺序断言：`ai_cli_center_next_card` 必须早于 `ai_cli_center_prepare_card`。

## 非目标

- 不改变 SSH、tmux、Claude Code 或 Codex CLI 的启动命令。
- 不读取 Claude/Codex 私有历史，不自动进入 tmux，不改变 Termux 原始终端能力。
- 不增加首页控件，不发布候选包或正式包。

## 本地验证

- `test/workflow-trigger-policy-test.sh`：通过。
- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `test/dialog-readable-style-test.sh`：通过。
- `./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：
  未跑到测试断言，配置阶段访问 `https://dl.google.com/.../com.android.tools.build:gradle:8.13.2`
  超时；这是当前远程机网络到 Google Maven 的本地环境限制，不是本切片断言失败。已确认没有残留
  Gradle 进程。
- `timeout --foreground 300s ./scripts/pre-push-smoke.sh`：静态、治理、版本、GitHub、Skill 和资源守卫阶段
  均通过；进入 Gradle 阶段后 300 秒超时且无断言失败输出。已确认没有残留 Gradle 进程。

Android 运行时、完整单测、Lint、Debug APK 和模拟器截图以后续 GitHub CI / Emulator UI 为准。
