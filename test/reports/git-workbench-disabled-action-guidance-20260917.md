# Git 工作台灰置操作可理解性验收记录（2026-09-17）

## 本轮目标

让手机端 Git 工作台中的灰置按钮不仅“不可点”，还说明为什么不可点、下一步如何解锁。该改动服务于
TermuxPro 增值层的 Git 可视化管理和移动端工作流效率，不改变 Termux 原始终端、PTY 或 Git 命令语义。

## 主动发现

| 观察 | 证据来源 | 影响旅程 | 分类 | 处理 |
|---|---|---|---|---|
| Git 工作台已有分支、同步、暂存、提交、stash 等按钮，但按钮灰置时用户只能猜条件。 | 负责人代码走查 `GitDiffActivity.showOverview` | 手机 SSH 远程开发时查看仓库状态后决定下一步 | tmux/Git 可视化、移动端交互体验 | 本轮推进 |
| 页面已有“下一步建议”，但低频工具箱按钮仍需要独立解释，否则用户滚动到底部看到灰按钮会误以为功能坏了。 | 产品旅程复盘 | 切换分支、推送、提交、stash 前的判断 | 移动端工作流效率 | 本轮推进 |
| 直接把解释文字显示在按钮旁会让 Git 工作台继续变重。 | UI/UX 取舍 | 360dp、小屏和 200% 字体 | 移动端交互体验 | 只写入 contentDescription，不增加视觉噪声 |

## 变更范围

- `GitDiffActivity` 统一通过 `setButtonAvailability` 绑定按钮启用状态、透明度和可访问说明。
- 为按文件、暂存、取消暂存、提交、保存 stash、删除本地分支、fetch、快进拉取和普通推送补充启用/禁用说明。
- 同步默认英文资源和简体中文资源。
- Robolectric 回归覆盖常见灰置状态，验证说明包含“没有配置上游 / 没有未暂存 / 不能提交 / 没有可安全删除”等原因。

## 非目标与安全

- 不新增 Git 写操作。
- 不执行真实 SSH、Git、tmux 或 AI CLI 命令。
- 不改变按钮启用条件、确认弹窗、安全删除、普通推送和 fast-forward-only 规则。
- 不重做 Termux 原始终端能力。

## 验收重点

- 灰置按钮必须有可解释的 `contentDescription`。
- 写操作仍保持确认后执行，不支持 force push、merge、rebase、丢弃文件或删除远端分支。
- 视觉页面不新增大段说明，避免 Git 工作台继续过载。

## 本地验证

- `git diff --check`：通过。
- `./scripts/resource-guard.sh`：通过；当前是远程共享环境，KVM 不可用，应保持单个重任务。
- `./scripts/codex-quota-guard.sh`：通过；总剩余额度 20%，高于 15% 阈值。
- `./scripts/validate-skills.sh`：通过。
- 推送前静态门禁集合通过：版本元数据、workflow 触发策略、发布通知格式、发布窗口、GitHub 噪声审计、
  GitHub CLI/check suite、上下文检查点、Goal 生命周期、生成缓存清理、Android SDK 引导和字符串资源
  中英文 key 对齐。
- `:app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest`：本地共享机运行超过 5 分钟仍无终态，
  已中止以避免占用资源；不作为失败结论，完整编译和 Robolectric 回归以后续 GitHub CI 为准。
- `:app:compileDebugJavaWithJavac`：本地共享机 5 分钟超时无输出，已停止；远端 CI 负责最终编译门禁。
