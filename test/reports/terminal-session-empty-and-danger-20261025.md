# 终端会话抽屉空态与关闭按钮危险色验收（2026-10-25）

## 主动发现候选

| 候选 | 来源 | 影响 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| 终端会话列表空态为一整块黑色区域，无“点新建开始”引导（UI 走查 terminal-navigation 截图实证）。 | UI 走查审计 #5 | 首次打开终端抽屉的用户面对黑屏不知所措 | 移动端工作流效率、原始能力兼容守护（ additive） | P1 | 本轮处理：ListView 挂 terminal_sessions_empty 空态视图（居中引导“点‘新建’开始第一个会话”），setEmptyView 自动切换可见性，有会话时零影响。 |
| “关闭”（停止当前会话）与“新建”等权重并排，误触距离为零；已有确认弹窗兜底但视觉无区分。 | UI 走查审计 #5 | 误点关闭杀掉远程任务的心理与实际成本 | 移动端工作流效率 | P1 | 本轮处理：关闭按钮文字用 tp_danger 危险色（沿用 GitDiff/TaskSessions 三处代码侧惯例），与“新建”视觉区分；确认弹窗层级保留。 |
| S7（AI 会话中心默认字体信息层级）与 ui-emulator 每日调度去噪仍为独立设计切片。 | 本轮复盘 | — | — | — | 暂缓维持：S7 需先真机走查确认实际阅读负担（backlog 已登记理由）；调度去噪涉及发布证据连续性口径。 |

## 本轮范围

- activity_termux.xml：terminal_sessions_list 挂 terminal_sessions_empty 空态 TextView（drawer tint、gone 默认）。
- TermuxActivity.setTermuxSessionsListView：setEmptyView 接线；close_session_button 文字色 tp_danger（代码侧，与既有三处惯例一致）。
- 新增 terminal_sessions_empty 中英资源；ui-affordance-batch-test.sh 追加 S8 防回退段（空态接线+危险色必须存在）。

## 非目标

- 不改变按钮尺寸权重与停止确认弹窗流程。
- 不为浅色主题引入新令牌（tp_danger 为产品深色主题既有唯一危险色）。

## 验收

- test/ui-affordance-batch-test.sh 通过（含新段）；中英资源 parity 通过。
- 布局/交互由 CI 模拟器 UI 门禁的终端导航场景截图复验。

## 结论

终端抽屉空态可自解释，停止会话动作有危险色视觉缓冲；“新增控件零流程噪声”复盘项达标（一个 gone 视图 + 一次文字色覆盖）。
