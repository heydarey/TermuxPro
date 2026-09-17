# AI CLI 本地记录目标变更防误启动

状态：`READY FOR DEV MERGE`。

## 增值服务准入

- 分类：AI CLI、远程工作区、移动端交互体验。
- 非目标：不读取 Claude/Codex 私有历史，不删除远端 AI 历史，不进入或管理远端 tmux，不改变 Termux 原始终端/PTY/本地 shell 行为。
- 用户价值：手机端经常在多个服务器/目录间切换，旧的 TermuxPro 本地启动记录如果被重放到新的服务器或项目，会把 Claude/Codex 开到错误上下文，尤其共享 Claude 账号下风险更高。

## 主动发现候选

| 候选观察 | 证据来源 | 影响旅程 | 分类 | 结论 |
|---|---|---|---|---|
| AI CLI 会话中心的本地启动记录按工作区 ID 读取，但“重复上次”实际使用当前工作区启动命令；若同一工作区被编辑到新服务器/目录，旧记录和当前目标可能不一致。 | 负责人代码走查 `AiCliSessionCenterActivity.launchAiCliWithModeGuard()` 与 `AiLaunchHistoryStore.record()`。 | 多服务器 SSH 开发、重复打开 Codex/Claude。 | AI CLI、远程工作区 | 本轮推进。 |
| 新建会话记录重复打开没有确认弹窗，目标不一致时比历史选择器风险更高。 | 产品旅程复盘：用户以为“重复上次”会回到记录里的旧项目。 | 继续上次 AI 工作。 | AI CLI、移动端交互体验 | 本轮优先覆盖。 |
| 可以把旧记录按条删除，但目标变更时缺少“检查工作区/删除过期记录”的恢复路径。 | UI/UX 走查本地记录 CRUD。 | 切换服务器后清理本地记录。 | 移动端交互体验 | 本轮补齐闭环。 |

## 改动

- 重放本地 AI CLI 启动记录前，比对记录中的 `workspaceId + host + port + path` 和当前活动工作区。
- 目标不一致时不启动终端，弹出“工作区目标已变化”说明：
  - 展示记录目标和当前目标；
  - 提供“检查工作区”；
  - 提供“删除本地记录”；
  - 明确不会把旧记录重放到另一个服务器或项目。
- 历史选择记录和新建会话记录共用同一防误启动逻辑。

## 验收

- 已通过：
  - `bash test/android-string-resource-parity-test.sh`
  - `./scripts/validate-skills.sh`
- 已补测试：
  - `AiCliSessionCenterActivityTest.blocksRepeatingLocalRecordWhenWorkspaceTargetChanged`
- 本机受限：
  - 首次执行 `:app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest` 时，Gradle 配置阶段访问 `dl.google.com` 下载 AGP `8.13.2` 超时。
  - 使用项目级 `TERMUXPRO_USE_CHINA_MIRROR=1` 重试后，单测试类在共享服务器上超过 6 分钟无输出；为避免占用共享机，已停止本地 Gradle。
  - Android/Robolectric、Lint 和 APK 构建等待 GitHub CI 验证，不伪造本地运行时通过。

## 回归面

- AI CLI 会话中心本地记录列表、重复打开、删除单条、清空当前工作区记录。
- 多工作区/同工作区目标编辑后，旧记录不能误启动到新目标。
- 目标一致时，既有重复打开和历史选择确认行为保持不变。
