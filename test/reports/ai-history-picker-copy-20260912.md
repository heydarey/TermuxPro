# AI 历史选择器入口文案验收记录（2026-09-12）

## 背景

TermuxPro 不读取 Claude Code 或 Codex CLI 的远端历史，也不替用户选择最近会话；它只能在正确 SSH 目标下
启动对应 CLI 的原生历史选择器。此前按钮使用“选择历史”或“选择历史会话”，容易让手机用户误以为
TermuxPro 已经拿到并管理了远端历史列表。

## 增值服务分类

- AI CLI
- 远程工作区
- 移动端交互体验

这不是 Termux 原始终端能力重做；只修正 AI CLI 增值入口的语义边界。

## 主动发现

1. “选择 Claude/Codex 历史”容易被理解为 App 内直接选择远端历史，而实际是打开 CLI 原生选择器。
2. 首页快捷启动弹窗、AI 会话中心和快捷指令模板使用的历史入口文案不完全一致，增加理解成本。
3. 共享 Claude 账号场景下，入口文案必须在按钮层面表达“只打开选择器，不自动恢复”，不能依赖长说明兜底。

## 本轮改动

- 通用 AI 启动弹窗按钮改为“打开历史选择器（不自动恢复）”。
- AI 会话中心按钮改为“Claude/Codex 历史选择器｜不自动恢复”，让按钮本身携带安全边界。
- 快捷指令模板改为“Claude/Codex：打开历史选择器”。
- 英文资源同步为 “Open history picker”。
- 启动命令、历史记录、确认弹窗和 SSH/tmux 策略均保持不变。

## 验收重点

- 用户能从按钮本身判断：TermuxPro 只是打开原生选择器，不读取、不展示、不自动选择远端历史。
- 新建会话仍是安全默认；历史选择器入口仍可达但语义更谨慎。
- 不增加首页控件，不改变 Termux 原生终端行为。

## 本地验证

- `rg` 扫描当前资源与测试断言：旧“选择 Claude/Codex 历史”仅保留在 backlog 历史说明中，当前 UI 资源
  和活跃测试已统一为“打开历史选择器”。
- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `test/dialog-readable-style-test.sh`：通过。
- 定向 Robolectric：
  `:app:testDebugUnitTest --tests AiCliSessionCenterActivityTest --tests WorkspaceActivitySmokeTest --tests CustomCommandsActivityTest`
  在本机 Gradle 配置阶段因共享机 Android NDK `27.0.12077973` 未配置失败，未进入测试断言；不作为功能
  通过证据，完整 Android 构建、Robolectric 和模拟器 UI 以 GitHub Runner 为准。
- `timeout --foreground 180s ./scripts/pre-push-smoke.sh`：脚本级门禁、Android SDK 引导测试和资源守卫
  均通过；进入 Gradle 阶段后 180 秒超时退出，未产生代码断言失败。本轮不在共享机安装全局 NDK 或启动
  本地模拟器。
- GitHub Runner 首次 CI 暴露静态门禁失败：历史入口按钮文本没有直接包含“不自动恢复”。已把 AI 会话
  中心两个历史按钮改为两行短文案，并同步更新 `test/ai-launch-decision-copy-test.sh`，防止只依赖
  contentDescription 或长说明兜底。
