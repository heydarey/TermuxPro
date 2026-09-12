# AI 历史入口显式选择语义验收（2026-09-11）

## 主动发现

- 旅程：开发者在手机首屏进入 AI CLI 会话中心，决定新建 Claude/Codex，还是继续历史上下文。
- 证据：首屏按钮原文为“Claude 历史 / Codex 历史”；既有 `test/reports/ux-audit-ssh-ai-tmux-20260909.md` 已记录这类按钮容易被理解成 TermuxPro 直接管理或自动恢复 Claude/Codex 历史。
- 风险：Claude Code 在共享账号下可能存在他人会话；按钮缺少“选择”会削弱用户显式确认意识。
- 增值服务分类：AI CLI、Claude/Codex 共享账号安全、移动端交互体验。

## 本轮调整

- 中文按钮改为“选择 Claude 历史 / 选择 Codex 历史”。
- 英文按钮改为“Choose Claude history / Choose Codex history”。
- 保持命令不变：Claude 仍进入 `claude --resume` 原生选择器，Codex 仍进入 `codex resume` 原生选择器。

## 验收标准

1. AI CLI 会话中心首屏按钮必须把历史入口表达为“选择”，不能暗示自动恢复最近会话。
2. 点击历史入口仍只打开 CLI 原生选择器，不读取私有历史，不自动选择会话。
3. AI 快捷启动仍强制 SSH-only，不继承工作区自动 tmux 策略。
4. 不改变 Termux 原始终端、PTY、本地 shell 或基础会话行为。
