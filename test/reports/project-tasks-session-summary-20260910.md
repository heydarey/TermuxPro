# 项目任务会话摘要验收记录

## 主动发现

| 观察 | 来源 | 影响旅程 | 增值服务分类 | 优先级 | 结论 |
|---|---|---|---|---|---|
| 项目任务页已经能启动 `mobile-task-*` 持久会话，但用户回到页面后只能看到任务列表，不知道当前工作区是否已有后台任务 | 负责人走查“AI 完成后 → 运行项目任务 → 回看任务状态”旅程 | 手机用户运行测试/构建后，需要记住另一个 tmux 管理入口，无法在原页面判断是否要恢复任务 | 远程工作区、tmux 可视化、移动端工作流效率 | P1 | 本轮推进 |
| 共享账号场景下，任务会话摘要必须明确只统计当前工作区，其他工作区任务不能被自动进入或误认为可管理 | 用户此前反馈 Claude Code 共享账号和 tmux 自动进入风险 | 防止误入其他项目/其他用户任务会话 | tmux 可视化、安全边界 | P1 | 本轮推进 |
| 当前变更不能读取任务输出或 pane 内容，否则会扩大隐私和性能风险 | 架构安全审查 | 保持只读、低成本、脱敏元数据读取 | 安全与可维护性 | P1 | 本轮约束 |

## 变更

- 项目任务页新增“任务会话状态”卡片。
- 页面检测项目类型时，同时只读查询 TermuxPro 管理的 `mobile-task-*` tmux 元数据。
- 摘要展示：
  - 当前工作区项目任务会话数量；
  - 最近一个当前工作区任务会话名和连接状态；
  - 其他工作区任务数量，并明确不会自动进入或管理。
- 读取失败时明确提示不会自动进入任何 tmux，可手动点“查看任务会话”重试。

## 非目标

- 不读取 tmux pane 内容、命令输出或 Claude/Codex 历史。
- 不自动 attach、stop、rename 或写入任何已有会话。
- 不改变项目任务识别、任务启动命令、tmux ownership 校验和 Termux 原始终端能力。

## 验收

- `ProjectTasksActivityTest.taskSessionSummaryHighlightsOnlyCurrentWorkspaceTasks`
  - 当前工作区任务会话被摘要展示；
  - 其他工作区任务只显示数量，不显示为可恢复目标。
- `ProjectTasksActivityTest.taskSessionSummaryDoesNotAutoRecoverOtherWorkspaceTasks`
  - 只有其他工作区任务时，页面提示当前工作区暂无任务；
  - 明确不会自动进入或管理。
- 本地低资源验证通过：
  - `TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.ProjectTasksActivityTest`

## 回归面

- 项目任务列表仍由 `ProjectTaskDetector` 解析项目元数据。
- “查看任务会话”仍复用 `TaskSessionsActivity`，继续由归属校验控制 CRUD。
- 共享服务器上不启动本地 Android 模拟器；Android 运行时行为由 GitHub Runner Emulator UI 门禁覆盖。
