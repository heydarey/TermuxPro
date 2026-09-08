# Git 工作台下一步建议验收记录

## 背景

用户在手机上通过 SSH 使用 Codex CLI / Claude Code 后，最常见的下一步是确认 Git 状态、审查修改、提交、
拉取或推送。Git 工作台已有分支、改动、暂存、提交、stash、拉取和推送能力，但首屏仍需要用户自己从多
个状态文本和横向按钮中判断当前最安全的动作。

## 本轮范围

- 分类：Git 可视化、远程工作区、移动端工作流效率。
- 在 Git 工作台概览区新增“下一步建议”卡片。
- 根据当前仓库状态生成建议：
  - 游离 HEAD：先切回明确分支或新建分支。
  - 有未提交修改且没有暂存：先审查修改/按文件，或保存 stash 切任务。
  - 已有暂存修改：先确认暂存/未暂存范围，再提交。
  - 无上游：继续本地提交，远端同步前手动配置 upstream，不让 TermuxPro 猜目标分支。
  - 落后上游：工作树干净时只允许快进拉取，不自动 merge/rebase。
  - 领先上游：确认最近提交和目标后普通推送，不支持 force push。
  - 干净同步：可继续 AI 编码、运行项目任务或新建分支。

## 非目标

- 不新增提交、推送、拉取以外的新 Git 写操作。
- 不修改远程命令协议。
- 不重做 Termux 原始终端中的 Git 命令能力。

## 风险控制

- 建议卡只读取已有 `GitRepositoryOverview` 状态，不执行远端命令。
- 所有写操作仍沿用既有按钮启用/禁用和二次确认门禁。
- 无上游、游离 HEAD、落后/领先状态不伪造成可自动修复。

## 验收标准

1. Git 工作台首屏展示当前目标、分支、改动、同步状态和下一步建议。
2. 下一步建议必须覆盖游离 HEAD、未暂存、已暂存、无上游、落后、领先和干净同步。
3. 文案必须说明安全边界：不猜 upstream、不自动 merge/rebase、不 force push。
4. 360dp / 200% 字体下建议卡可随页面纵向滚动，不阻塞原有按钮。
5. 本轮不得影响 Termux 原始终端输入、滚动、PTY 或用户手工 Git 命令。

## 本地验证

- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./test/dialog-readable-style-test.sh`：通过。
- `. ./scripts/resolve-jdk17.sh && TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew lint :app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest --tests com.termux.app.GitRepositoryOverviewTest --tests com.termux.app.GitWorkbenchNavigationTest --tests com.termux.app.CustomLayoutsSmokeTest --max-workers=2`：通过。
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./scripts/pre-push-smoke.sh origin/dev`：通过。

## 结论

本轮切片通过本地产品、UI 和 QA 验收。它只增强 Git 工作台首屏的决策可读性，不改变远端命令协议，也不
影响 Termux 原始终端能力。
