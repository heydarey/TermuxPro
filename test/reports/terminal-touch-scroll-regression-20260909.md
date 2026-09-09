# 终端触摸滚动回归复验

## 背景

用户反馈旧体验包 `0.5.0` 中，手机端终端上下滑动无法查看历史输出，滑动被解释为命令行上下键或
TUI 列表移动。该问题会直接影响 SSH 远程使用 Codex CLI、Claude Code 时阅读长输出，是 TermuxPro
增值层不得破坏原始终端能力的兼容红线。

## 当前结论

当前 `dev` / `dev_dailyIteration` 代码仍保持既有修复：手机触摸默认滚动终端 scrollback，不向 shell、
Codex CLI 或 Claude Code 写入上下方向键；AI/TUI 面板滚动必须由用户显式切换，且仅在目标程序启用
鼠标追踪时发送滚轮事件，不支持时回退到 scrollback。

## 验证命令

```text
./test/terminal-touch-scrollback-test.sh
TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.TerminalTouchScrollPolicyTest --tests com.termux.app.TerminalProjectToolsMenuTest
```

## 验证结果

- `test/terminal-touch-scrollback-test.sh`：通过。
- `TerminalTouchScrollPolicyTest`：通过。
- `TerminalProjectToolsMenuTest`：通过。

## 后续关注

- 若用户仍在 `0.5.0` 体验包复现，应升级到最新稳定版后复验。
- 后续任何终端工具箱、AI/TUI 模式或手势逻辑变更，必须继续运行上述门禁，确保 Termux 原始 scrollback
  能力不被 TermuxPro 增值层污染。
