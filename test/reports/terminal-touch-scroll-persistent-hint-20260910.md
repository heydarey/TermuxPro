# 终端触摸滚动模式持久提示验收（2026-09-10）

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| 用户切到 AI/TUI 控制模式后，顶部按钮会变成 `工具箱·TUI`，但临时提示几秒后消失，用户仍可能不知道为什么上下滑动不再像历史滚动。 | 用户真实反馈复核 + 负责人走查终端页 | 远程 SSH → Claude/Codex TUI → 回看上方输出 | 移动端交互体验、AI CLI/TUI 使用体验 | P1 | 本轮处理。TUI 模式会改变手指滑动语义，必须保留持久状态提示和回切线索。 |
| 现有 `terminal_feedback_banner` 只能显示临时反馈，如果直接复用临时 toast，状态提示会被自动隐藏，无法表达“当前模式”。 | 负责人代码走查 `TerminalFeedbackController` | 终端页所有前台反馈 | 移动端交互体验 | P1 | 本轮处理。反馈控制器新增持久状态能力，临时反馈结束后恢复持久状态。 |
| 直接改 `TerminalView` 滚动语义可能破坏 Termux 原始终端、外接鼠标和 TUI 程序兼容。 | 架构评审 | 原始终端 scrollback、外接鼠标、less/vim/tmux pane | 原始能力兼容守护 | P0 守护 | 本轮不改原生滚动路径，只改 TermuxPro 增值层提示和入口。 |

## 本轮范围

- 当触摸滚动模式为 `tui` 时，在终端顶部显示持久横幅：
  - 明确“当前为 AI/TUI 控制模式”；
  - 明确手指上下滑动会优先控制支持鼠标事件的面板；
  - 明确通过 `工具箱·TUI` 切回历史模式查看上方终端输出。
- 临时反馈仍可覆盖持久横幅，例如“已切换会话”或“已应用键区”；临时反馈结束后自动恢复 TUI 模式状态。
- 切回 `scrollback` 推荐模式后隐藏持久横幅，避免正常终端阅读场景被打扰。

## 非目标

- 不重写 Termux 原始 `TerminalView` 触摸滚动实现。
- 不改变默认模式：手机触摸默认仍查看终端历史。
- 不改变外接鼠标滚轮、less/vim/tmux pane 等程序内滚动兼容。
- 不自动检测或干预 Claude/Codex 私有 TUI 状态。

## 验收

本轮新增/更新以下回归：

- `TerminalFeedbackControllerTest.temporaryFeedbackRestoresPersistentStatusAfterTimeout`
  - 验证临时反馈结束后恢复持久 TUI 状态。
- `TerminalFeedbackControllerTest.hidingPersistentStatusLetsTemporaryFeedbackDisappearNormally`
  - 验证切回历史模式后，临时反馈按原逻辑消失，不留下空白横幅。
- 既有 `CustomLayoutsSmokeTest.terminalFeedbackUsesExplicitReadableColorsAndStartsHidden`
  - 继续验证反馈横幅有显式深色可读样式且默认隐藏。

## 结论

本切片降低了手机端 AI/TUI 模式下“上下滑动到底在干什么”的理解成本，同时不触碰 Termux 原始终端滚动
实现。由于当前远程机 KVM 不可用，本轮不在本机启动模拟器；Android 运行时证据继续依赖 GitHub
Emulator UI 和后续候选包/真机体验。
