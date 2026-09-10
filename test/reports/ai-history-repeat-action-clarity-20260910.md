# AI 最近启动重复操作可理解性验收记录

## 结论

本轮选择优化 AI CLI 会话中心的“最近 AI 启动”主操作：当当前工作区存在启动记录时，重复按钮直接显示
将要重复的工具和模式，例如“重复：Codex CLI · 历史选择”；工作区无效或没有记录时仍显示“重复上次”并
保持禁用。

## 主动发现候选

| 候选 | 证据 | 增值服务分类 | 结论 |
|---|---|---|---|
| AI 最近启动重复按钮语义不足 | `AiCliSessionCenterActivity.bindHistory()` 里下一步文案会说明最新工具和模式，但按钮本身始终是“重复上次”；手机滚动后容易丢失上下文。 | AI CLI、移动端工作流效率 | 本轮处理，属于高频 SSH + Claude/Codex 恢复路径。 |
| Git 工作台首屏长目标路径可能影响扫读 | backlog 已记录“后续观察 Git 首屏中长目标路径的阅读节奏”。 | Git 可视化、移动端交互体验 | 暂缓，已有较完整 Git 操作闭环，本轮优先 AI 高频入口。 |
| 终端工具箱推荐操作仍可能过多 | backlog 已记录工具箱置顶推荐分组，后续需要继续截图审计。 | 上下文工具箱、移动端交互体验 | 暂缓，避免在同一轮扩大菜单信息架构改动。 |

## 验收重点

- 最近记录存在时，重复按钮必须展示具体工具和模式，不依赖用户记住上方说明。
- 删除按钮继续展示具体目标，清空仍需二次确认。
- 只操作 TermuxPro 本地启动记录；不读取、不修改 Claude/Codex 私有历史，不进入 tmux，不影响原始终端。
- 无有效工作区或无记录时，重复按钮恢复通用禁用态，避免显示过期目标。

## 回归范围

- `AiCliSessionCenterActivityTest.repeatsDeletesAndClearsOnlyCurrentWorkspaceLaunchHistory`
- `AiCliSessionCenterActivity.bindHistory()`
- 中文与英文资源字符串

