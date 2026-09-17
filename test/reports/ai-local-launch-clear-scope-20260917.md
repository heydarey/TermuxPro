# AI 本地启动记录清空范围验收

日期：2026-09-17

## 增值服务准入

- 分类：AI CLI、远程工作区、移动端交互体验。
- 增值层位置：帮助手机用户在 Claude Code / Codex CLI 工作流里管理 TermuxPro 自己记录的本地启动入口，降低共享账号和多工作区场景下的误删焦虑。
- 非 Termux 原始能力：不修改 PTY、终端输入、基础会话、包管理、本地 shell 或官方 Termux 历史能力。

## 主动发现候选观察

1. `清空记录` 按钮在有历史时仍是泛化文案，用户必须点开确认弹窗才知道会清空多少条记录。
   - 影响旅程：共享 Claude/Codex 场景下，用户可能担心一键清掉远端历史或 tmux 上下文。
   - 推进理由：这是高频 AI CLI 会话中心里的危险动作，应在按钮层就把数量和本地范围讲清楚。
2. 单条删除按钮已经动态展示 `删除本地记录：工具 · 模式`，但批量清空按钮没有对应动态范围，形成同页语义不一致。
   - 影响旅程：同一个“本地记录”对象生命周期里，单删可预测，批量删除不可预测。
   - 推进理由：遵守 CRUD 闭环和危险动作前置解释原则，避免用户只能靠弹窗二次确认理解对象范围。
3. AI 会话中心多处强调不读取 Claude/Codex 私有历史，但批量清空按钮本身缺少无障碍说明。
   - 影响旅程：TalkBack 或自动化验收只能读到“清空记录”，无法判断它是否会影响远端 AI 历史、tmux 或终端输出。
   - 推进理由：可见文案保持短，但辅助语义必须包含工作区、数量和不影响远端对象的边界。

## 实现范围

- 有历史时，`清空记录` 按钮改为 `清空 N 条本地记录`。
- 按钮 `contentDescription` 包含当前工作区名称、记录数，以及“不删除 Claude/Codex 远端历史、tmux 会话或终端输出”。
- 无工作区或无记录时仍保持普通 `清空记录`，按钮禁用。
- 清空确认弹窗、实际清空逻辑和工作区隔离不变。

## 非目标

- 不读取、解析、删除或展示 Claude/Codex 私有历史。
- 不删除远端 tmux 会话。
- 不删除终端输出或远端文件。
- 不修改 AI CLI 启动命令。

## 验收重点

- 批量清空动作在点击前即可看出数量和本地范围。
- 单删、批量清空、管理列表保持同一“TermuxPro 本地记录”语义。
- 原始终端能力不受影响。

## 本地验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：本地远程机启动 Gradle 后超过 60 秒无输出，按共享资源策略手动中断；未进入可证明的测试失败状态，Android/Robolectric 结果以 GitHub CI 为准。
- `./scripts/pre-push-smoke.sh origin/dev`：静态阶段通过（Skill、版本、workflow 策略、发布通知、GitHub 噪声、上下文检查点、Goal 生命周期、缓存清理、Android SDK 引导、字符串资源和资源守卫）；进入 Gradle 阶段后超过 60 秒无输出，按共享远程机资源策略手动中断，Android/Robolectric 结果以 GitHub CI 为准。
