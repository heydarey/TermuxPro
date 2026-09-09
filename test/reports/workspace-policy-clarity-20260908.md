# 工作区 tmux 连接方式清晰度回归（2026-09-08）

## 增值服务准入

- 分类：远程工作区、tmux 可视化、移动端工作流效率。
- 非目标：不重做 Termux 原始终端、PTY、本地 shell、包管理或基础会话能力。

## 用户问题

共享服务器和共享 Claude Code 账号场景下，连接后自动进入 tmux 存在误入他人或错误项目会话的风险。
原工作区配置把连接策略放在“高级设置”里，用户很难在连接前判断 TermuxPro 会只建立 SSH、列出 tmux，
还是进入指定 tmux 会话。

## 本轮改动

- 将工作区配置入口文案从“高级设置”改为“tmux 连接方式”。
- 展开后增加常驻标题“连接后如何处理 tmux”。
- 展开后增加安全提示：默认只建立 SSH；只有用户明确选择并填写会话名时才进入指定 tmux；共享账号不要
  自动进入未知会话。
- 默认策略保持“仅 SSH（推荐）”，不改变既有命令构造和连接行为。

## 验收结论

- 首次配置页默认不展示 tmux 策略选择器，避免把低频策略推到首页。
- 用户主动展开后可以先看到策略语义和风险说明，再选择是否进入指定会话。
- 该切片只增强远程工作区/tmux 增值层的决策清晰度，不影响 Termux 原始终端滚动、输入、PTY 或会话。

## 已执行验证

```text
./scripts/resource-guard.sh
./scripts/validate-skills.sh
./test/github-cli-wrapper-test.sh
./test/github-check-suites-test.sh
source ./scripts/resolve-jdk17.sh
TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.WorkspaceActivitySmokeTest
```

结果：全部通过。

## 未覆盖

- 本地共享服务器 KVM 不可用，未在本机启动 Android 模拟器。
- 360dp 默认/200% 字体截图矩阵和运行时触摸验收需交给 GitHub Runner 或后续隔离设备执行。
