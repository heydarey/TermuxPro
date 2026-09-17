# 快捷指令运行目标边界验收记录（2026-09-17）

## 主动发现

| 观察 | 来源 | 影响旅程 | 优先级 | 处理 |
|---|---|---|---|---|
| 快捷指令执行确认只展示服务器、目录和命令，未明确说明会新开独立 SSH 会话。 | 负责人代码走查 | 手机 SSH 远程开发中运行测试、构建或 AI 后验证命令 | P1 | 本轮修复 |
| 直接运行按钮的无障碍说明只列出目标和目录，读屏或大字体用户仍需推断是否会输入当前 Claude/Codex TUI。 | UI/UX 走查 | 共享 Claude 账号、当前终端正在运行 AI CLI 时避免污染输入上下文 | P1 | 本轮修复 |
| 这不是 Termux 原始终端能力重做，而是 TermuxPro 自定义快捷指令增值层的最后决策点安全边界。 | 产品准入检查 | 自定义快捷指令、远程工作区、移动端工作流效率 | P1 | 纳入主线 |

## 改动

- 快捷指令确认弹窗改为展示 `目标：host:port · 目录`。
- 确认弹窗明确说明：新开独立 SSH 会话，不会把命令输入当前终端、Claude Code 或 Codex CLI，也不会自动进入 tmux。
- 直接运行按钮的无障碍描述同步说明独立 SSH 会话和不污染当前终端/AI TUI。

## 非目标

- 不改变快捷指令 CRUD、危险命令识别、工作区隔离和实际 SSH 执行命令。
- 不研究或重做 Termux 原始终端、PTY、本地 shell、基础会话或基础快捷键。

## 验收

- Robolectric 回归断言确认弹窗包含独立 SSH、当前终端/AI TUI 隔离和不自动进入 tmux 的边界。
- 直接运行按钮内容描述包含同样边界。
- 执行仍通过 `EXTRA_NEW_SESSION=true` 打开新的 `TermuxActivity` SSH 会话。

## 本地验证记录

- `./test/android-string-resource-parity-test.sh`：通过。
- `git diff --check`：通过。
- `./scripts/pre-push-smoke.sh`：静态门禁全部通过；进入 Gradle 阶段后本地共享服务器无新输出等待数分钟，为避免占用共享机资源已中断。本机缺少 Android NDK 27，定向 Robolectric 首次运行在配置阶段提示 `NDK not configured`；完整 Android 单元测试、Lint 和 APK 构建以 GitHub CI 为准。
