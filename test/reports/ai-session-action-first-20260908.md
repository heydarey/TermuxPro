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

## 远端验收与结论

- PR #229 分支 CI `34202852410`、360dp 深色 Emulator UI `34202852403`、自动合并
  `34202852469` 与合入 dev 后的收尾 CI `34203377972` 均通过。
- Emulator UI artifact `termuxpro-emulator-ui-242` 已人工复核：默认字体与 200% 字体截图中，两个会话
  决策按钮均完整显示在目标与风险说明之前；没有裁切、重叠或只显示“取消”的退化。
- 结论：本切片通过。首次可见区域已先提供安全可理解的下一步，再按需呈现完整上下文；共享 Claude
  账号仍不会被静默续接，Termux 原始终端启动路径未被改变。
