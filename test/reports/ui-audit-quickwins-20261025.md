# UI 审计快赢批量修复验收（2026-10-25）

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| 200% 大字体下终端顶部工具按钮被裁为“工具箱·”，模式语义丢失；SSH 地址提示被截断为“user@host 或 ssh 命”。 | UI 走查审计（run 40173680080 artifact termuxpro-emulator-ui-461：terminal-navigation-font200.png、workspace-font200.png） | 大字体用户使用终端工具箱与首次 SSH 输入 | 移动端工作流效率、远程工作区 | P1 | 本轮处理：fontScale≥1.5 时工具按钮切换“历史/TUI”极简变体（完整语义保留在无障碍描述），SSH 提示收敛为“user@host 或 ssh…”。 |
| SSH 公钥页空态（未找到公钥）时“复制公钥/安装到服务器”渲染成正常实心样式；代码已有 setEnabled(false)，但深色主题低对比下不可辨。 | UI 走查审计截图复核 + 代码走查 SshKeysActivity | 首次配置密钥免密的用户误点必失败按钮 | 远程工作区 | P2 | 本轮处理：禁用时同步 setAlpha(0.4)，恢复可用时还原。 |
| Git 工作台同屏三次“按文件审查”：状态摘要“推荐动作：”复述 + 卡片按钮 + 底部主按钮，增量信息为零。 | UI 走查审计（git-diff-default.png） | Git 审查页决策成本 | tmux/Git 可视化 | P2 | 本轮处理：状态摘要移除“推荐动作”复述并删除 recommendedAction 与 8 个无引用 recommend 字符串；主按钮（状态自适应）与建议区保留为唯一下一步入口。与审计建议的“删底部 CTA”不同，保留主按钮因其状态自适应承载多状态动作，风险更小。 |
| AI CLI 会话中心同屏两段重复免责声明、tmux 行零操作可见性、无效工作区死胡同错误态。 | UI 走查审计 | AI CLI、tmux 管理、增值子页首次体验 | AI CLI、tmux/Git 可视化、远程工作区 | P1 | 本轮暂缓：涉及页面结构与交互重构（M 级），登记 backlog 后续切片处理，不与小修混入同一提交。 |

## 本轮范围

- `TerminalProjectToolsMenu.toolsButtonLabel` 新增大字体重载；`TermuxActivity` 按 fontScale≥1.5 传入；新增 `workspace_tools_scrollback_action_compact`（历史/History）、`workspace_tools_tui_scroll_action_compact`（TUI）。
- `WorkspaceActivity.configureLargeFontLayout` 大字体 SSH 提示改用新 `workspace_host_hint_minimal`（user@host 或 ssh…）。
- `SshKeysActivity` 公钥不可用时复制/安装按钮 setAlpha(0.4) 禁用视觉。
- `GitDiffActivity.stateSummary` 移除 recommendedAction 复述；删除 recommendedAction 方法与 8 个 `git_workbench_recommend_*` 字符串（中英）；`git_workbench_state_summary` 收敛为“当前状态：%1$s；%2$s”。
- `TerminalProjectToolsMenuTest` 补大字体重载断言；`GitDiffActivityTest` 断言摘要含“当前状态”且不含“推荐动作”。
- 新增静态门禁 `test/ui-affordance-batch-test.sh` 锁定上述行为防回退。

## 非目标

- 不重构 AI CLI 会话中心信息层级、tmux 行操作、无效工作区错误态（登记后续切片）。
- 不改变工具按钮的功能行为、无障碍描述与默认字体文案。
- 不改变 Git 主按钮状态机与建议区文案。

## 验收

- `bash test/ui-affordance-batch-test.sh` 通过。
- `bash test/ai-launch-decision-copy-test.sh` 通过（strings 变更后无回退）。
- 大字体与 Git 页面视觉行为由冻结时 ui-emulator 门禁截图复验（workspace-font200、terminal-navigation、git-diff 场景）。

## 结论

三个高证据 UI 快赢项以最小侵入方式落地：大字体可读性、空态禁用可辨性、Git 页信息去噪；更大的结构化体验问题已按优先级登记，不与小修混合。
