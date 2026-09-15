# AI CLI 会话中心新开 SSH 语义验收记录（2026-09-15）

## 本轮主动发现候选

| 观察 | 来源 | 影响旅程 | 增值服务分类 | 优先级 | 决策 |
|---|---|---|---|---|---|
| AI CLI 会话中心首屏按钮写“新建 Claude / 新建 Codex”，但实际行为是新开一个 SSH 终端并执行 CLI；多工作区用户需要靠上下文自行推断会不会复用当前终端。 | 负责人主动走查 `AiCliSessionCenterActivity` 和 `activity_ai_cli_session_center.xml` | 手机工作台 → AI CLI 会话中心 → 新建 Claude/Codex | AI CLI、远程工作区、移动端交互体验 | P1 | 本轮处理：按钮和分组直接写“新开 SSH”。 |
| “历史选择器”按钮虽然已有“不自动恢复”，但没有把“新开 SSH 终端”抬到视觉文案；共享 Claude 账号下，用户可能仍担心 TermuxPro 会在当前 tmux/AI 会话里注入命令。 | UI/UX 语义审计 | 共享账号 → 打开历史选择器 → 手动选择历史 | AI CLI、Claude/Codex 共享账号安全 | P1 | 本轮处理：历史分组和按钮统一说明“新开 SSH 打开历史”。 |
| 本地只检查重复资源 key，不检查默认英文与简体中文 key 是否一致；本机 NDK 缺失时，默认资源缺 key 只能等远端 Java 编译才暴露。 | CI 失败复盘 | 多语言资源维护、发布构建 | 移动端工作流效率、可维护性 | P1 | 本轮修复：恢复缺失 key，并新增中英文资源 key 对齐门禁。 |
| 首次修复后远端 Emulator UI 在 200% 字体截图阶段失败，说明完整“新开 SSH 打开历史选择器”文案在窄屏大字体下有挤压风险。 | GitHub Runner `34944891386` 失败产物与默认截图复核 | AI CLI 会话中心 → 200% 字体 → 四个启动入口 | AI CLI、移动端交互体验 | P1 | 本轮继续处理：大字体下按钮显示短文案，完整安全语义保留在 contentDescription 和普通字体资源中。 |

## 改动范围

- AI CLI 会话中心推荐分组改为“推荐：新开 SSH 终端启动”。
- 历史分组改为“谨慎：新开 SSH 终端打开历史选择器”。
- Claude/Codex 四个启动按钮均显式说明“新开 SSH”。
- 无障碍说明同步表达：新开 SSH 终端、不会自动恢复历史、不会自动进入 tmux。
- 新增 `test/android-string-resource-parity-test.sh`，并纳入 `scripts/pre-push-smoke.sh`。
- 200% 字体下将四个 AI 操作按钮收敛为“Claude/Codex + 新开 SSH/历史 + 不自动恢复”的短文案，减少按钮高度和首屏挤压；完整“新开 SSH 打开历史选择器”语义保留给辅助技术。

## 非目标

- 不新增二次确认弹窗，避免高频新建 AI 会话多一次点击。
- 不改变 Claude/Codex 命令、SSH 连接命令、历史记录存储或 tmux 策略。
- 不读取 Claude/Codex 私有历史，不自动选择历史，不自动进入 tmux。
- 不改动 Termux 原始终端、PTY、滚动、输入或基础会话行为。

## 验收标准

- AI CLI 会话中心首屏能直接看出新建和历史入口都会新开 SSH 终端。
- 历史入口仍明确“不自动恢复”，共享账号下需要用户自己确认会话归属。
- 点击新建仍直接打开新的 `TermuxActivity`，并使用 `POLICY_SSH_ONLY`，不包含 `tmux attach-session` 或 `tmux new-session`。
- 资源文件无重复 key，中文与英文资源 key 对齐，避免默认资源缺失到远端编译才暴露。

## 回归面

- `AiCliSessionCenterActivityTest.showsSafeEmptyStateWithoutReadingPrivateAiHistory`
- `AiCliSessionCenterActivityTest.aiActionsOpenIndependentSshOnlyTerminalWithoutAutoTmux`
- `AiCliSessionCenterActivityTest.largeFontUsesCompactVisibleAiActionsWithoutLosingSafetyDescriptions`
- `AiCliSessionCenterActivityTest.aiRiskCueExplainsPlainSshPolicyBeforeLaunch`
- `test/android-string-resource-parity-test.sh`
- `test/dialog-readable-style-test.sh`
- GitHub Emulator UI：需复跑覆盖默认字体与 200% 字体截图，验证 PR #357 从 `UNSTABLE` 恢复。

## 复盘

- 减少的真实负担：开发者不用在手机上猜“新建 AI”是新开终端、复用当前终端，还是自动进入 tmux。
- 控件噪声：没有新增按钮和弹窗，只把已有入口说清楚。
- 原始 Termux 能力：未修改原始终端路径，只打磨 TermuxPro AI CLI 增值层。
- 下一轮优先检查：AI CLI 会话中心后半页“本地启动记录 / AI 完成后 / 启动前确认”在真实历史较多时是否仍然容易扫读；资源类改动必须先跑 key 对齐门禁。
