# AI CLI 远端环境预检入口验收记录

日期：2026-09-07

## 结论

状态：`PASS`。

本轮在 AI CLI 会话中心新增“检查 AI 远端环境”入口。用户准备启动 Claude Code 或 Codex CLI 前，可以
从同一上下文直接进入只读连接诊断，核对远端项目目录、tmux、Git、Node/Java、Claude Code 和 Codex
CLI 是否可用，降低手机端盲启动后的排错成本。

## 增值服务准入

- 分类：AI CLI、远程工作区、tmux/Git 可视化、移动端工作流效率。
- 用户问题：手机端 SSH 到远程服务器后，Claude/Codex 是否安装、项目路径是否正确、tmux/git 是否可用
  通常要靠手输命令确认；在软键盘场景下排错成本高。
- 增值点：把 AI CLI 启动前的远端工具链检查收敛到 AI 会话中心，复用当前工作区配置并进入结构化诊断页。
- 非目标：不研究、不重写 Termux 原始终端、PTY、包管理、本地 shell、基础会话、基础快捷键或基础文件
  能力；不读取 Claude/Codex 私有历史；不自动恢复最近会话；不自动进入或创建 tmux。

## 安全与架构评审

- 入口复用 `ConnectionDiagnosticNavigation.newIntentForActiveWorkspace` 和已有
  `WorkspaceCommandBuilder.buildConnectionDiagnosticCommand`。
- 诊断命令是只读 SSH 命令，只执行 `uname`、`cd` 和 `command -v`，不会写入远端、不创建 tmux、不
  attach tmux、不执行 AI CLI。
- 工作区缺失或配置不完整时回退到 `WorkspaceActivity`，让用户先补齐 SSH 目标和项目路径。
- 诊断页继续使用 `BatchMode=yes`，不会在后台保存密码或替用户接受主机指纹。

## UI/UX 验收

- AI CLI 会话中心新增按钮文案为“检查 AI 远端环境”，放在快捷指令模板与 tmux/Git 工具入口之间。
- 用户旅程：确认目标工作区 → 检查 AI 远端环境 → 根据 Claude/Codex/tmux/Git 可用性再决定新建或恢复
  AI 会话。
- 信息架构保持首页瘦身原则：该入口只出现在 AI 会话中心，不回流首页堆控件。

## 自动化验证

- `git diff --check`
- `./scripts/validate-skills.sh`
- `./test/dialog-readable-style-test.sh`
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest --tests com.termux.app.ConnectionDiagnosticNavigationTest --tests com.termux.app.WorkspaceCommandBuilderTest --max-workers=2`

## 回归面

- AI 新建/历史入口仍固定 `ssh_only`，不会自动进入 tmux。
- tmux、Git、快捷指令模板入口仍按原有目标路由。
- 无有效工作区时，AI 启动、tmux、Git 和诊断入口均回退工作台。
