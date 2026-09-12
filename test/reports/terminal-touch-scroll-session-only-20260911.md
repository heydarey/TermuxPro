# 终端触摸滚动临时模式回归

## 背景

真实体验反馈显示：在手机端使用 Claude Code / Codex CLI 时，用户期望手指上下滑动优先查看终端上方输出；
如果滑动被解释为 TUI 内部滚动、命令历史或列表移动，会被感知为“终端没法正常滚动”。这不是要重做
Termux 原生终端能力，而是 TermuxPro 增值工具箱不能污染原始终端阅读能力。

## 主动发现

| 候选观察 | 影响旅程 | 证据 | 分类 | 优先级 | 处理 |
|---|---|---|---|---|---|
| 工具箱的 TUI 滚动切换会写入持久偏好，误点后下次仍可能影响阅读历史 | 手机 SSH 到远端跑 AI CLI，想回看上方输出 | `toggleTouchScrollMode()` 调用 `setTerminalTouchScrollMode()` | 原始能力兼容守护、AI CLI UX | P1 | 本轮修复 |
| 工具箱文案没有表达“临时/可恢复”，用户不知道重新进入是否恢复默认 | 终端现场快速判断当前滑动语义 | `terminal_touch_scroll_*` 文案只说“改为控制” | UI/UX | P1 | 本轮修复 |
| 继续扩大 TUI 手势能力会增加原始终端风险 | Claude/Codex、less、vim、tmux 混合场景 | 触摸和鼠标滚轮语义差异明显 | 原始能力兼容守护 | P2 | 暂缓，只守护默认滚动 |

## 改动

- 工具箱里的“控制 AI/TUI 面板”改为当前终端临时模式，不再写入持久偏好。
- 顶栏工具箱状态读取当前 `TerminalView` 实际模式，避免按钮文案和当前行为不一致。
- 文案明确“临时控制”，提示重新打开终端后默认恢复历史滚动。
- `TerminalView` 增加只读模式 getter，不修改 PTY、scrollback 或底层滚动算法。

## 非目标

- 不重写 Termux 原始终端滚动算法。
- 不改变外接鼠标滚轮兼容路径。
- 不替代设置页里的持久偏好；高级用户仍可在设置页调整。
- 不向远端 shell、tmux、Claude Code 或 Codex CLI 注入命令。

## 验收重点

- 新开终端默认仍是历史滚动。
- 工具箱临时切到 TUI 后，按钮和提示能清晰引导切回历史模式。
- 再次打开终端不因上一次工具箱误点而持续污染触摸滚动。
- 原始终端、PTY、软键盘和外接鼠标滚轮不受影响。

## 本地验证

- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./test/terminal-touch-scrollback-test.sh`：通过。
- `./test/version-metadata-test.sh`：通过。
- `./test/workflow-trigger-policy-test.sh`：通过。
- `./test/release-notification-format-test.sh`：通过。
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./scripts/pre-push-smoke.sh origin/dev`：脚本类门禁通过；
  Gradle/Robolectric 阶段在本机 180 秒超时，无代码失败输出。当前远程共享环境 KVM 不可用，本轮不启动
  本机模拟器；Android 编译、Robolectric 和截图验收交由 GitHub CI / Emulator UI 门禁完成。
