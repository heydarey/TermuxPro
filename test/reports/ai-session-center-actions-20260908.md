# AI CLI 会话中心操作层级审计

## 发现

- 增值服务分类：AI CLI、远程工作区、移动端交互体验。
- AI 会话中心承载 Claude Code 与 Codex CLI 的高频启动路径；原页面把“新建（安全默认）”和“选择历史”
  压缩为同级窄按钮。中文 200% 字体下，两类决策既没有清晰主次，也缺乏足够的横向空间来稳定呈现
  “不自动恢复”这一安全边界。
- 页面此前没有进入模拟器截图清单，不能证明实际 Android 渲染中这四个关键操作是否可见。

## 调整

1. Claude 与 Codex 均改为纵向的全宽操作：绿色主按钮为“新建（安全默认）”，次按钮为“选择历史
   （不自动恢复）”。这使单手点击、大字体阅读和安全决策顺序一致。
2. 按钮文案直接说明恢复策略；命令预览、共享账号说明及现有 SSH-only 启动策略保持不变。
3. 将 `ai-cli-session-center` 纳入默认/200% 字体 Emulator UI 截图矩阵，并在仪器测试中锁定四个启动操作
   均可见。

## 本地验证

- `:app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest --rerun-tasks`：7 项，0 failure /
  0 error；覆盖空工作区、目标上下文、独立 SSH-only 启动、工具导航与主次操作文案。
- `test/ui-screenshot-manifest-test.sh`、`test/ai-launch-decision-copy-test.sh`、
  `test/dialog-readable-style-test.sh`、`scripts/validate-skills.sh` 与 `git diff --check`：通过。

## 待远端验收

- 研发 PR CI、360dp 深色默认/200% 字体 Emulator UI 和合入 dev 后收尾 CI 通过后，复核四个操作在实际截图中
  均可见，且“新建（安全默认）”始终先于历史选择。

## 第一次截图审计驳回

- 首次实现的 PR #231 虽通过 CI 与模拟器运行，但 artifact `termuxpro-emulator-ui-243` 的默认及 200%
  字体首屏显示：重复摘要、完整连接策略和“启动前先确认”卡片把启动按钮整体推到首屏之外。
- 这证明“控件可渲染”不等于“用户能及时找到操作”。本报告不将 PR #231 作为验收通过结论；已立即重开
  后续修复：删除重复摘要、将首屏上下文收敛为服务器与目录、把 Claude/Codex 的两个“新建（安全默认）”
  聚合到首屏启动卡，并将策略详情放入按需的启动前确认区。历史选择保留在对应工具说明卡中，不自动恢复。
