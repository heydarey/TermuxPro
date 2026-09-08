# AI CLI tmux 策略可见性验收记录

日期：2026-09-07

## 结论

状态：`PASS`。

AI CLI 会话中心现在展示当前工作区配置的连接/tmux 策略，并额外明确 AI 快捷启动策略：始终只建立 SSH，
启动前不会继承工作区的自动 tmux 进入策略。这避免用户在共享 Claude 账号或多人 tmux 环境中误以为
“Claude/Codex 新建/历史”会自动进入某个 tmux 会话。

## 增值服务准入

- 分类：AI CLI、远程工作区、tmux 可视化、移动端工作流效率。
- 用户问题：同一个远程工作区可能配置为“仅进入指定 tmux”或“创建/进入指定 tmux”，但 AI CLI 中心
  的四个启动入口实际固定 `ssh_only`，如果页面不展示差异，用户容易误判上下文。
- 增值点：在启动 AI CLI 前把工作区策略和 AI 快捷启动策略同时展示，降低误入他人 tmux 或共享 Claude
  历史上下文的风险。
- 非目标：不修改 Termux 原始终端、PTY、本地 shell、基础会话或基础快捷键；不自动进入、创建、重命名
  或停止 tmux；不读取 Claude/Codex 历史。

## 安全与架构评审

- `WorkspaceTargetStore` 只读解析已有工作区 `connectionPolicy` 和 `sessionName`，不改变存储结构。
- AI CLI 启动命令仍调用 `WorkspaceCommandBuilder.buildSshCommand(..., POLICY_SSH_ONLY, "")`。
- UI 只展示策略，不把工作区自动 tmux 策略应用到 Claude/Codex 启动入口。

## UI/UX 验收

- 有效工作区下，目标详情包含：
  - `host:port · path`
  - 工作区连接策略
  - AI 快捷启动策略
- 未配置工作区时仍展示空状态，并回退到工作台配置。
- 策略文案覆盖普通 SSH、只列出 tmux、仅进入指定 tmux、创建或进入指定 tmux、未知策略。

## 自动化验证

- `git diff --check`
- `./scripts/validate-skills.sh`
- `./test/dialog-readable-style-test.sh`
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest --tests com.termux.app.WorkspaceTargetStoreTest --max-workers=2`

## 回归面

- AI 新建/历史入口仍不包含 `tmux attach-session` 或 `tmux new-session`。
- 远端环境预检、tmux 会话中心、Git 工作台和快捷指令入口仍复用当前 active workspace。
- 旧版工作区配置缺少策略字段时，默认展示普通 SSH。
