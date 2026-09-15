# 工作区大字体首屏信息层级验收记录

## 背景

- 来源：负责人主动走查 PR #359 的模拟器证据 `termuxpro-emulator-ui-375`。
- 增值服务分类：远程工作区、AI CLI、上下文工具箱、移动端交互体验。
- 不是 Termux 原始能力重做：不修改 PTY、本地 shell、包管理、终端滚动或 OpenSSH 行为，只调整
  TermuxPro 工作区首页的信息层级。

## 发现

1. `workspace-connection-verified-font200.png` 显示已配置工作区在 200% 字体下，连接摘要占用较高，
   首屏底部只露出部分 Claude 入口。
2. 用户打开首页的高频目标是继续 SSH、启动 Claude/Codex 或按需打开工具箱；过长摘要会把高频动作
   推到下方，增加滚动负担。
3. 该问题不是功能缺失，但会让大字体用户误以为首页仍然“堆控件/找入口”，属于真实移动使用体验缺陷。

## 本轮处理

- 仅在 `fontScale >= 1.5` 时启用紧凑模式：
  - 首页标题、连接卡标题和摘要文字降低一级字号并限制行数；
  - 已保存工作区标题从“服务器与项目”收敛为“当前连接”；
  - 摘要改为“工作区名 · host:port”和“path · 验证状态”；
  - tmux 策略改为短句，例如“tmux：不自动进入”。
- PR #360 首次模拟器截图复核发现，摘要已变短但 AI 按钮仍因大字体纵向堆叠，工具箱入口露出不够早；
  因此继续驳回并修正为：工作区首页大字体下 AI 快捷入口保留双列，按钮短标签为“Claude / Codex”，
  完整安全说明继续保存在 `contentDescription`。
- PR #360 CI 首次失败暴露测试场景不严谨：测试保存了工作区但未写入 VERIFIED 连接事实，却断言 AI
  快捷入口可见。已修正为显式写入已验证连接事实后再断言首屏入口，保持“未验证不展示 AI 快捷入口”
  的安全门槛不变；因此紧凑摘要状态应为“最近验证”。
- 随后检查发现 CI/Release/模拟器 workflow 仍安装 NDK `29.0.14206865`，而项目版本源和 AGP 实际提示
  已统一到 `27.0.12077973`；已同步 GitHub Actions，避免 PR、Release 与本地 bootstrap 使用不同 NDK。
- 普通字体仍保留完整卡片标题、完整 host/port/path/status 和完整 tmux 策略说明。
- 无障碍描述仍保留完整目标与路径，避免为了视觉压缩牺牲辅助技术信息。

## 本地验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./test/android-sdk-bootstrap-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `TERMUXPRO_OFFLINE=1 ./scripts/pre-push-smoke.sh`：静态门禁全部通过，Gradle 段在配置阶段失败关闭。
- 定向 Robolectric：当前共享机无法完成，原因：
  - 在线解析 Android Gradle Plugin 时 `dl.google.com` 连续读超时；
  - 离线配置阶段发现项目级 SDK 缺少 NDK `27.0.12077973`；
  - 已把 README、开发文档、bootstrap、doctor、Android SDK 引导脚本和对应测试统一到
    `27.0.12077973`，避免后续机器按旧版本准备环境。

## 远端验收要求

推送后必须等待：

- 分支 `TermuxPro CI` 通过；
- 分支 `TermuxPro Emulator UI` 通过，并人工复核 `workspace-connection-verified-font200.png`；
- 自动 PR 控制器合入 `dev` 后，`dev` 收尾 CI 通过；
- `dev_dailyIteration` 对齐空检查成功。

## 非目标

- 不改变 SSH 连接命令、tmux 策略、AI CLI 启动模式或工作区 CRUD。
- 不在共享服务器安装全局 Android SDK/NDK、模拟器或系统包。
