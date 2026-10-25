# AI CLI 本地启动记录操作说明回归

## 背景

本轮从移动端真实使用场景复核 AI CLI 会话中心：用户主要通过手机 SSH 到远端服务器使用 Claude Code
和 Codex CLI，本地启动记录只是 TermuxPro 的入口记忆，不等于 Claude/Codex 远端历史库，也不应暗示会
自动进入 tmux 或自动恢复私有会话。

## 本轮观察

1. 无有效工作区时，“再次打开 / 删除最近 / 查看全部 / 清空”按钮虽被禁用，但没有直接解释为什么不可用，
   容易让用户误以为功能坏了。
2. 当前工作区无本地记录时，按钮只显示默认名称，不够明确“当前工作区还没有记录”和“不影响远端历史”。
3. 最近记录目标已变化时，主文案已有防误入提示，但按钮的可访问性说明没有强调“点击只打开检查/删除对话，
   不会启动 Claude/Codex”。

## 改动范围

- `AiCliSessionCenterActivity`：按无工作区、空记录、有记录、目标变更四种状态动态设置按钮说明。
- 中英文资源：补齐本地启动记录按钮的 disabled/active/stale contentDescription。
- Robolectric 用例：覆盖无工作区、空记录、正常记录、目标变更和清空后的说明。

## 明确非目标

- 不读取 Claude/Codex 私有历史。
- 不自动选择 Claude/Codex 历史。
- 不自动进入 tmux。
- 不删除远端 AI 历史、tmux 会话或终端输出。

## 验收点

- 用户看到禁用按钮时能理解当前缺少什么，而不是猜测应用故障。
- 删除和清空操作始终说明“只删除 TermuxPro 本地记录”。
- 目标变更记录只能进入检查/删除对话，不会直接重放到另一个服务器或项目。

## 本地验证

- `git diff --check`：通过。
- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./scripts/release-window-guard.sh`：通过，当前稳定版 `v0.10.1` 今天刚发布，允许继续日常迭代。
- `./test/version-metadata-test.sh`：通过。
- `./test/workflow-trigger-policy-test.sh`：通过。
- `./test/release-notification-format-test.sh`：通过。
- `./test/release-window-guard-test.sh`：通过。
- `./test/github-noise-audit-test.sh`：通过。
- `./test/github-cli-wrapper-test.sh`：通过。
- `./test/github-check-suites-test.sh`：通过。
- `./test/context-checkpoint-test.sh`：通过。
- `./test/goal-lifecycle-policy-test.sh`：通过。
- `./test/generated-cache-cleanup-test.sh`：通过。
- `./test/android-sdk-bootstrap-test.sh`：通过。
- `TERMUXPRO_DRY_RUN=1 ./scripts/pre-push-smoke.sh origin/dev`：通过；映射到 GitHub CI 继续跑
  `AiCliSessionCenterActivityTest`、`AiCliLaunchCommandTest`、`AiTerminalActionTest`、
  `CustomLayoutsSmokeTest`、`RemoteToolRecoveryTest`、`WorkspaceActivitySmokeTest` 和
  `WorkspaceCommandBuilderTest`。
- `:app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：本共享远程环境 300 秒内无输出并
  被 timeout 停止；未继续重试以避免占用 CPU/内存，完整 Robolectric 结果以 GitHub CI 为准。
