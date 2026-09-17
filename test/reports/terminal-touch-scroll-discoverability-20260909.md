# 终端触摸滚动入口可发现性回归（2026-09-09）

## 背景

`0.10.0` 体验反馈显示：终端页上下滑动在某些状态下像是在控制命令行/TUI，而不是查看上方历史输出。

这类问题不能简单归为“用户配置问题”。对手机软键盘场景，手指上下滑动的默认心智是阅读历史；如果用户切到 AI/TUI 滚动模式，产品必须显性提示当前状态，并提供清晰的切回动作。

## 本轮修复

- 终端顶部工具入口从固定“工具箱”改为显示当前滑动语义：
  - `工具箱·历史`：手指滑动查看终端历史，推荐默认。
  - `工具箱·TUI`：手指滑动控制支持鼠标事件的 AI/TUI 面板。
- 工具箱菜单中的切换项从“滑动：A → B”改为明确动作：
  - `改为查看终端历史（推荐）`
  - `改为控制 AI/TUI 面板`
- 不改变 Termux 原始终端默认滚动策略：默认仍为 scrollback；外接鼠标滚轮仍保留 TUI/程序内滚动兼容。

## 验证

已通过：

```bash
source ./scripts/resolve-jdk17.sh
TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest \
  --tests com.termux.app.TerminalTouchScrollPolicyTest \
  --tests com.termux.app.TerminalProjectToolsMenuTest \
  --tests com.termux.app.CustomLayoutsSmokeTest
```

## 后续观察

如果真实设备仍反馈“无法上滑看历史”，下一步不再只改文案，应继续核查：

- 当前偏好值是否被错误迁移为 `tui`；
- AI/Claude/Codex 启动流程是否误改触摸滚动模式；
- TerminalView 在 alternate screen 下的触摸事件是否仍被某些 ROM 标记为鼠标源。
