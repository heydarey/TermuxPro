# AI 启动记录清空确认验收记录

## 增值服务准入

- 分类：AI CLI、远程工作区、移动端工作流效率。
- 结论：通过。该切片只处理 TermuxPro 增值层保存的 AI CLI 启动记录，不读取或修改 Claude/Codex 私有历史，
  不触碰 Termux 原始终端/PTY/本地 shell 能力。

## 主动发现

| 观察 | 来源 | 影响旅程 | 优先级 | 下一动作 |
|---|---|---|---|---|
| “清空记录”属于不可逆本地数据操作，但当前点击后直接清空。 | 负责人产品走查 | AI CLI 会话中心 → 最近启动 → 误触恢复 | P1 | 增加二次确认 |
| 用户需要知道清空范围：只删 TermuxPro 本地记录，不删远端 AI 历史、tmux 会话或终端输出。 | 安全/隐私走查 | 共享 Claude 账号和多人 tmux 场景 | P1 | 确认弹窗显式说明删除边界 |
| 清空确认需要沿用深色弹窗样式，避免重新引入黑字/白底问题。 | UI/UX 走查 | 深色主题、大字体和危险操作层级 | P1 | 使用 `TermuxProDialogStyle.show` 并断言确认按钮颜色 |

## 本轮修复

- `AiCliSessionCenterActivity` 将“清空记录”改为 `confirmClearCurrentHistory()`。
- 有效工作区且存在历史记录时才弹出确认；无工作区或无记录时只刷新状态，不产生无效弹窗。
- 确认内容包含工作区名称、待删除记录数和安全边界：
  - 不删除 Claude/Codex 远端历史；
  - 不删除 tmux 会话；
  - 不删除终端输出。
- 确认弹窗使用 TermuxPro 深色样式。

## 验收标准

1. 点击“清空记录”不会立即删除。
2. 取消确认后，最近 AI 启动记录仍保留。
3. 确认后，只清空当前工作区的 TermuxPro 本地启动记录。
4. 弹窗文字可读，确认按钮使用产品主色。

## 当前证据

待随本轮统一执行：

- `AiCliSessionCenterActivityTest`
- `pre-push-smoke`
- GitHub CI 与 Emulator UI

## 非目标

- 不读取 Claude/Codex 私有历史。
- 不删除远端历史、tmux 会话、终端输出或任何远端文件。
- 不改变 AI CLI 启动命令策略；AI 启动仍始终仅建立 SSH，不自动进入 tmux。
