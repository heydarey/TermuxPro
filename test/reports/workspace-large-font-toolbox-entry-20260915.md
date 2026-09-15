# 工作区大字体工具箱入口可见性验收记录

## 背景

- 来源：负责人继续复核 PR #360 的模拟器证据 `termuxpro-emulator-ui-381`。
- 增值服务分类：远程工作区、AI CLI、上下文工具箱、移动端交互体验。
- 不是 Termux 原始能力重做：不修改 PTY、本地 shell、包管理、终端滚动、OpenSSH 或 tmux 命令链，
  只优化 TermuxPro 工作区首页到增值工具箱的入口层级。

## 主动发现记录

1. 产品经理观察：200% 字体下已配置工作区的首屏已经能看到远程终端和 Claude/Codex，但工具箱入口仍需要
   继续滚动一点；用户要进入 Git、tmux 管理、快捷指令或远端文件时，需要先判断“下面还有没有入口”。
2. UI/UX 观察：`AI CLI 快速启动` 标题在大字体下信息冗余，因为 Claude/Codex 按钮本身已经表达了 AI
   行为；保留短标题即可减少垂直占用。
3. QA 观察：工具箱默认折叠是正确方向，不能为了露出入口重新把工具按钮堆回首页；本轮只压缩入口，
   不改变展开后的 CRUD、确认和只读/写入边界。

## 本轮处理

- 仅在 `fontScale >= 1.5` 时启用：
  - AI 区块标题从“AI CLI 快速启动”收敛为“AI CLI”；
  - Claude/Codex 按钮保持双列，文字仍为“Claude / Codex”，按钮高度压缩到 48dp 触控底线；
  - 工具箱按钮高度压缩到 48dp，文案收敛为“工具箱 / 收起工具箱”；
  - 工具箱按钮的完整能力说明继续保留在 `contentDescription`，避免视觉压缩影响 TalkBack。
- 默认字体不改变原文案和布局，继续显示“AI CLI 快速启动”和“打开开发工具箱 / 收起开发工具箱”。

## 本地验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- 定向 Robolectric：当前共享机项目级 SDK 缺少 NDK `27.0.12077973`，Gradle 配置阶段失败关闭；
  需要远端 Actions 完成 JVM/Robolectric、Lint/APK 和模拟器 UI 门禁。

## 远端验收要求

推送后必须等待：

- 分支 `TermuxPro CI` 通过；
- 分支 `TermuxPro Emulator UI` 通过，并人工复核 `font200/workspace-connection-verified-font200.png`；
- 自动 PR 控制器合入 `dev` 后，`dev` 收尾 CI 通过；
- `dev_dailyIteration` 对齐空检查成功。

## 非目标

- 不改变工作区保存、删除、SSH 连接、AI CLI 启动确认、tmux 策略或工具箱展开后的具体工具行为。
- 不启动本机模拟器，不安装全局 Android SDK/NDK。
