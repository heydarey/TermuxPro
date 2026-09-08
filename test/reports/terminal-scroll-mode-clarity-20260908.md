# 终端触摸滚动模式可发现性验收记录（2026-09-08）

## 背景

用户在旧体验包中反馈：手机端终端上下滑动被解释为命令行上下键，无法回看历史输出。当前代码已把手机触摸
默认滚动终端历史，并提供 AI/TUI 面板滚动模式，但终端顶栏和工具箱文案仍偏抽象，容易让用户不知道当前
手势到底会“看历史”还是“控制 TUI”。

## 本轮改动

- 终端顶栏工具箱状态从“工具箱 · 历史/TUI”改为“工具箱 · 滑动看历史/滑动控 TUI”。
- 工具箱菜单项改为“滑动：终端历史（推荐）→ 切到 AI/TUI”和“滑动：AI/TUI 面板 → 切到历史”。
- Toast 明确说明“手指上下滑动已改为查看终端历史”或“控制支持鼠标事件的 AI/TUI 面板”。

## 验收要点

- 默认模式仍是 `scrollback`，手机手指滑动优先回看终端历史，不向 shell/TUI 发送方向键。
- TUI 模式只在目标程序启用鼠标追踪时控制 AI/TUI 面板；不支持鼠标事件时仍回退到终端历史。
- 入口文案直接表达当前手势语义，降低用户把旧包行为或错误模式误判为“终端不能滚动”的概率。

## 本地验证

- `git diff --check`
- `./scripts/validate-skills.sh`
- `./test/dialog-readable-style-test.sh`
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew :app:testDebugUnitTest --tests com.termux.app.TerminalTouchScrollPolicyTest --tests com.termux.app.TerminalProjectToolsMenuTest --max-workers=2`
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./scripts/pre-push-smoke.sh origin/dev`
