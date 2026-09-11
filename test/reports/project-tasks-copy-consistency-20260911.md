# 项目任务 / 测试入口文案一致性验收记录（2026-09-11）

## 增值服务准入

- 分类：远程工作区、移动端工作流效率、上下文工具箱。
- 不是 Termux 原始能力重做：本轮不修改 PTY、shell、会话、包管理或终端输入行为，只收敛
  TermuxPro 增值入口的信息架构。

## 主动发现候选

| 候选观察 | 证据 | 影响旅程 | 优先级 | 处理 |
|---|---|---|---|---|
| 同一能力在 AI 中心、工作区/终端工具箱、Git 工作台分别显示“运行项目任务”“项目任务”“运行项目任务 / 测试” | `strings.xml` 与 `TerminalProjectToolsMenuTest` | AI 改完代码后，用户需要判断哪个入口才是测试/构建验收入口，增加扫读成本 | P1 | 本轮统一为“项目任务 / 测试” |
| 项目任务页标题只叫“项目任务”，说明里只强调“读取项目描述信息”，没有把测试/构建/开发任务作为核心价值说清楚 | `activity_project_tasks.xml` 使用 `project_tasks_title` 和 `project_tasks_description` | 用户进入页面后仍可能不确定这里是否适合跑验收测试，尤其从 Git 工作台跳转过来时 | P1 | 本轮更新标题与说明 |
| 部分入口仍缺少“会先进入确认页，不会向当前 shell 直接发命令”的无障碍描述 | 终端工具箱菜单项目前只有标题，缺少 per-item contentDescription | 使用辅助功能或只看菜单标题时，无法完全判断安全边界 | P2 | 暂缓：需要统一菜单描述能力，后续与工具箱无障碍切片合并 |

## 本轮改动

- `workspace_project_tasks_action`、`ai_cli_center_open_project_tasks`、`git_workbench_project_tasks`
  统一为“项目任务 / 测试”。
- `project_tasks_title` 统一为“项目任务 / 测试”。
- `project_tasks_description` 明确说明会识别测试、构建和开发任务，并在启动前展示目标与完整命令。

## 回归用例

- `TerminalProjectToolsMenuTest.toolboxUsesDeveloperTaskGroupsBeforeRawActions`：
  终端工具箱的项目入口应显示“项目任务 / 测试”。
- `ProjectTasksActivityTest.pageCopyFramesTasksAsProjectTasksAndTests`：
  项目任务页标题和说明必须传达测试/构建/开发任务价值。
- `GitDiffActivityTest.overviewOpensProjectTasksForSameWorkspaceAfterGitReview`：
  Git 工作台中的跨页验收入口保持同一文案，并继续进入项目任务页。

## 本机验证

- `git diff --check`：通过。
- `rg` 静态路径检查：确认中文/英文资源、测试断言、backlog 和本报告已同步。
- `./scripts/pre-push-smoke.sh`：脚本类门禁已通过，包括 Skill 校验、版本元数据、workflow 触发策略、
  发布通知格式、GitHub 噪声审计脚本、GitHub CLI 封装、check suite 诊断、上下文检查点、SDK 引导脚本
  与资源守卫；进入 Gradle 冒烟后长时间无输出，为避免占用远程共享机器已中断。
- 首次云端 CI 暴露 `ProjectTasksActivityTest` 引用了不存在的 `project_tasks_title` /
  `project_tasks_description` id。修复方式不是删除断言，而是为标题和说明 TextView 增加稳定 id，让页面
  语义可被持续回归。
- 完整 Android 单测、lint、构建和模拟器 UI 验收交由 GitHub Actions Runner。

## 验收结论

这轮减少的是移动端跨页面识别负担：用户从 AI 中心、Git 工作台、工作区或终端工具箱看到的都是同一个
“项目任务 / 测试”能力，不需要猜“项目任务”和“运行测试”是否是两件事。所有入口仍只进入结构化页面，
任务执行前继续要求用户确认目标与命令，不改变原始 Termux 终端行为。
