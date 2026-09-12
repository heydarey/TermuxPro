# AI 本地启动记录语义验收（2026-09-12）

## 背景

用户高频场景是在手机端 SSH 到远程服务器后启动 Claude Code 或 Codex CLI。AI CLI 会话中心已经能记录
TermuxPro 发起过的启动入口，但“最近 AI 启动 / 重复启动”容易被理解成 TermuxPro 正在读取或恢复
Claude/Codex 的真实历史，尤其在共享 Claude 账号下会增加误进入他人上下文的心理成本。

## 本轮主动发现

| 观察 | 影响旅程 | 分类 | 结论 |
| --- | --- | --- | --- |
| “最近 AI 启动”标题不够明确 | 用户可能误以为 App 在管理远端 AI 历史 | AI CLI、移动端交互体验 | 本轮修复 |
| “重复：工具 · 模式”按钮容易被理解为恢复历史 | 用户滚动后脱离上方说明，可能误触 | AI CLI、远程工作区 | 本轮修复 |
| 删除和清空确认已说明本地边界，但入口标题没有同等强度 | 同一页面安全语义不一致 | 移动端工作流效率 | 本轮修复 |

本轮不是重做 Termux 原始终端能力；只优化 TermuxPro 增值层对 AI CLI 启动记录的解释和可操作语义。

## 变更

- “最近 AI 启动”改为“TermuxPro 本地启动记录”。
- 记录区说明改为“这里不是 Claude/Codex 历史库”，明确只记录本机本 App 打开的启动入口。
- 记录项时间改为“本地记录时间”，避免被理解为远端 AI 会话时间。
- “重复”按钮改为“再次打开”，强调只是再次打开同一入口，不自动恢复远端历史。
- 管理弹窗、单条详情和清空标题同步使用“本地记录”语义。
- 静态门禁补充本地记录语义检查，防止后续退化。

## 验收标准

1. 记录区标题必须明确包含 TermuxPro 本地记录。
2. 记录区说明必须直接声明不是 Claude/Codex 历史库。
3. 重复按钮必须表达“再次打开入口”，不能暗示自动恢复或读取远端历史。
4. 删除、清空仍只影响 TermuxPro 本地记录，不触碰远端 AI 历史、tmux 会话或终端输出。
5. 不改变 Termux 原始终端、PTY、滚动或本地 shell 行为。

## 验证

- `test/ai-launch-decision-copy-test.sh`：通过，新增本地记录语义静态门禁。
- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `test/dialog-readable-style-test.sh`：通过。
- `./gradlew --offline --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：
  本地未执行到测试断言，原因是共享远程机 Android NDK `27.0.12077973` 未配置；依既有规则不在本机临时扩大
  SDK/NDK 安装，后续以 GitHub Runner 的 Android CI 与模拟器门禁作为运行时验收。
- `timeout --foreground 180s ./scripts/pre-push-smoke.sh`：前置脚本、版本元数据、GitHub 工作流策略、发布通知、
  GitHub 噪声审计、上下文检查点、长期 Goal 规则、Android SDK 引导和资源守卫均通过；随后在 Gradle 阶段
  达到 180 秒本地上限，未出现业务断言失败。完整 Android 门禁继续交由 GitHub Runner 验收。
- GitHub CI 首次运行暴露 `AiCliSessionCenterActivityTest` 的中文断言与实际文案空格不一致；已修正断言，
  不改变产品文案和运行逻辑。
