# 终端工具箱 AI 启动当前会话语义验收记录（2026-09-15）

## 本轮主动发现候选

| 观察 | 来源 | 影响旅程 | 增值服务分类 | 优先级 | 决策 |
|---|---|---|---|---|---|
| 终端工具箱直接启动 Claude/Codex 时，弹窗标题仍是“启动 Claude/Codex”，按钮也只写“新建/历史”，容易让用户误以为会像工作台入口一样新开隔离 SSH 终端。 | 负责人主动走查 `TermuxActivity.showAiLaunchDialog` 与 `AiSessionDialog` | 已在某个 SSH/tmux/AI TUI 中工作 → 打开工具箱 → 启动 AI CLI | AI CLI、移动端交互体验、共享 tmux 安全 | P1 | 本轮处理：标题和按钮直接说明“发送到当前终端”。 |
| 当前已有段落提示“命令会发送到当前正在显示的终端”，但在手机弹窗中位于操作按钮下方，用户可能先看按钮并操作。 | UI/UX 评审 | 软键盘/小屏确认弹窗、共享 Claude 账号 | AI CLI、移动端工作流效率 | P1 | 本轮处理：把风险语义提升到标题和主按钮，不增加额外控件。 |
| 终端工具箱菜单描述仍写“在当前终端启动”，语义上不如“发送命令到当前显示终端”精确。 | 术语一致性审计 | 工具箱入口 → AI 选择弹窗 | AI CLI、上下文工具箱 | P2 | 本轮处理：菜单无障碍描述同步改为先确认再发送到当前终端。 |

## 改动范围

- `AiSessionDialog` 支持区分工作台入口与终端入口。
- 终端入口标题改为“发送到当前终端：Claude Code / Codex CLI”。
- 终端入口按钮改为“发送新建命令到当前终端 / 发送历史选择器命令到当前终端”。
- 终端工具箱菜单描述同步强调“命令发送到当前正在显示的终端”。

## 非目标

- 不改变 Claude/Codex 实际命令。
- 不自动选择历史、不自动进入 tmux、不读取 AI 私有历史。
- 不改变 Termux 原始终端、PTY、输入、滚动或本地会话语义。

## 验收标准

- 从工作台打开 AI 启动弹窗时，仍显示普通“启动 Claude/Codex”文案。
- 从终端工具箱打开 AI 启动弹窗时，标题和按钮都能直接看出命令会发送到当前终端。
- 菜单入口、弹窗标题、按钮和说明不再互相矛盾。
- 用户取消弹窗不会发送任何命令；确认后仍走既有 `confirmAndSendCommand` 路径。

## 回归面

- `AiCliLaunchCommandTest.terminalEntrypointTitleAndButtonsSayCurrentTerminal`
- `AiCliLaunchCommandTest.actionLabelsPreviewExactCliCommand`
- `TerminalProjectToolsMenuTest`
- `WorkspaceActivitySmokeTest.aiShortcutRequiresExplicitNewOrHistoryChoice`

## 复盘

- 减少的真实负担：开发者在手机终端里不必猜“启动”会新开终端还是污染当前 SSH/tmux/AI 会话。
- 控件噪声：没有新增按钮，只调整已有确认层级中的语义。
- 原始 Termux 能力：不拦截、不重写、不改变原始终端输入和会话行为。
- 下一轮优先检查：AI 会话中心的“重复上次”是否也需要在首屏更突出当前目标和安全边界。
