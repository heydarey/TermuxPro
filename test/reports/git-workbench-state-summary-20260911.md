# Git 工作台首屏状态摘要

## 背景

手机上进入 Git 工作台的高频问题不是“有没有按钮”，而是用户需要快速判断：我在哪个分支、有没有改动、
哪些已暂存、是否落后/领先远端、下一步该审查、提交、拉取还是推送。此前这些信息分散在多行文本和工具箱
按钮中，开发者仍需要自行拼接判断。

## 主动发现

| 候选观察 | 影响旅程 | 证据 | 分类 | 优先级 | 处理 |
|---|---|---|---|---|---|
| Git 首屏缺少一句话状态结论，用户要分别读分支、改动、暂存和同步 | 手机上审查 AI 修改前的 Git 判断 | `activity_git_diff.xml` 仅有分散的 `changes/index/sync/next_step` | Git 可视化、移动端工作流效率 | P1 | 本轮修复 |
| 推荐主按钮已有，但没有和仓库状态绑定成完整语义 | AI CLI 结束后判断下一步 | `bindPrimaryAction()` 和 `nextStepGuidance()` 各自输出，缺少首屏摘要 | Git 可视化 | P1 | 本轮修复 |
| 再新增更多 Git 按钮会增加页面复杂度 | 360dp / 200% 字体下扫读 Git 工具箱 | Git 工具箱已有分支、同步、改动、历史等按钮组 | UI/UX | P2 | 暂缓，优先摘要而非堆功能 |

## 改动

- Git 工作台首屏新增状态摘要卡，合并展示：
  - 工作树是否干净，或改动文件/已暂存/未暂存数量。
  - 上游是否配置，以及同步/领先/落后/分叉状态。
  - 当前推荐动作。
- 摘要同步设置 `contentDescription`，TalkBack 不需要逐行拼装状态。
- 不新增 Git 危险能力，不做 force push、merge、rebase 或丢弃文件。

## 非目标

- 不重写 Git 操作实现。
- 不替用户猜 upstream。
- 不在当前 shell 注入 git 命令。
- 不改变 Termux 原始终端、PTY 或滚动行为。

## 验收重点

- 首屏能一眼看到状态结论和推荐动作。
- 已暂存/未暂存/领先/落后信息仍保留原有明细。
- 所有危险操作仍走显式确认。

## 本地验证

- `./scripts/resource-guard.sh normal`：通过；当前环境为远程/共享，KVM 不可用，不启动本机模拟器。
- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./test/version-metadata-test.sh`：通过。
- `./test/workflow-trigger-policy-test.sh`：通过。
- `./test/release-notification-format-test.sh`：通过。
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./scripts/pre-push-smoke.sh origin/dev`：脚本类门禁通过；
  Gradle/Robolectric 阶段在本机 180 秒超时，无代码失败输出。Android 编译、Robolectric 和界面截图
  继续交由 GitHub CI 与 Emulator UI 门禁验证。
