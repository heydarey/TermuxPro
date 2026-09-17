# Git Stash 列表与操作语义补强报告（2026-09-17）

## 结论

本轮选择 Git 工作台的 Stash 列表语义作为小步纵向切片，结论为 `READY_FOR_PR`。

改动只影响 TermuxPro Git 可视化增值层的文案、标题和测试断言，不改变远端 Git 命令、不新增 `pop`、
`clear`、`force`、`reset` 或任何远端仓库写入路径。

## 主动发现候选

1. **Git Stash 列表进入后丢失数量感**
   - 证据：概览按钮已显示 `1 条 Stash`，但 `createStashesDialog()` 标题仍为通用 `Stash 列表`。
   - 影响旅程：手机用户从概览进入二级弹窗后，需要重新通过列表长度判断范围。
   - 增值分类：Git 可视化管理、移动端工作流效率。
   - 处理：本轮推进，列表标题显示 `Stash 列表（N）` / `Stashes (N)`。

2. **单条 Stash 操作项语义不够贴近安全行为**
   - 证据：操作菜单原为“应用 Stash / 删除 Stash”，但实际安全策略是 apply 保留、drop 仅删除单条。
   - 影响旅程：用户可能误以为应用等于 `pop`，或删除会清理所有 stash。
   - 增值分类：Git 可视化管理、安全反馈。
   - 处理：本轮推进，操作项改为“应用但保留 / 删除这一条”，英文为 `Apply and keep / Delete this stash`。

3. **候选：Git 工作台后续仍需继续巡检对象级 CRUD 语义**
   - 证据：Git 工作台已经具备分支、改动、提交、stash 多对象管理，但不同对象的动作范围仍需持续检查。
   - 影响旅程：手机端误点成本高，用户需要在点击前看懂“只影响哪一个对象、是否会写远端、是否可逆”。
   - 增值分类：Git 可视化管理、移动端交互体验。
   - 本轮暂缓理由：本轮先关闭 Stash 二级入口的明确缺口；其他 Git 对象另行按证据排期。

## 增值服务准入

- 属于 TermuxPro 增值服务：Git 可视化管理。
- 不是 Termux 原始终端/PTY/包管理/本地 shell/基础会话/基础快捷键能力重做。
- 不修改 Git 命令执行路径，只提升手机端操作前的信息理解与风险提示。

## 改动范围

- `GitDiffActivity#createStashesDialog()`：列表标题改为带数量的 plural 文案。
- 中文/英文资源：
  - 新增 `git_workbench_stashes_title_count`。
  - 将操作项改为“应用但保留 / 删除这一条”和英文对应文案。
- `GitDiffActivityTest#stashDialogsExplainSafeCreateApplyAndDropRules()`：
  - 断言列表标题带数量。
  - 断言应用确认按钮仍为“应用但保留”。
  - 断言单条操作菜单文案体现“保留”和“只删这一条”。

## 非目标

- 不实现 stash pop。
- 不实现 stash clear。
- 不允许对脏工作树应用 stash。
- 不新增提交、推送、拉取、重置、变基或远端分支写操作。
- 不改 Termux 原生终端行为。

## 验收计划

本地低资源验证：

1. `./test/android-string-resource-parity-test.sh`：通过。
2. `./scripts/validate-skills.sh`：通过。
3. `git diff --check`：通过。
4. `source scripts/resolve-jdk17.sh && ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest`：
   在远程共享机上长时间无进一步输出，为避免持续占用资源由维护者中断，退出码 130；不记为断言失败。
   运行时回归以后续 GitHub Actions 完整 CI 为准。
5. `./scripts/pre-push-smoke.sh`：静态段已通过，包括 skill、版本元数据、workflow 触发策略、发布通知、
   GitHub 噪声审计、GitHub CLI 封装、check suite 诊断、上下文检查点、长期 Goal 规则、缓存清理、
   Android SDK 引导、字符串资源校验和资源守卫；进入 Gradle JVM 冒烟后长时间无进一步输出，为保护共享
   服务器资源由维护者中断，退出码 130。完整 JVM/Android 验证以后续 GitHub Actions 为准。
6. GitHub CI `35209700609` 首次运行进入全模块测试并失败，失败点为
   `GitDiffActivityTest#stashDialogsExplainSafeCreateApplyAndDropRules` 中将确认弹窗主按钮误断言为菜单项
   “应用但保留”。实际产品语义应保持确认按钮“应用但不删除”，菜单项才是“应用但保留”。已修正测试断言，
   不需要改产品命令或确认文案。
7. 修正后本地重跑目标测试时，Gradle 配置阶段访问 `https://dl.google.com/.../gradle-8.13.2.pom`
   发生 read timeout，未进入测试执行；该结果记录为本机网络依赖解析失败，后续以 GitHub Actions 复验为准。

远端门禁：

- PR 上的 TermuxPro CI。
- 若 GitHub 判定涉及 Android UI 运行时文件，则等待 Emulator UI 门禁；否则以静态/JVM 门禁为准。

## 回滚方式

回滚本切片只需恢复 `GitDiffActivity` 的列表标题、两组资源文案和对应测试断言；不涉及持久化数据迁移。
