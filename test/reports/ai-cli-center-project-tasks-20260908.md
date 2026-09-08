# AI CLI 会话中心项目任务入口验收记录（2026-09-08）

## 背景

用户核心场景是手机 SSH 到远程机器使用 Codex CLI 和 Claude Code。AI 完成修改后，开发者通常需要查看 Git
改动并运行项目任务或测试。如果这些操作只能靠手动输入命令，容易把验证命令误输入到正在工作的 AI/TUI
终端里，也不符合 TermuxPro “增值服务优先”的产品方向。

## 本轮改动

- 在 AI CLI 会话中心的“AI 完成后”卡片中增加“运行项目任务”入口。
- 入口复用现有 `ProjectTasksNavigation` 和 `ProjectTasksActivity`，不新增重复命令执行逻辑。
- 工作区无效或缺少归属 token 时，入口回退到工作台配置页，不伪造可运行状态。
- 更新中英文文案，明确建议顺序：先查看 Git 改动，再运行项目任务或测试，避免污染当前 AI 终端。

## 验收重点

- 属于 TermuxPro 增值层：AI CLI、远程工作区、移动端工作流效率。
- 不改变 Termux 原始终端、PTY、本地 shell、包管理或基础会话能力。
- 不自动读取 Claude/Codex 私有历史。
- 不自动进入 tmux，不继承工作区自动 tmux 策略。
- 无效工作区必须回到工作台配置。

## 自动化覆盖

- `AiCliSessionCenterActivityTest`：
  - 空状态仍显示安全提示和 AI 完成后说明。
  - 有效工作区下可从 AI 中心打开项目任务页。
  - 无效工作区下项目任务入口回退到工作台。
- `ProjectTasksNavigationTest` 继续覆盖有效/无效工作区导航。

## 结论

本切片把 AI 会话中心从“启动 AI”继续推进到“AI 完成后验证”，减少手机端输入成本和 TUI 上下文污染风险。
