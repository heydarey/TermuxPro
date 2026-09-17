# 终端顶部入口语义回归

## 背景

用户高频场景是在手机终端内通过 SSH 使用 Claude Code / Codex CLI。终端顶部入口需要在小屏上保持短文本，
但也必须让真实开发者清楚知道每个入口会做什么、不会做什么，避免把“会话”“远程配置”“工具箱·历史/TUI”
误解为头像、普通标签或会污染当前 shell 的快捷按钮。

## 主动发现

| 候选观察 | 影响旅程 | 证据 | 分类 | 优先级 | 处理 |
|---|---|---|---|---|---|
| `会话`入口的可访问说明只等于短标题，未说明不会连接新远端 | 手机 SSH 开发中切换终端标签 | `activity_termux.xml` 原 `contentDescription=@string/workspace_open_sessions` | 移动端交互体验 | P1 | 本轮修复 |
| `工具箱·历史/TUI` 顶部按钮只复用短标题，未解释当前手势滚动模式 | AI/TUI 与 scrollback 切换 | `TermuxActivity.updateTerminalToolsButtonState()` 原说明只取 label | 上下文工具箱、原始能力兼容守护 | P1 | 本轮修复 |
| 顶部按钮文字若继续加长会挤压 360dp / 200% 字体布局 | 窄屏单手操作 | `activity_termux.xml` 顶栏已有 4 个高频入口 | UI/UX | P2 | 保持短文本，仅增强 contentDescription |

## 改动

- `会话`入口新增完整说明：打开左侧本地终端会话列表，不连接新的远端服务器。
- `工具箱·历史/TUI` 保持短标签，但按当前模式提供不同说明：
  - 历史模式：手指上下滑动用于查看历史输出。
  - TUI 模式：手指上下滑动优先控制 AI 或 TUI 面板。
- 补充中英文资源和 Robolectric 布局/菜单断言。

## 非目标

- 不改变 Termux 原始终端的触摸滚动实现。
- 不新增首页控件。
- 不自动连接 SSH、tmux、Claude Code 或 Codex CLI。

## 验收重点

- 顶栏视觉仍保持短文本，不增加小屏拥挤。
- 无障碍语义和测试能说明入口边界。
- 终端原始 scrollback 默认语义不受影响。
