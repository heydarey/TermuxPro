# AI CLI 历史入口启动确认回归报告（2026-09-12）

## 主动发现记录

| 候选 | 来源 | 优先级 | 增值服务分类 | 影响旅程 | 结论 |
| --- | --- | --- | --- | --- | --- |
| 历史入口一点即打开 Claude/Codex 原生历史选择器 | 负责人主动走查 AI CLI 会话中心 | P1 | AI CLI、远程工作区、移动端交互体验 | 共享 Claude/Codex 账号下，手机误触“历史选择”会直接进入远端 CLI 选择器，用户到最后一步才确认目标服务器和命令 | 本轮处理：历史入口先确认目标、路径、命令和不自动进入 tmux |
| 新建 AI 会话也缺少最后确认 | 负责人主动挑战既有假设 | P2 | AI CLI、移动端工作流效率 | 新建会话风险低于历史恢复；如果所有启动都加确认，会拖慢最高频新建路径 | 暂缓：保持“新建 Claude/Codex”一键启动 |
| “重复上次”在历史模式下仍可能脱离上下文 | 产品回归走查 | P1 | AI CLI、远程工作区 | 用户滚动到最近记录卡片底部后，点击重复历史记录仍可能不知道将打开哪个历史选择器 | 本轮处理：当重复的是历史选择模式，也先展示同一确认 |

## 本轮改动

- “选择 Claude 历史 / 选择 Codex 历史”不再立即启动 SSH 终端，而是先弹出确认。
- 确认页展示：
  - 将连接的 `host:port · path`
  - 实际执行命令，如 `claude --resume` / `codex resume`
  - TermuxPro 只打开 CLI 原生选择器
  - 不自动选择历史、不自动进入 tmux
  - 共享账号下必须确认会话归属
- “重复上次”或“查看全部记录 → 重复启动”如果重复的是历史选择模式，同样进入确认；重复新建会话仍保持快速启动。

## 非目标

- 不读取、解析、展示或删除 Claude/Codex 远端私有历史。
- 不自动恢复最近 AI 会话。
- 不改变 Termux 原始终端、PTY、滚动、包管理或本地 shell 行为。
- 不修改工作区 tmux 自动进入策略；AI CLI 启动仍固定为普通 SSH。

## 验收要点

- 点击历史入口后，确认前不会启动 `TermuxActivity`。
- 用户确认后才打开新的 SSH 终端，并执行对应 AI CLI 历史选择命令。
- 启动命令仍包含 `ssh -t` 和目标服务器，不包含 `tmux attach-session` 或 `tmux new-session`。
- 历史启动记录仍按当前工作区隔离保存，不读取远端 AI 历史或终端输出。

## 回归范围

- `AiCliSessionCenterActivityTest.aiActionsOpenIndependentSshOnlyTerminalWithoutAutoTmux`
- `AiCliSessionCenterActivityTest.repeatLastHistoryLaunchRequiresTargetConfirmation`
- `AiCliSessionCenterActivityTest.repeatsSelectedAiLaunchRecordFromManageDialog`
- `scripts/pre-push-smoke.sh`
