# AI 本地启动记录语义一致性验收（2026-09-15）

## 背景

上一轮已把 AI CLI 会话中心主标题和主操作收敛为“TermuxPro 本地启动记录 / 再次打开”，但继续走查发现
空态、删除确认、管理弹窗和记录时间仍残留“AI 启动记录 / 启动时间 / 重复启动”。这些词在共享
Claude 账号和多工作区场景下仍可能让用户误以为 TermuxPro 正在读取或管理远端 Claude/Codex 历史。

## 主动发现候选

| 候选观察 | 证据来源 | 影响旅程 | 增值服务分类 | 优先级 | 处理结论 |
| --- | --- | --- | --- | --- | --- |
| 空态仍写“没有 AI 启动记录” | `strings.xml` 走查 | 新用户可能误解为缺少远端 AI 历史读取能力 | AI CLI、移动端交互体验 | P1 | 本轮处理 |
| 删除标题仍写“AI 启动记录” | `AiCliSessionCenterActivityTest` 与资源走查 | 删除前最后一步语义不一致，增加误删远端历史顾虑 | AI CLI、远程工作区 | P1 | 本轮处理 |
| 记录时间仍写“启动时间” | 记录详情/确认弹窗走查 | 多次打开同一历史选择器时，用户难以判断这是本机记录时间还是远端会话时间 | AI CLI、移动端工作流效率 | P1 | 本轮处理 |

本轮不是 Termux 原始终端、PTY、包管理、本地 shell 或基础会话能力重做；只优化 TermuxPro 增值层对
Claude/Codex 启动入口的解释和安全边界。

## 变更

- 空态改为“当前工作区还没有 TermuxPro 本地启动记录”。
- 删除最近/删除单条标题改为“本地启动记录”。
- 管理弹窗空态与无障碍描述改为“TermuxPro 本地启动记录”。
- 记录详情与删除确认中的时间标签改为“本地记录时间”。
- 历史模式重复确认改为“再次打开这条 TermuxPro 本地启动记录”。
- 静态门禁新增空态、删除标题和本地记录时间检查。

## 验收标准

1. AI CLI 会话中心所有记录相关入口必须能让用户理解：这里管理的是 TermuxPro 本机记录，不是 Claude/Codex 远端历史。
2. 删除动作必须明确只删除本地记录，不删除远端 AI 历史、tmux 会话或终端输出。
3. 重复历史选择模式必须表达“再次打开入口”，不暗示自动恢复最近历史。
4. 不改变 SSH、tmux、终端 PTY、触摸滚动或 Termux 原始能力。

## 验证

- `test/ai-launch-decision-copy-test.sh`：通过；新增空态、删除标题和本地记录时间静态门禁。
- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `test/dialog-readable-style-test.sh`：通过。
- `rg` 扫描应用资源与 `AiCliSessionCenterActivityTest`：未发现残留 `最近 AI 启动`、`AI 启动记录`、
  `启动时间：`、`重复启动`、`Recent AI launches`、`AI launch record`、`Launched:` 或 `Repeat launch`。
- `./gradlew --offline --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：
  本地未执行到测试断言，原因是共享远程机 Android NDK `27.0.12077973` 未配置；不在共享机临时扩大
  SDK/NDK 安装，完整 Android 门禁交由 GitHub Runner 验收。
- `timeout --foreground 180s ./scripts/pre-push-smoke.sh`：前置脚本、版本元数据、GitHub workflow 策略、发布通知、
  噪声审计、上下文检查点、长期 Goal 规则、Android SDK 引导和资源守卫均通过；随后在 Gradle 阶段达到
  180 秒本地上限。未出现业务断言失败，完整 Android 门禁继续交由 GitHub Runner 验收。
