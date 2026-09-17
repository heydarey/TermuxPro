# CURRENT_STATE 发布窗口规则防回退（2026-09-17）

## 结论

通过。`scripts/update-context-checkpoint.sh` 生成的恢复步骤已重新包含“满 5 天优先做发布评审、满 7 天必须正式发布或落盘有证据 HOLD、少发版不是不发版、一周至少一次正式稳定版发布”的完整规则。`test/context-checkpoint-test.sh` 已把这些句子纳入静态门禁，防止后续上下文检查点模板再次退回旧口径。

该切片属于自治研发与发布交付治理，不改变 Android 运行时、终端、SSH、tmux、Git、AI CLI 或快捷指令行为。

## 主动发现记录

| 候选观察 | 来源 | 影响旅程 | 分类 | 处理结论 |
| --- | --- | --- | --- | --- |
| `update-context-checkpoint.sh` 生成的恢复步骤只保留“满 7 天”，遗漏“满 5 天优先发布评审”。 | 会话恢复后运行脚本并检查 `docs/CURRENT_STATE.md` diff。 | 模型切换/上下文压缩后选择下一轮任务。 | 发布交付治理、自治研发可靠性 | 本轮修复。 |
| `context-checkpoint-test.sh` 只检查是否有“稳定版发布窗口”，没有检查具体发布节奏口径。 | 测试覆盖审计。 | 防止规则文件已更新但恢复检查点仍是旧模板。 | 测试门禁 | 本轮补断言。 |
| 若恢复检查点缩短规则，后续可能误把“不要频繁发布”理解成“未满 7 天完全不用评审”。 | 发布节奏复盘。 | 稳定版一周至少一版或明确 HOLD 的 SLA。 | 发布交付体验 | 本轮防回退。 |

## 变更范围

- `scripts/update-context-checkpoint.sh`
  - 恢复步骤第 5 条补齐满 5 天评审、满 7 天发布/HOLD、少发版不是不发版、一周至少一次正式稳定版发布。
- `test/context-checkpoint-test.sh`
  - 新增关键发布节奏句子断言。

## 非目标

- 不改 `scripts/release-window-guard.sh` 的状态机。
- 不触发候选版或正式版发布。
- 不改变 Android 产品功能、UI 或运行时行为。
- 不修改用户级/系统级配置。

## 验收

- `./test/context-checkpoint-test.sh` 必须能从 `--stdout` 输出中读到完整发布窗口恢复规则。
- `scripts/update-context-checkpoint.sh --stdout` 不得把发布窗口降级为只检查满 7 天。
- 后续上下文压缩、模型切换或 tmux/SSH 恢复时，恢复步骤能提醒负责人：满 5 天优先评审，满 7 天必须正式发布或落盘 HOLD。

