# 项目任务空态快捷指令引导验收记录（2026-09-17）

## 主动发现

| 观察 | 来源 | 影响旅程 | 增值服务分类 | 结论 |
| --- | --- | --- | --- | --- |
| 项目任务页识别不到内置任务时，只提示“未找到支持的项目任务”，用户不知道下一步如何把公司脚本沉淀到手机端。 | 负责人代码走查 `ProjectTasksActivity.showEmpty()` | 手机 SSH 远程开发中从工具箱打开“项目任务 / 测试”后继续执行测试或构建 | 自定义快捷指令、远程工作区、移动端工作流效率 | 本轮推进 |
| 直接在空态展示所有模板会增加项目任务页复杂度。 | UI/UX 取舍 | 360dp、小屏和 200% 字体 | 移动端交互体验 | 只新增一个按需入口，不常驻展开模板列表 |
| 连接失败、工作区无效和识别不到任务是不同状态，不能都引导用户创建快捷指令。 | QA/产品复核 | 错误恢复与空态恢复 | 远程工作区、移动端交互体验 | 错误态仍保留“返回工作区”，仅空态显示“打开快捷指令与模板” |

## 改动范围

- `ProjectTasksActivity` 在空态显示“打开快捷指令与模板”，跳转 `CustomCommandsActivity`。
- 空态文案说明：如果项目使用公司脚本或自定义命令，可创建当前工作区快捷指令模板；此处尚未执行远端命令。
- 错误态、无效工作区、任务会话查看、确认运行和远端命令构造均不变。

## 非目标

- 不新增项目类型探测规则。
- 不自动生成或执行任何远端命令。
- 不改变 Termux 原始终端、PTY、本地 shell 或基础会话能力。

## 验收

- `ProjectTasksActivityTest.emptyProjectTasksOfferCustomCommandTemplatesWithoutRunningRemoteCommands`
  验证空态出现快捷指令入口、隐藏返回工作区恢复按钮，并跳转到 `CustomCommandsActivity`。
- `RemoteToolRecoveryTest.projectTasksInvalidWorkspaceShowsReadableRecoveryState` 继续验证无效工作区仍显示返回工作区恢复路径。

由于本机 KVM 不可用，本轮不声明模拟器验收；Android 运行时与截图回归继续交给 GitHub CI / Emulator UI 门禁。
