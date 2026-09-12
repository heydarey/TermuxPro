# AI CLI 启动决策层级优化验收记录（2026-09-12）

## 背景

用户高频场景是手机 SSH 到远端服务器后使用 Codex CLI 和 Claude Code。Claude Code 在共享账号场景下容易
误续接他人上下文，tmux 也可能存在多人共享会话。此前 AI 会话中心虽然已经声明“不自动进入 tmux/历史”，
但“新建 Claude / 选择 Claude 历史 / 新建 Codex / 选择 Codex 历史”四个按钮视觉层级接近，手机用户需要
阅读说明后自行判断哪个更安全。

## 增值服务分类

- AI CLI
- 远程工作区
- 移动端工作流效率

这不是 Termux 原始终端能力重做；仅优化 TermuxPro 增值层中 AI CLI 启动前的决策体验。

## 主动发现

1. “开始 AI 工作”区域把安全默认和高风险历史入口放在同一层级，容易让用户误以为四个入口风险相同。
2. Claude 共享账号风险依赖长说明文字，按钮附近缺少明确的“推荐 / 谨慎”决策提示。
3. 首页已经完成渐进展示，不能再把更多 AI 说明堆回首页；应在 AI 会话中心内部降低判断成本。

## 本轮改动

- 将 AI 会话中心启动区拆成两段：
  - “推荐：新建独立 AI 会话”下只放新建 Claude / 新建 Codex。
  - “谨慎：只打开历史选择器”下放选择 Claude 历史 / 选择 Codex 历史。
- 200% 字体下隐藏重复的启动说明，保留风险摘要、推荐/谨慎标签和四个核心按钮，避免核心入口被挤出首屏。
- 保持所有启动命令、安全弹窗、历史记录和 SSH/tmux 策略不变。
- 补充 Robolectric 断言，防止后续回退为无提示的四按钮同级结构。

## 验收重点

- 用户不用读完长说明，也能先看到“新建是推荐默认”。
- 历史入口仍可达，但必须带着谨慎语义出现。
- 不增加首页控件，不改变 Termux 原始终端行为。
- 不自动进入 tmux，不自动恢复 Claude/Codex 历史。

## 本地验证

- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `test/dialog-readable-style-test.sh`：通过。
- `:app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：本机在配置阶段因共享机
  Android NDK `27.0.12077973` 未配置失败，未进入测试断言；该限制不作为功能通过证据，完整 Android
  构建和模拟器截图继续交给 GitHub Runner 门禁。
- `./scripts/pre-push-smoke.sh`：脚本级门禁与资源守卫通过，进入 Gradle 阶段后 180 秒超时；未产生代码
  断言失败日志。本轮不在共享机安装全局 NDK 或启动本地模拟器。
- GitHub Runner 首次模拟器 200% 字体验收暴露四个核心按钮未全部留在首屏；已修正大字体层级，等待同
  PR 复跑验证。
