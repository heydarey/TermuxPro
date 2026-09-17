# 工作区大字体 SSH 地址示例验收记录（2026-09-15）

## 本轮主动发现候选

| 观察 | 来源 | 影响旅程 | 增值服务分类 | 优先级 | 决策 |
|---|---|---|---|---|---|
| 最新 Emulator UI 200% 字体截图中，工作区 SSH 地址输入框的示例 `例如 hdr@192.168.1...` 在输入框内被裁切，容易显得页面未适配大字体。 | 负责人复核 `termuxpro-emulator-ui-373` 的 `workspace-font200.png` | 首次打开工作台 → 配置 SSH 地址 → 打开远程终端 | 远程工作区、移动端交互体验 | P1 | 本轮处理：大字体下使用短 hint“user@host 或 ssh 命令”。 |
| 字段常驻标签已经清楚表达“SSH 地址（支持粘贴 ssh 命令）”，具体 IP 示例在大字体下不是必需信息，保留反而制造裁切噪声。 | UI/UX 层级审计 | 大字体/窄屏 → 识别字段含义 | 远程工作区、移动端交互体验 | P1 | 本轮处理：普通字体保留具体示例，大字体让标签承担完整语义。 |
| 工作区主按钮、tmux 连接方式和路径字段在 200% 字体截图中仍完整可达；此时重排整个表单会扩大风险。 | QA 截图复核 | 工作台 → 表单填写 → 打开远程终端 | 远程工作区、移动端工作流效率 | P2 | 暂缓：本轮只消除被裁切示例，不调整表单结构。 |

## 改动范围

- `WorkspaceActivity` 在 `fontScale >= 1.5` 时把 SSH 地址输入框 hint 改为短文案。
- 新增 `workspace_host_hint_compact` 中英文资源。
- `WorkspaceActivitySmokeTest` 增加大字体断言，确保短 hint 不改变字段标签与 `labelFor` 关系。

## 非目标

- 不改变 SSH 地址解析、保存、连接、诊断或错误处理逻辑。
- 不改变普通字体下的具体 IP 示例。
- 不调整工作区表单结构、不新增字段、不改变 tmux 策略。
- 不改动 Termux 原始终端或本地 shell 能力。

## 验收标准

- 200% 字体下 SSH 地址示例不再依赖较长 IP 文案，减少裁切风险。
- 字段标签仍保留“支持粘贴 ssh 命令”的完整语义。
- 输入框仍支持 `user@host`、`user@host:port`、`ssh -p port user@host` 等既有格式。
- 中英文资源 key 对齐，工作区大字体测试覆盖短 hint 行为。

## 本地验证

- `git diff --check`：通过。
- `test/android-string-resource-parity-test.sh`：通过。
- `test/pre-push-smoke-test.sh`：通过。
- `scripts/validate-skills.sh`：通过。
- `WorkspaceActivitySmokeTest.largeFontUsesShortSshAddressHintWithoutChangingFieldLabel`：本机共享服务器缺少
  NDK `27.0.12077973`，离线配置阶段失败；不在本轮修改系统 SDK/NDK，推送后由 GitHub Runner 完成
  Android 编译与 Emulator UI 验收。

## 复盘

- 减少的真实负担：大字体用户首次配置 SSH 时不会看到被截断的示例，从而降低“不适配/不专业”的感觉。
- 控件噪声：没有新增控件，只按字体场景切换辅助示例。
- 原始 Termux 能力：未触碰基础终端能力，只优化 TermuxPro 远程工作区配置体验。
- 下一轮优先检查：工作区大字体下 `tmux 连接方式` 按钮是否需要更短的场景化文案，避免在已配置状态中继续制造高度压力。
