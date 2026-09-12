# AI CLI 重复历史来源确认回归报告（2026-09-12）

## 主动发现记录

| 候选 | 来源 | 优先级 | 增值服务分类 | 影响旅程 | 结论 |
| --- | --- | --- | --- | --- | --- |
| 重复历史记录确认页缺少原启动时间 | 负责人继续走查 PR #339 后的 AI CLI 会话中心 | P1 | AI CLI、远程工作区、移动端交互体验 | 同一工作区多次启动 Codex/Claude 历史选择后，“重复启动”只显示目标和命令，用户无法确认重复的是哪一次本地记录 | 本轮处理：重复历史模式确认页展示“原启动时间” |
| 普通“选择历史”确认页不展示启动时间 | 设计复核 | P3 | AI CLI | 普通入口不是重复记录，没有原始时间上下文；展示时间反而会制造无意义信息 | 不处理 |
| 重复新建会话也增加确认 | 产品取舍复核 | P2 | AI CLI、移动端工作流效率 | 新建会话是安全默认和高频路径；额外确认会降低手机端启动效率 | 暂缓：仅历史选择模式需要最后确认 |

## 本轮改动

- 从“重复上次”或“查看全部记录 → 重复启动”进入历史选择模式时，确认页展示被重复记录的原启动时间。
- 普通“选择 Claude/Codex 历史”仍展示目标、路径、命令和安全边界。
- 新建会话保持一键启动。

## 非目标

- 不读取 Claude/Codex 远端历史列表。
- 不解析或自动选择任何历史会话。
- 不改变 Termux 原始终端、PTY、滚动和本地 shell 行为。

## 回归范围

- `AiCliSessionCenterActivityTest.repeatLastHistoryLaunchRequiresTargetConfirmation`
- `AiCliSessionCenterActivityTest.repeatsSelectedAiLaunchRecordFromManageDialog`
- `AiCliSessionCenterActivityTest.aiActionsOpenIndependentSshOnlyTerminalWithoutAutoTmux`
