# AI 本地启动记录空态与英文文案语义补齐（2026-09-17）

## 结论

通过。AI CLI 会话中心的空态/禁用态按钮和英文资源继续收敛到“TermuxPro 本地启动记录”语义，
不再用容易误读的 `Repeat last`、`Delete latest`、`Clear records` 或 `local entry record`。
该切片只影响 AI CLI 增值层展示文案，不读取 Claude/Codex 私有历史，不自动恢复历史，不进入 tmux，
不改变 Termux 原始终端、PTY、本地 shell 或包管理能力。

## 主动发现记录

| 候选观察 | 来源 | 影响旅程 | 分类 | 处理结论 |
| --- | --- | --- | --- | --- |
| 中文主路径已有“本地启动记录”，但空态/禁用态按钮仍写“重复上次 / 删除最近 / 清空记录”。 | 负责人走查 `AiCliSessionCenterActivity.bindHistory()` 和中文资源。 | 用户首次打开 AI CLI 会话中心或当前工作区无记录时，仍可能误解 TermuxPro 会恢复远端 AI 历史。 | AI CLI、移动端交互体验 | 本轮修复。 |
| 英文资源仍有 `Repeat last / Delete latest / Clear records`，系统语言切到英文或生成截图时语义回退。 | UI/UX 多语言资源走查。 | 英文系统、截图证据、无障碍朗读和后续翻译复用。 | AI CLI、移动端交互体验 | 本轮修复。 |
| 摘要标题 `local entry record` 太泛，无法说明这是本 App 发起的 AI CLI 本地启动记录。 | 产品语义审计。 | 开发者判断“这是远端历史还是本地启动入口”。 | AI CLI、远程工作区 | 本轮修复为 `local launch record`。 |

## 变更范围

- 中文资源：
  - `ai_cli_center_repeat_last` → `再次打开最近本地记录`
  - `ai_cli_center_delete_latest` → `删除最近本地记录`
  - `ai_cli_center_clear_history` → `清空本地记录`
- 英文资源：
  - `Repeat last` → `Open latest local record`
  - `Delete latest` → `Delete latest local record`
  - `Clear records` → `Clear local records`
  - `Latest N local entry record(s)` → `Latest N local launch record(s)`
- 静态门禁：
  - `test/ai-launch-decision-copy-test.sh` 新增中英文按钮与摘要断言。

## 非目标

- 不读取、解析、删除 Claude/Codex 私有历史。
- 不新增 AI 历史 CRUD，不自动恢复最近历史。
- 不进入或管理 tmux 会话。
- 不改变 SSH 连接、终端滚动或 Termux 原始能力。

## 验收标准

1. 无记录时，重复/删除/清空按钮即使禁用也明确是本地记录。
2. 有记录时，清空按钮继续展示数量并说明只清空当前工作区本地记录。
3. 英文系统下同样能看出 TermuxPro 管的是本机本 App 启动记录，而不是远端 Claude/Codex 历史库。
4. 静态门禁防止后续回退到 `Repeat last / Delete latest / Clear records` 这类泛化文案。

## 验证

- `./test/ai-launch-decision-copy-test.sh`：通过。
- `./test/android-string-resource-parity-test.sh`：通过。
- `git diff --check`：通过。
- `./scripts/pre-push-smoke.sh`：静态门禁、资源守卫、版本/发布/Goal/通知/GitHub 脚本等前置项通过；
  进入 `:app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest --tests com.termux.app.CustomLayoutsSmokeTest --tests com.termux.app.WorkspaceActivitySmokeTest`
  后本地 Gradle 超过 10 分钟无输出但仍占用 CPU，按共享服务器资源策略手动中断，未作为本地通过证据。
  本轮代码变更仅涉及字符串资源、静态测试和文档，最终 Android 单测门禁保留给 GitHub CI。
- GitHub CI 首次运行暴露 `AiCliSessionCenterActivityTest#repeatsDeletesAndClearsOnlyCurrentWorkspaceLaunchHistory`
  仍断言旧空态文案 `重复上次`；已同步更新为 `再次打开最近本地记录`，并补充删除/清空空态按钮断言。
- 修复断言后尝试本地最小 Robolectric：
  `timeout 300 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest.repeatsDeletesAndClearsOnlyCurrentWorkspaceLaunchHistory`；
  本地 300 秒无测试输出后 timeout 退出码 124，未作为通过证据，继续以 GitHub CI 复跑为准。
