# 终端会话反馈可读性验收记录（2026-09-19）

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| 会话切换反馈虽然已改为页面内横幅，但兜底标题仍可能只显示 `[1]`，用户不知道切到了哪个会话。 | 负责人代码走查 `TermuxTerminalSessionActivityClient.formatToastTitle` | 终端侧栏切换 SSH / Claude / Codex 会话 | 移动端交互体验、AI CLI 使用体验 | P1 | 本轮处理：兜底改为“会话 1 / Session 1”，避免暴露技术编号。 |
| 会话名或终端标题如果包含换行、Tab 或首尾空白，反馈横幅可能看起来像空提示或被撑开。 | QA 走查标题格式化函数 | 会话切换、后台会话提醒、会话结束提示 | 移动端交互体验、可读反馈 | P1 | 本轮处理：统一折叠空白并限制单段长度。 |
| 反馈标题如果只有会话标题没有序号，用户从侧栏切换多个相似 SSH 会话时缺少定位线索。 | UI/UX 复核终端会话列表语义 | 多 SSH / 多 AI CLI 会话切换 | 终端增值体验、远程开发效率 | P2 | 本轮保留序号，但以自然语言展示，并把名称和标题分层显示。 |

## 本轮范围

- 将终端前台反馈中的会话标题从 `[1]` 这类内部编号改为“会话 1 / Session 1”。
- 有会话名时展示为“会话 5 · ssh-153”，有终端标题时换行展示标题，例如 `codex`。
- 折叠会话名和标题中的换行、Tab、重复空格，并对单段超长文本做省略，避免横幅像空白 Toast 或撑满屏幕。

## 非目标

- 不改变 Termux 原始终端会话切换逻辑。
- 不关闭现有会话切换提示开关。
- 不读取远端 tmux、Claude 或 Codex 私有历史来推断标题。

## 验收

- `TermuxTerminalSessionActivityClientTest.toastTitleAlwaysContainsSessionIndexEvenWithoutNameOrTitle`
  - 验证空会话名/空标题时仍显示可读兜底。
- `TermuxTerminalSessionActivityClientTest.toastTitleKeepsNameAndTerminalTitleReadable`
  - 验证会话名与终端标题分层可读。
- `TermuxTerminalSessionActivityClientTest.toastTitleCleansBlankLinesAndVeryLongLabels`
  - 验证异常空白和超长标题不会制造空白或过长反馈。
- 本地验证：
  - `git diff --check` 通过。
  - `./test/android-string-resource-parity-test.sh` 通过。
  - `./scripts/validate-skills.sh` 通过。
  - 目标 Gradle 单测在本机远程环境中两次未完成：直连 Google Maven 因 `dl.google.com` 读取超时失败；
    切到 `TERMUXPRO_USE_CHINA_MIRROR=1` 后仍长时间无输出，已为保护共享机器中断。完整 Android 单测与
    APK 构建交由 GitHub CI 门禁验证，不把本地 Gradle 结果标记为通过。

## 结论

这次改动继续补齐“终端页反馈不能只是功能存在，还要让用户一眼知道发生了什么”。它只作用于
TermuxPro 前台反馈文案，不影响原始终端输入、滚动、PTY 或远端会话。
