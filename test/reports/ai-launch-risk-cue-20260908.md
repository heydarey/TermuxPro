# AI 启动前 tmux 风险摘要验收

## 背景

用户的核心场景是手机 SSH 到远程服务器使用 Claude Code 和 Codex CLI。Claude Code 在共享账号下容易误入
他人的 tmux/历史上下文；此前 AI 会话中心已经说明 AI 快捷启动不会继承工作区 tmux 策略，但该说明位于
“启动前先确认”区域，用户可能先点了 Claude/Codex。

## 增值服务分类

- AI CLI
- 远程工作区
- tmux 可视化与安全边界
- 移动端工作流效率

该改动不触碰 Termux 原始终端、PTY、本地 shell、包管理或基础会话能力。

## 改动

- 在 AI 会话中心“开始 AI 工作”卡片内新增启动前风险摘要。
- 无有效工作区时明确：AI 启动不会猜服务器、项目或 tmux 会话。
- 普通 SSH 策略下明确：仅连接 SSH，不自动进入 tmux 或恢复历史。
- 工作区配置为查看/进入/创建 tmux 时明确：AI 快捷启动不会继承自动 tmux，需要用户先去会话中心显式选择。

## 验收标准

- 用户点击 Claude/Codex 前即可看到本次 AI 启动是否会进入 tmux。
- 共享 Claude/tmux 场景下，界面必须明确“工作区默认 tmux 不会被 AI 启动继承”，避免污染他人会话。
- 无有效工作区时不能猜测或回退到其他服务器。
- 不读取 Claude/Codex 私有历史，不自动恢复最近会话。

## 验证

- `TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：通过。
- `./scripts/pre-push-smoke.sh origin/dev`：通过。
