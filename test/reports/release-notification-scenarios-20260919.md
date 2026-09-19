# 发布通知重点体验场景验收（2026-09-19）

## 背景

用户反馈版本上架通知过于笼统：只能知道有新版本，却不知道新版本应重点体验什么、解决了哪些真实痛点。
既有脚本已经包含版本、类型、Release URL、APK SHA-256、新增能力、修复问题、已知限制、验收状态和
用户动作，但仍缺少“本版最值得打开 App 验证哪条旅程”的明确提示。

## 主动发现

| 观察 | 证据 | 影响旅程 | 增值服务分类 | 处理 |
|---|---|---|---|---|
| 通知有功能/修复，但没有映射到真实体验路径 | `scripts/termuxpro-release-notification.sh` | 用户不知道该优先验证 SSH、AI CLI、Git/tmux 还是快捷指令 | 发布交付体验、移动端工作流效率 | 本轮修复 |
| 格式测试没有锁定体验场景字段 | `test/release-notification-format-test.sh` | 后续通知可能再次退回“上架消息 + 功能列表” | QA/发布治理 | 本轮补门禁 |
| Codex/Claude/AGENTS 规则缺少体验场景要求 | `.agents/skills`、`.claude/skills`、`AGENTS.md` | 其他智能体接手发布时可能按旧口径生成通知 | 维护交接稳定性 | 本轮同步 |

## 本轮变更

- `scripts/termuxpro-release-notification.sh` 新增必填 `--scenarios` / `--scenarios-file`。
- 通知正文新增“重点体验场景”段落。
- `test/release-notification-format-test.sh` 校验该字段存在且缺失时失败。
- 同步 AGENTS、Codex skill、Claude skill 和 backlog 的通知口径。

## 非目标

- 不发送真实飞书通知。
- 不创建候选版或正式 Release。
- 不改变 App 运行时代码、SSH/tmux/Git/AI CLI 行为。

## 验收

- `test/release-notification-format-test.sh`
- `test/workflow-trigger-policy-test.sh`
- `scripts/validate-skills.sh`

