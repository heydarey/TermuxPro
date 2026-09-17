# 终端顶栏 AI 入口安全语义验收

日期：2026-09-17

## 增值服务准入

- 分类：AI CLI、移动端交互体验。
- 不是 Termux 原始能力重做：本轮不修改 PTY、终端输入、基础会话、包管理或本地 shell 行为；只调整 TermuxPro 增值层的终端顶栏入口辅助语义。

## 主动发现记录

1. 终端顶栏为保护 320–360dp 和 200% 字体使用短标签 `AI`，但辅助语义此前只说明“打开会话中心”，没有说明它不会立即执行命令。
   - 影响旅程：用户在远程 SSH 终端中看到 `AI`，可能担心误触后直接向当前终端发送 `claude` 或 `codex`。
   - 处理：保留短标签，强化 `contentDescription`。
2. 共享 Claude Code 和共享 tmux 是用户已明确指出的高风险场景，AI 入口需要在入口层就说明“不自动恢复历史或进入 tmux”。
   - 影响旅程：用户不必打开弹窗后才知道安全边界，降低误恢复他人上下文的心理负担。
   - 处理：把“不自动恢复历史 / 不进入 tmux”纳入顶栏入口描述。
3. 菜单项 `AI CLI 会话中心` 已有较完整说明，但顶栏短按钮是更高频入口；两者语义不一致会导致真实使用中只看顶栏的人缺少安全信息。
   - 影响旅程：高频终端内使用者更依赖顶栏，不应只在二级菜单里解释策略。
   - 处理：布局 smoke test 锁定顶栏 AI 按钮描述包含安全关键字。

## 实现范围

- 中文和英文 `terminal_ai_cli_center_description` 均补充：先选择新建或历史入口，不会立即发送命令、自动恢复历史或进入 tmux。
- 可见文本仍为 `AI`，不增加顶栏宽度，不影响 48dp 触控目标。
- `CustomLayoutsSmokeTest.terminalNavigationUsesPersistentLabelsAndAccessibleTargets` 新增断言，防止后续把安全语义退化为普通“打开中心”。

## 非目标

- 不改变 AI CLI 启动命令。
- 不改变 Claude/Codex 历史选择器策略。
- 不读取或管理 Claude/Codex 私有历史。
- 不进入或管理远端 tmux 会话。

## 验收重点

- 顶栏仍适合窄屏和大字体：可见标签保持短文本 `AI`。
- 辅助语义能明确告诉用户该入口只打开会话中心，必须先选择，不会立刻对当前终端产生副作用。
- 原始终端能力不受影响：不拦截普通终端输入、不改变滑动或 PTY 行为。

## 本地验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.CustomLayoutsSmokeTest`：未进入测试执行；本地远程机解析 `com.android.tools.build:gradle:8.13.2` 时访问 `dl.google.com` 读超时。该项需由 GitHub Actions 的 Android 环境继续验证。
- `./scripts/pre-push-smoke.sh origin/dev`：静态阶段通过（Skill、版本、workflow 策略、发布通知、GitHub 噪声、上下文检查点、Goal 生命周期、缓存清理、Android SDK 引导、字符串资源和资源守卫）；进入 Gradle 阶段后超过 60 秒无输出，按共享远程机资源策略手动中断，Android/Robolectric 结果以 GitHub CI 为准。
- GitHub CI 首轮暴露 `TerminalProjectToolsMenuTest` 仍断言旧的 AI 会话中心描述；已同步更新工具箱菜单断言，确保顶栏 `AI` 和工具箱 `AI CLI 会话中心` 共用一致的安全语义。
