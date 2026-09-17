# Git Stash 保存目标上下文验收报告（2026-09-17）

## 结论

状态：本地实现完成，等待本地/远端门禁验证。

本轮继续补强 Git 工作台 Stash 纵向体验：此前“应用但保留”和“删除这一条”已经在确认页展示
`host:port · path` 与当前分支/游离 HEAD；主动走查发现“保存 Stash”仍只展示改动数量。手机端多服务器、
多工作区和 AI 修改复盘场景下，用户保存 WIP 前也必须知道当前对象边界。

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 增值服务分类 | 处理结论 |
| --- | --- | --- | --- | --- |
| 保存 Stash 确认页只展示改动数量，不展示当前服务器、目录和分支。 | 负责人主动走查 `GitDiffActivity#createStashDialog()` 与上一轮 stash apply/drop 报告。 | 远程 SSH 开发 → Git 工作台 → 保存 Stash。 | Git 可视化、远程工作区、移动端工作流效率。 | 本轮处理。 |
| Stash apply/drop 已补目标上下文，但保存、提交、推送、拉取等 Git 写操作仍需持续检查对象边界是否一致。 | QA 复核 Git 工作台写操作。 | Git 工作台 → 写操作确认。 | Git 可视化、安全反馈。 | 本轮只处理保存 Stash；其他操作按证据轮换。 |
| AI CLI 本地记录重复打开已补确认，但后续仍需检查“运行项目任务/快捷指令”是否在执行前展示同等级目标上下文。 | 产品复盘近期 AI CLI 切片。 | AI CLI 后运行测试/任务。 | AI CLI、上下文工具箱。 | 暂缓；当前优先关闭 Git Stash 同类缺口。 |

## 增值服务准入

- 属于 TermuxPro 增值服务：Git 可视化管理、远程工作区、移动端工作流效率。
- 不是 Termux 原始终端、PTY、包管理、本地 shell、基础会话或基础快捷键能力重做。
- 不改变远端 Git 命令；仅在写操作确认前补充用户判断所需上下文。

## 变更范围

- `GitDiffActivity#createStashDialog()`
  - 读取当前 `ConnectionTarget`。
  - 保存 Stash 确认文案新增当前目标与 HEAD 摘要。
- 中英文资源：
  - `git_workbench_stash_message` 新增目标和 HEAD 占位。
- `GitDiffActivityTest#stashDialogsExplainSafeCreateApplyAndDropRules()`
  - 断言保存 Stash 弹窗包含 `host:port · path`、当前分支和“不提交、不推送”边界。

## 非目标

- 不新增 `git stash pop`。
- 不新增 `git stash clear`。
- 不改变 `git stash push -u -m ...` 的安全命令。
- 不新增提交、推送、拉取、重置、变基或远端分支写操作。
- 不研究或重做 Termux 原始终端/PTY/本地 shell 能力。

## 验收标准

1. 有可保存改动时，保存 Stash 确认页展示改动数量、已暂存/未暂存数量、目标和当前 HEAD。
2. 保存确认页继续明确“不提交、不推送”。
3. 非法 stash 说明仍在本地输入框阻止，不执行远端命令。
4. apply/drop 的既有目标上下文断言继续通过。
5. 中英文字符串 key 保持一致。

## 验证

- 通过：`./test/android-string-resource-parity-test.sh`
- 通过：`git diff --check`
- 未完成：`source scripts/resolve-jdk17.sh && timeout 120 ./gradlew --offline --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest.stashDialogsExplainSafeCreateApplyAndDropRules`
  - 失败阶段：Gradle 配置阶段，未进入测试断言。
  - 失败原因：本机项目级 SDK 缺少 `ndk;27.0.12077973`，Gradle 报告
    `NDK not configured. Download it with SDK manager. Preferred NDK version is '27.0.12077973'.`
  - 后续门禁：以 GitHub CI 的 Runner NDK 环境完成 Robolectric 复验；若 CI 失败，本切片不得合入 `dev`。
- 部分通过：`timeout 210 ./scripts/pre-push-smoke.sh`
  - 已通过：Skills 校验、版本元数据、workflow 策略、发布通知格式、发布窗口守卫、GitHub 噪声审计、
    GitHub CLI 封装、check suite 诊断、上下文检查点、长期 Goal 生命周期、缓存清理、Android SDK
    引导脚本、字符串资源和资源守卫。
  - 未完成：Gradle 阶段 210 秒内无进一步输出，被超时保护终止；未发现残留 Gradle/SDK 下载进程。
  - 后续门禁：推送后等待 GitHub CI；若 CI 失败，本切片不得合入 `dev`。

## 复盘问题

- 本轮减少的真实负担：用户保存 WIP 前不用返回概览或靠记忆确认当前远程仓库。
- 新增控件/流程噪声：无新增按钮或页面，只在原有确认页补充一行目标和一行 HEAD。
- Termux 原始能力影响：不触碰原始终端、PTY、包管理、本地会话或基础快捷键。
- 下一轮建议检查：继续检查项目任务/快捷指令执行确认是否展示同等级目标上下文。
