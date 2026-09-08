# 工作区与 tmux 大字体可读性审计

## 选题

- 增值服务分类：多工作区、tmux 可视化、移动端交互体验。
- 审计旅程：开发者在手机上确认当前远程项目，进入终端工具箱后查看共享服务器的 tmux 会话；在系统 200%
  字体下，仍应优先看到可执行操作，而不是重复的说明文字。
- 证据来源：先从 GitHub Emulator UI run `34198797402` 下载并检查 artifact
  `termuxpro-emulator-ui-240` 定位问题；修正后再从 run `34200643606` 下载 artifact
  `termuxpro-emulator-ui-241` 复查 360dp、深色、简体中文、默认和 200% 字体。

## 截图发现与取舍

1. 默认字体工作区页中，卡片标题和第一个管理按钮都写“服务器与项目”。按钮实际打开的是新建、复制、
   删除等对象操作，重复标签无法解释其行为，且会让用户误以为点击后才可编辑连接。
   本轮将按钮改为“更多工作区操作”；“编辑连接”“新建工作区”和主操作“打开远程终端”维持独立、可预期的
   语义。
2. 200% 字体 tmux 页中，完整共享归属说明会扩展为多行，首个会话被明显下推。大字体用户不应靠缩小字体
   换取空间，因此只在 font scale >= 1.5 时显示“仅当前工作区会话可管理；其余会话只可进入。”；完整规则
   仍作为内容描述提供给辅助技术，默认字体保留详细说明。
3. 本轮没有改变 tmux 的安全边界：当前工作区会话才可重命名或停止，其他工作区、其他使用者与归属未知
   会话仍只可显式进入；没有新增自动进入、默认恢复或远端写操作。

## 本地回归

- `:app:testDebugUnitTest --tests com.termux.app.TaskSessionsActivityTest --tests
  com.termux.app.WorkspaceActivitySmokeTest --rerun-tasks`：通过。
  - `TaskSessionsActivityTest`：4 项，0 failure / 0 error；覆盖会话归属、安全操作、创建校验和大字体提示选择，
    并锁定完整辅助技术描述。
  - `WorkspaceActivitySmokeTest`：15 项，0 failure / 0 error；覆盖首页渐进展示、工作区语义和连接配置行为。
- `git diff --check`：通过。
- `test/dialog-readable-style-test.sh`：通过。

## 远端验收证据

- PR #227 分支 CI `34200643688` 通过。
- PR #227 360dp 深色默认/200% 字体 Emulator UI `34200643606` 通过；复查截图确认“更多工作区操作”不再与
  卡片标题重复，200% 字体下 tmux 归属提示已收敛，首个会话卡片更靠近操作区。
- PR #227 已合入 `dev`（merge commit `81aea24b06805c8b3324c3ed29319592b1921b59`），合入后 dev 收尾 CI
  `34201266672` 通过；自动 PR 收尾 run `34200643638` 通过。本轮闭环完成。
