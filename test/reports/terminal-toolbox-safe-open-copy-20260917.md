# 终端工具箱常用工作流安全打开文案（2026-09-17）

## 结论

通过。终端工具箱首屏高频入口继续保持 AI CLI 会话中心、Git 工作台、tmux 管理和快捷指令置顶；
本轮把分组标题从“推荐操作”升级为“常用工作流（安全打开）”，并在 Git、tmux、快捷指令的说明中
明确它们会打开结构化增值页面，不会把命令输入当前终端或 AI TUI，也不会自动进入共享 tmux 会话。

该切片只调整 TermuxPro 增值工具箱的可见文案和无障碍描述，不改变原始终端输入、PTY、滚动、快捷键、
shell、本地会话或包管理行为。

## 主动发现记录

| 候选观察 | 来源 | 影响旅程 | 增值分类 | 处理结论 |
| --- | --- | --- | --- | --- |
| “推荐操作”能说明优先级，但不能说明这些入口是安全打开页面，不会污染当前 Claude/Codex 终端。 | 负责人走查 `TerminalProjectToolsMenu` 菜单结构。 | SSH 进入远程项目后，从终端现场打开 AI/Git/tmux/快捷指令。 | AI CLI、Git/tmux 可视化、上下文工具箱 | 本轮处理。 |
| Git 工作台说明只说查看分支/改动，未在入口层说明不会把 `git` 命令输入当前终端。 | UI/UX 文案审计。 | 正在 Claude/Codex TUI 中工作时打开 Git。 | Git 可视化、移动端工作流效率 | 本轮处理。 |
| tmux 管理说明没有前置“不会自动进入共享会话”，共享账号场景下用户需要再次推断风险。 | 安全/共享账号场景复核。 | 查看或进入 tmux 会话。 | tmux 可视化、远程工作区 | 本轮处理。 |
| 快捷指令入口只说不会自动执行命令，但未说明不会输入当前终端或 AI TUI。 | 产品闭环复核。 | 自定义命令管理与执行前确认。 | 自定义快捷指令、上下文工具箱 | 本轮处理。 |

## 变更范围

- 中文：
  - `推荐操作` → `常用工作流（安全打开）`
  - Git 工作台描述增加“不会把 git 命令输入当前终端”
  - tmux 管理描述增加“不会自动进入共享会话”
  - 快捷指令描述增加“进入页面不会执行命令，也不会输入当前终端或 AI TUI”
- 英文：
  - `Recommended actions` → `Common workflows (safe open)`
  - 同步补充 Git/tmux/quick commands 的 safe-open 边界。
- 测试：
  - `TerminalProjectToolsMenuTest#toolboxUsesDeveloperTaskGroupsBeforeRawActions`
    覆盖分组标题与三条高频入口描述。

## 非目标

- 不新增或删除工具箱入口。
- 不改变菜单顺序、点击处理或命令执行路径。
- 不向当前终端自动输入 Git/tmux/快捷指令命令。
- 不改变 Termux 原始终端、PTY、本地 shell、基础快捷键、滚动或会话行为。

## 验收

- 终端工具箱首屏第一组明确是“常用工作流（安全打开）”。
- AI、Git、tmux、快捷指令仍位于首屏高频入口区。
- Git/tmux/快捷指令说明能在 TalkBack/无障碍描述中读出安全边界。
- `TerminalProjectToolsMenuTest` 锁定该文案，防止回退到模糊推荐语。

## 验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `git diff --check`：通过。
- `timeout 300 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.TerminalProjectToolsMenuTest`：
  本地 300 秒无测试输出后 timeout 退出码 124，未作为通过证据；本轮保留到 GitHub CI 运行完整 Android
  单元测试与 Lint/APK 门禁。
