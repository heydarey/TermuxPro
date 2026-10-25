# AI 本地启动记录过期阻断弹窗时间上下文验收（2026-09-17）

## 背景

AI CLI 会话中心已经在本地启动记录列表中标记“目标已变化”，并在重复过期记录时阻断启动，要求用户检查工作区
或删除本地记录。但阻断弹窗只展示记录目标和当前目标，没有展示这条记录的本地记录时间。对于连续多次打开
Claude/Codex、共享账号或相似项目目录的场景，用户仍需要靠记忆判断是哪一次启动留下了过期记录。

## 增值服务准入

- 分类：AI CLI、远程工作区、移动端交互体验。
- 增值层：帮助手机用户安全处理 TermuxPro 本地 AI 启动记录，避免把旧记录重放到错误 SSH 目标。
- 非目标：不读取 Claude/Codex 远端历史，不读取终端输出，不自动恢复历史，不自动进入 tmux，不修改 Termux
  原始终端、PTY、本地 shell 或包管理能力。

## 主动发现记录

| 候选 | 来源 | 影响旅程 | 结论 |
| --- | --- | --- | --- |
| 过期记录阻断弹窗缺少本地记录时间 | 代码走查 `showHistoryTargetChangedDialog` | 用户不知道要删除的是哪一次启动记录 | 本轮修复 |
| 重复历史确认已有时间，但过期阻断没有 | Backlog 对照 | 同一条历史安全链路反馈不一致 | 本轮统一 |
| 多工作区目标变化只能靠目标判断 | 测试用例走查 | 相似服务器/目录下删除判断成本高 | 本轮降低判断成本 |

## 改动

1. `ai_cli_center_history_target_changed_message` 增加“本地记录时间 / Local record time”。
2. `showHistoryTargetChangedDialog` 传入 `formatLaunchTime(entry.launchedAtMillis)`。
3. Robolectric 断言过期记录弹窗包含“本地记录时间”。

## 验收标准

- 重复过期 AI 本地启动记录时，不启动 Termux 终端。
- 弹窗展示工具、模式、本地记录时间、记录目标和当前目标。
- 弹窗仍只提供“检查工作区 / 删除本地记录”，不会自动恢复 AI 历史或进入 tmux。
- 删除只影响当前工作区 TermuxPro 本地启动记录，不删除 Claude/Codex 远端历史、tmux 会话或终端输出。

## 回归面

- `AiCliSessionCenterActivityTest.blocksRepeatingLocalRecordWhenWorkspaceTargetChanged`
- Android 字符串中英文 key 对齐与重复校验

## 本地验证记录

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest.blocksRepeatingLocalRecordWhenWorkspaceTargetChanged`：
  本地未进入测试执行阶段，Gradle 配置期从 `dl.google.com` 拉取 Android Gradle Plugin 超时。最终
  Robolectric/Android 门禁以 GitHub CI 为准。
- `./scripts/pre-push-smoke.sh`：静态门禁已通过；进入同一 Gradle 阶段后长时间无输出，为避免占用共享远程
  机器资源，人工中断。最终 Android/Robolectric 门禁以 GitHub CI 为准。
