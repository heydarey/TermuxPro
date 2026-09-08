# AI CLI 会话选择操作优先审计

## 选题与问题证据

- 增值服务分类：AI CLI、远程工作区、移动端交互体验。
- 审计来源：GitHub Emulator UI artifact `termuxpro-emulator-ui-241`（run `34200643606`）。
- 默认和 200% 字体截图均显示原生 `AlertDialog.setItems()` 将“新建会话 / 选择历史会话”排在长目标与
  风险说明之后；在可视首屏中只剩“取消”，用户必须猜测可滚动性才能看到真正操作。这不符合 Claude 共享
  账号场景中“先明确选择新建或历史，再决定上下文”的安全目标。

## 交互调整

1. 将两个决策项改为自定义弹窗内的可见按钮，并放在详细上下文之前：新建会话（安全默认）与选择历史会话
   （不自动恢复）。按钮直接展示将执行的 `claude` / `claude --resume` / `codex` / `codex resume`。
2. 目标、共享账号风险及终端当前上下文说明保留在按钮之后，可滚动阅读；用户不再需要先滚过说明才能发现
   唯一的有效操作。
3. 终端工具箱与工作区首页共用同一弹窗构造器，避免一个入口修好、另一个入口继续隐藏决策项。
4. 未改变命令策略：Claude 历史入口仍只启动 `claude --resume` 原生选择器，Codex 历史入口仍为
   `codex resume`；TermuxPro 不解析、列出或自动进入最近 AI 历史。

## 本地回归

- `:app:testDebugUnitTest --tests com.termux.app.AiCliLaunchCommandTest --tests
  com.termux.app.WorkspaceActivitySmokeTest --rerun-tasks`：通过。
  - `AiCliLaunchCommandTest`：7 项，0 failure / 0 error；锁定真实 CLI 命令与按钮文案。
  - `WorkspaceActivitySmokeTest`：15 项，0 failure / 0 error；验证首页启动弹窗中两个可点击决策项和完整
    目标/风险上下文均存在。
- `test/ai-launch-decision-copy-test.sh`、`test/dialog-readable-style-test.sh`、`scripts/validate-skills.sh`、
  `git diff --check`：均通过。

## 待远端验收

- 研发 PR CI、360dp 深色默认/200% 字体 Emulator UI、自动合并与 dev 收尾 CI 通过后，必须复查截图中两个
  会话决策按钮均位于详细说明之前且可见。
