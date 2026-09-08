# AI CLI 会话中心行动分组回归报告（2026-09-08）

## 增值服务分类

- AI CLI
- 远程工作区
- tmux 可视化
- Git 可视化
- 移动端工作流效率
- 上下文工具箱

## 背景

用户核心场景是手机 SSH 到远程服务器使用 Claude Code 和 Codex CLI。AI 会话中心已经具备新建、
历史选择、环境预检、tmux 会话中心、Git 工作台和快捷指令入口，但页面底部按钮连续堆叠，用户需要
自行判断“启动前应该做什么、启动 AI、AI 完成后应该看什么”。

## 本轮改动

- 将 AI 会话中心入口按行动阶段分为：
  - 当前上下文：展示工作区、路径、工作区连接策略和 AI 快捷启动策略。
  - 启动前先确认：放置 AI 远端环境检查和 tmux 会话选择/管理。
  - Claude Code / Codex CLI：保留新建与历史选择入口，继续固定 `ssh_only`。
  - AI 完成后：放置 Git 改动检查、快捷指令模板和服务器/项目配置。
- 文案明确“共享服务器上最容易误入的是远端环境和 tmux”，引导先检查再启动。
- 不读取 Claude/Codex 私有历史。
- 不自动进入、创建或恢复 tmux。
- 不改 Termux 原始终端、PTY、本地 shell、基础会话或基础快捷键行为。

## 验收重点

1. 空工作区时仍展示可读空状态，并引导回工作台配置。
2. 有效工作区时仍展示目标地址、项目路径、工作区连接策略和 AI 快捷启动策略。
3. 启动前入口应能到达连接诊断和 tmux 会话中心。
4. AI 完成后入口应能到达 Git 工作台、快捷指令和工作区配置。
5. Claude/Codex 启动仍必须打开独立 SSH-only 新终端，不继承工作区自动 tmux 策略。

## 本地验证

- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./test/dialog-readable-style-test.sh`：通过。
- `. ./scripts/resolve-jdk17.sh && TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest --tests com.termux.app.CustomLayoutsSmokeTest --max-workers=2`：通过。
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./scripts/pre-push-smoke.sh origin/dev`：通过。
