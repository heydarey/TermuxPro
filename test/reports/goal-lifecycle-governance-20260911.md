# 长期 Goal 生命周期治理验收记录（2026-09-11）

## 结论

状态：`READY FOR DEV MERGE`。本轮修复的是 TermuxPro 自治维护流程缺陷：长期日常迭代不能因为单个切片
完成就被标记为 `complete`。后续单轮完成后只能继续下一轮，或在用户要求、额度低于 15%、资源不足、
外部服务不可用等情况下保存检查点并暂停等待恢复。

## 主动发现候选

| 候选观察 | 来源 | 影响旅程 | 分类 | 优先级 | 处理 |
|---|---|---|---|---|---|
| 单轮切片完成后把长期 Goal 标记为完成，会让无人值守维护静默停止 | 用户反馈 + 本轮实际操作复盘 | 日常迭代、断线恢复、无人值守维护 | 移动端工作流效率 / 发布交付治理 | P0 | 本轮修复 |
| 仓库规则已有额度连续运行，但没有明确禁止“切片完成=长期 Goal complete” | 负责人主动审计 `AGENTS.md` 与 skill | 会话恢复时 agent 可能重复犯同一流程错误 | 发布交付治理 | P1 | 本轮修复 |
| 只有文字规则仍可能退化，缺少脚本门禁 | 负责人主动审计 `scripts/pre-push-smoke.sh` | 后续 PR 可能移除关键规则但 CI 不发现 | 可维护性治理 | P1 | 本轮新增校验 |

## 增值服务准入检查

- 不是 Termux 原始终端、PTY、包管理、本地 shell、基础会话或基础快捷键能力。
- 直接服务 TermuxPro 的长期自治研发、发布治理和持续体验改进，避免用户反复手动重启维护任务。
- 不触碰用户数据、SSH 凭据、远端文件、真实 tmux 会话或 Android 原始终端行为。

## 改动范围

- `AGENTS.md`：明确 TermuxPro 日常维护是长期持久 Goal，单轮完成不得置为 `complete`。
- `.agents/skills/termuxpro-development/SKILL.md`：补充 Codex 侧长期 Goal 生命周期规则。
- `.claude/skills/termuxpro-development/SKILL.md`：补充 Claude Code 接手时的同口径规则。
- `test/goal-lifecycle-policy-test.sh`：新增回归校验，防止规则退化。
- `scripts/pre-push-smoke.sh`：把长期 Goal 生命周期校验纳入推送前静态门禁。

## 验收标准

1. 全新会话恢复时能从 AGENTS 和 skill 中读到“长期 Goal 只暂停不完成”的明确规则。
2. `scripts/pre-push-smoke.sh` 必须覆盖该规则校验。
3. 本轮不启动模拟器、不执行远端 tmux 写操作、不触碰 Termux 原始能力。

## 回滚方式

若该策略未来不再适合，只需删除本轮新增规则和 `test/goal-lifecycle-policy-test.sh`，并从
`scripts/pre-push-smoke.sh` 移除对应静态检查；不会影响 APK 运行时代码。
