# Git Stash 操作目标上下文验收报告（2026-09-17）

## 结论

状态：本地实现与静态门禁完成，目标 Robolectric 需由 GitHub CI 完整验证。

本轮补强 Git 工作台 stash 操作确认页：应用但保留、删除单条 stash 前，必须展示当前远端目标
`host:port · path` 和当前分支/游离 HEAD。这样多服务器、多工作区和 AI 修改复盘场景下，用户在手机上
确认前不需要回忆当前页面来自哪个远程仓库。

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 增值服务分类 | 处理结论 |
| --- | --- | --- | --- | --- |
| stash apply/drop 确认页只展示 stash ref 和说明，没有当前仓库目标与分支。 | 负责人主动走查 `GitDiffActivity.confirmApplyStash/confirmDropStash`。 | 远程 SSH 开发 → Git 工作台 → Stash 列表 → 应用/删除。 | Git 可视化、远程工作区、移动端工作流效率。 | 本轮处理。 |
| AI 会话中心本地记录已补清空范围，但后续仍需继续检查所有删除/清空操作是否显示对象边界。 | 负责人复核近期 AI 历史切片。 | AI CLI 会话中心 → 本地记录管理。 | AI CLI、移动端交互体验。 | 暂缓；已有最新切片保护，后续轮换检查。 |
| tmux 管理页支持进入/新建/重命名/停止，但仍需继续扫查共享账号归属说明在 200% 字体下是否足够可见。 | backlog 与用户共享 Claude/tmux 风险反馈。 | tmux 管理 → 进入或管理指定会话。 | tmux 可视化、AI CLI 安全。 | 暂缓；当前优先修 Git 高风险确认。 |

## 变更范围

- `GitDiffActivity`
  - `confirmApplyStash()` 在确认文案中加入目标与当前 HEAD。
  - `confirmDropStash()` 在危险确认文案中加入目标与当前 HEAD。
  - 新增 `gitTargetSummary()` 和 `gitHeadSummary()`，复用现有 Git 工作台目标/分支文案。
- 中英文资源：
  - `git_workbench_stash_apply_message`
  - `git_workbench_stash_drop_message`
- `GitDiffActivityTest`
  - 断言 stash apply/drop 弹窗都包含 `host:port · path` 与当前分支。

## 非目标

- 不新增 `git stash pop`。
- 不新增 `git stash clear`。
- 不改变远端命令、Shell 转义和安全退出码。
- 不研究或重做 Termux 原始终端/PTY/本地 shell 能力。

## 验收标准

1. 工作树有未提交改动时，应用 stash 仍被阻止。
2. 干净工作树应用 stash 时，确认弹窗展示 stash ref、说明、目标和当前分支，并明确 stash 会保留。
3. 删除 stash 时，危险确认弹窗展示 stash ref、说明、目标和当前分支，并明确只删除这一条，不触碰工作树和远端仓库。
4. 中英文字符串 key 保持一致。

## 本地验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `source scripts/resolve-jdk17.sh && ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest.stashDialogsExplainSafeCreateApplyAndDropRules`：
  未进入测试阶段；Gradle 配置阶段读取 `https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/8.13.2/gradle-8.13.2.pom`
  超时，错误为 `Read timed out`。
- `./scripts/pre-push-smoke.sh`：静态门禁全部通过；进入自动补跑 Gradle 阶段后在共享远程机连续约 90 秒无输出，
  按资源守卫中断，退出码 130。完整 Android/Robolectric 验证交由 GitHub CI。

## 复盘

- 减少的真实负担：手机用户不需要切回概览页确认当前服务器、路径和分支，再决定是否应用/删除 stash。
- 新增控件/流程噪声：无新增页面或按钮，只在危险/写操作确认页补充上下文。
- Termux 原始能力影响：不触碰原始终端、PTY、包管理、本地会话或基础快捷键。
- 下一轮建议检查：继续轮换检查 Git/tmux/AI CLI 中所有“删除、清空、应用、进入、运行”操作是否在确认前显示对象边界。
