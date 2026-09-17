# AI CLI 会话中心大字体历史入口文案验收记录（2026-09-15）

## 本轮主动发现候选

| 观察 | 来源 | 影响旅程 | 增值服务分类 | 优先级 | 决策 |
|---|---|---|---|---|---|
| 最新 Emulator UI 200% 字体截图中，AI CLI 会话中心的历史按钮虽然可见，但“Codex 历史 / 不自动恢复”在双列按钮内被拆成多行，用户扫读时要重新拼词。 | 负责人复核 `termuxpro-emulator-ui-373` 的 `ai-cli-session-center-font200.png` | 手机 → AI CLI 会话中心 → 手动打开 Claude/Codex 历史选择器 | AI CLI、移动端交互体验 | P1 | 本轮处理：大字体按钮只表达工具和“历史手选”，安全边界上移到分组标签和无障碍说明。 |
| 终端抽屉 200% 字体下“会话 / 工作台 / AI / 工具箱”仍可见，但空间非常紧，继续加长入口会挤压终端主区域。 | 负责人复核 `terminal-navigation-font200.png` | 终端 → 顶栏增值工具入口 | 上下文工具箱、移动端交互体验 | P2 | 暂缓：不新增顶栏文案，后续只在 contentDescription 或工具箱内部增强说明。 |
| 工作区 200% 字体下 SSH 地址占位示例被裁切，但字段常驻标签和主操作仍清晰；相比之下 AI 历史按钮是当前更高频、更靠近用户反馈的痛点。 | 负责人复核 `workspace-font200.png` | 工作台 → 编辑服务器与项目 | 远程工作区、移动端交互体验 | P2 | 暂缓：后续评估示例文案折叠或辅助说明，不在本轮扩散。 |

## 改动范围

- 仅调整 AI CLI 会话中心在 `fontScale >= 1.5` 时使用的紧凑文案。
- 历史分组从“打开历史选择器”改为“历史手选，不自动恢复”，把关键安全语义放到分组层。
- Claude/Codex 历史按钮从“历史 / 不自动恢复”改为“历史手选”，减少双列按钮内断词。
- 普通字体完整文案、contentDescription、启动命令、历史记录、tmux 策略均不变。

## 非目标

- 不新增弹窗或按钮，不增加启动 AI 的点击步骤。
- 不读取 Claude/Codex 私有历史，不自动恢复历史，不自动进入 tmux。
- 不改动 Termux 原始终端、PTY、滚动、基础会话或输入行为。
- 不处理工作区占位示例和终端抽屉空间问题，仅记录为后续候选。

## 验收标准

- 200% 字体下，AI 会话中心历史入口不再把“不自动恢复”拆成难读的多行按钮文本。
- 用户仍能在同一屏理解历史入口是手动选择，TermuxPro 不自动恢复。
- contentDescription 仍包含完整“新开 SSH 打开 Claude/Codex 历史选择器”和共享账号归属提醒。
- 中英文资源 key 保持一致，AI 启动决策静态门禁继续通过。

## 回归面

- `AiCliSessionCenterActivityTest.largeFontUsesCompactVisibleAiActionsWithoutLosingSafetyDescriptions`
- `test/android-string-resource-parity-test.sh`
- `test/ai-launch-decision-copy-test.sh`
- GitHub Emulator UI：推送后复跑默认字体与 200% 字体截图。

## 本地验证

- `git diff --check`：通过。
- `test/android-string-resource-parity-test.sh`：通过。
- `test/ai-launch-decision-copy-test.sh`：通过。
- `test/pre-push-smoke-test.sh`：通过。
- `scripts/validate-skills.sh`：通过。
- 定向 Robolectric：本机共享服务器缺少 NDK `27.0.12077973`，离线配置阶段失败；不在本轮修改系统 SDK/NDK，
  推送后由 GitHub Runner 完成 Android 编译与 Emulator UI 验收。

## 复盘

- 减少的真实负担：大字体用户不用在窄按钮里拼读被拆碎的安全提示，能更快区分“新开 SSH”和“历史手选”。
- 控件噪声：没有新增控件，只重新分配文案层级。
- 原始 Termux 能力：未触碰基础终端能力，只优化 TermuxPro AI CLI 增值层。
- 下一轮优先检查：工作区首页大字体下 SSH 地址示例裁切是否会影响首次配置，以及是否需要把示例迁移到辅助提示。
