# AI CLI 本地启动记录重复打开确认（2026-09-17）

## 背景

用户高频场景是手机 SSH 到远程服务器后使用 Claude Code 和 Codex CLI。AI CLI 会话中心已经把历史入口改为
“CLI 原生选择器、不自动恢复”，但主动走查发现：当用户点击 TermuxPro 本地启动记录的“再次打开”时，
`历史选择` 会二次确认，而 `新建会话` 会直接新开 SSH 终端并执行 `claude` 或 `codex`。

这不属于 Termux 原始终端能力，而是 TermuxPro 增值层中的 AI CLI 启动编排与误触防护。

## 主动发现记录

| 候选观察 | 来源 | 影响旅程 | 增值服务分类 | 结论 |
| --- | --- | --- | --- | --- |
| “再次打开：Claude Code · 新建会话”直接启动，手机误触会立即污染共享 Claude 账号上下文。 | 负责人主动代码走查 `AiCliSessionCenterActivity.launchAiCliWithModeGuard` | AI CLI 本地记录恢复 | AI CLI、移动端工作流效率 | 本轮修复：新建会话记录也必须确认后再打开。 |
| “查看全部记录”选中一条新建会话后同样走直接启动路径，绕过最近记录按钮的文案风险提示。 | QA 走查管理弹窗路径 | AI CLI 本地记录 CRUD | AI CLI、移动端交互体验 | 本轮补测试：管理弹窗选择新建会话记录也先弹确认。 |
| 确认页需要同时说明目标、命令、记录时间和不会自动进入 tmux，否则共享账号用户仍要靠记忆判断是否安全。 | UI/UX 风险复核 | 共享服务器/共享 Claude 账号 | AI CLI、远程工作区 | 本轮新增确认文案，包含 `host:port · path`、命令、记录时间、tmux 与远端历史边界。 |

## 改动范围

- `AiCliSessionCenterActivity`
  - `PICK_HISTORY` 仍走历史选择确认。
  - `NEW_SESSION` 本地记录重复打开新增确认弹窗。
  - 目标不合法时仍回到工作区配置，不执行命令。
- `strings.xml` / `values-zh-rCN/strings.xml`
  - 新增重复打开确认标题、说明和按钮文案。
- `AiCliSessionCenterActivityTest`
  - 覆盖最近记录重复打开确认。
  - 覆盖管理弹窗中选择新建会话记录后的确认、取消和确认启动。

## 非目标

- 不读取 Claude/Codex 私有历史。
- 不改变直接点击“新开 SSH 跑 Claude/Codex”的显式启动行为。
- 不改变 Termux 原始终端、PTY、shell、基础会话或滚动行为。
- 不自动进入 tmux、不自动恢复远端历史、不删除远端 AI 数据。

## 验收标准

1. 点击重复本地新建会话记录时，不应立即启动 `TermuxActivity`。
2. 确认弹窗必须展示工具、模式、记录时间、目标和即将执行的命令。
3. 取消后不启动任何终端。
4. 确认后新开 SSH 终端并执行原记录对应的 AI CLI 命令。
5. 历史选择记录继续保留已有“打开历史选择”确认。
6. 工作区目标变化时继续阻止重放旧记录。

## 验证

- 通过：`./test/android-string-resource-parity-test.sh`
- 通过：`git diff --check`
- 通过：`bash -n scripts/ensure-android-sdk.sh`
- 通过：`python3 -m py_compile scripts/install-android-sdk-mirror-packages.py`
- 通过：`./scripts/install-android-sdk-mirror-packages.py --help >/dev/null`
- 未完成：`./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`
  - 原因：本机项目级 SDK 缺少 `ndk;27.0.12077973`，`dl.google.com` 在当前环境解析失败；
    腾讯 AndroidSDK 镜像可用但 NDK 大包下载速度不稳定，本轮为避免长期占用共享服务器会话已暂停本地
    NDK 下载。
  - 失败证据：`source scripts/resolve-jdk17.sh && timeout 120 ./gradlew --offline --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`
    在 7 秒内失败，Gradle 明确报告 `NDK not configured. ... Preferred NDK version is '27.0.12077973'.`
  - 已补救：`scripts/ensure-android-sdk.sh` 增加项目级镜像安装模式，新增
    `scripts/install-android-sdk-mirror-packages.py`，支持镜像索引、license 文件、大小/SHA-1 校验和
    `curl` 断点续传；后续可用
    `TERMUXPRO_ANDROID_SDK_MIRROR_BASE=https://mirrors.cloud.tencent.com/AndroidSDK ./scripts/ensure-android-sdk.sh`
    继续补齐本地 NDK。
  - 后续门禁：推送后等待 GitHub CI 的 Robolectric 回归结果；若 CI 暴露断言或编译问题，本切片不得合入
    `dev`。
- 部分通过：`timeout 180 ./scripts/pre-push-smoke.sh`
  - 已通过：Skills 校验、版本元数据、workflow 策略、发布通知格式、发布窗口守卫、GitHub 噪声审计、
    GitHub CLI 封装、check suite 诊断、上下文检查点、长期 Goal 生命周期、缓存清理、Android SDK
    引导脚本、字符串资源和资源守卫。
  - 未完成：Gradle 阶段 180 秒内无进一步输出，被超时保护终止；未发现残留 Gradle/SDK 下载进程。
  - 后续门禁：推送后必须等待 GitHub CI；若 CI 失败，本切片不得合入 `dev`。
