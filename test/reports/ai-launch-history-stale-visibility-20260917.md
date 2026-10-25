# AI CLI 本地过期记录前置可见性

日期：2026-09-17

## 结论

通过代码级改进与可执行静态门禁；Robolectric 目标测试已补断言，但本机 Android SDK 缺
NDK `27.0.12077973`，且在线依赖解析访问 `dl.google.com` 超时，因此本地 JVM 测试未完成，需由
GitHub CI 的标准 Android 环境补验。

## 主动发现记录

| 候选观察 | 证据来源 | 影响旅程 | 增值服务分类 | 决策 |
|---|---|---|---|---|
| 本地启动记录目标变化后，只有点击“再次打开”才知道记录已经不能直接使用。 | 负责人走查 `AiCliSessionCenterActivity.bindHistory()` 与 `launchAiCliWithModeGuard()`。 | 多服务器/多目录 SSH 开发中重复打开 Claude/Codex。 | AI CLI、远程工作区、移动端交互体验 | 本轮推进，把风险前移到列表层。 |
| “查看全部记录”列表此前只展示工具、模式、时间和旧目标，无法在选择前区分可打开记录与过期记录。 | UI/UX 走查本地启动记录管理弹窗。 | 维护多条本地启动记录时容易误点过期记录。 | AI CLI、移动端交互体验 | 本轮推进，列表项追加过期提示。 |
| 已有目标变化弹窗能阻止误启动，但入口按钮仍显示“再次打开”，给用户暗示过期记录可直接执行。 | 产品走查按钮文案与安全弹窗之间的语义差异。 | 共享 Claude 账号或多项目 Codex 场景下误解风险。 | AI CLI、远程工作区 | 本轮推进，过期记录主按钮改为“处理过期记录”。 |

## 改动范围

- AI CLI 会话中心的本地启动记录摘要会在记录目标与当前工作区不一致时追加：
  “目标已变化：不会直接再次打开，请检查工作区或删除这条过期本地记录。”
- “查看全部记录”弹窗同样展示过期提示。
- 当最新记录过期时，“下一步”提示改为先检查工作区或删除过期记录。
- 当最新记录过期时，主按钮从“再次打开”改为“处理过期记录”。
- 从管理列表选择过期记录时，直接显示既有目标变化弹窗，提供“检查工作区 / 删除本地记录”闭环。

## 非目标

- 不读取 Claude/Codex 私有历史。
- 不自动恢复 AI 历史。
- 不自动进入 tmux。
- 不删除远端 AI 历史、tmux 会话、终端输出或远端文件。
- 不改变 Termux 原始终端、PTY、包管理、本地 shell、基础会话和基础快捷键语义。

## 验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./test/ai-launch-decision-copy-test.sh`：通过。
- `git diff --check`：通过。
- `./scripts/pre-push-smoke.sh origin/dev`：Skills、版本元数据、workflow 策略、发布通知、GitHub 噪声审计、
  GitHub CLI/check suite、上下文检查点、长期 Goal、生成缓存、Android SDK 引导、字符串资源和资源守卫均通过；
  进入 Gradle 子阶段后长时间无输出，已中断以保护共享服务器资源。
- `./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：
  未跑到测试阶段，访问 `dl.google.com` 解析 Android Gradle Plugin 超时。
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：
  长时间无输出，已中断避免占用共享服务器资源。
- `./gradlew --offline --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：
  配置阶段失败，本机 SDK 缺 NDK `27.0.12077973`。

## 下一步

- 由 GitHub CI 补跑 Robolectric 与 APK 门禁。
- 后续继续检查 AI CLI 会话中心在多工作区、多条本地记录和 200% 字体下的扫读负担。
