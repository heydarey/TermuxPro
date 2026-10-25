# Git Stash 入口数量与安全语义验收

日期：2026-09-17

## 增值服务准入

- 分类：tmux/Git 可视化、移动端工作流效率。
- 增值层位置：帮助手机用户在 Git 工作台首屏判断当前仓库是否存在临时保存的 stash，并理解后续操作边界。
- 非 Termux 原始能力：不修改终端、PTY、本地 shell、基础会话或 Git 命令语义；只调整 TermuxPro Git 可视化入口文案和辅助语义。

## 主动发现候选观察

1. `Stash 列表`按钮在有记录时仍是静态文案，用户需要点开弹窗才知道数量。
   - 影响旅程：手机上切换任务前后，用户难以从首屏判断是否已有临时保存项。
   - 推进理由：stash 是 Git 工作台增值功能，应把“有没有、多少条”前置到入口层。
2. stash apply/drop 已在确认弹窗解释“应用但保留”和“只删除单条”，但入口层没有说明安全边界。
   - 影响旅程：用户可能误以为入口会直接 `pop`、清空全部，或触碰远端仓库。
   - 推进理由：危险/半危险 Git 操作应在点击前给出可感知边界，减少误触焦虑。
3. 无记录状态只靠按钮禁用和透明度表达，不利于无障碍和自动化验收。
   - 影响旅程：TalkBack/测试只能读到 `Stash 列表`，无法知道“当前仓库没有 stash 记录”。
   - 推进理由：给禁用态补充 contentDescription，符合入口可解释和可验证要求。

## 实现范围

- 无 stash 时，按钮仍显示 `Stash 列表`，禁用，并设置“当前仓库没有 stash 记录”的辅助描述。
- 有 stash 时，按钮显示 `N 条 Stash` / `N stashes`。
- 有 stash 时，辅助描述说明可查看当前仓库 stash，可应用但保留，或只删除选中单条 stash。

## 非目标

- 不新增 `git stash pop`。
- 不新增 `git stash clear`。
- 不自动应用或删除 stash。
- 不触碰远端仓库、分支、提交或工作树内容。

## 验收重点

- 首屏能直接判断 stash 数量。
- 入口辅助语义能证明不会批量清空、不使用 pop、不碰远端。
- 原始终端和手动 Git 命令能力不受影响。

## 本地验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest`：本地远程机启动 Gradle 后超过 60 秒无输出，按共享资源策略手动中断；未进入可证明的测试失败状态，Android/Robolectric 结果以 GitHub CI 为准。
