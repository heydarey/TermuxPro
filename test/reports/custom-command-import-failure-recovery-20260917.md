# 快捷指令导入失败可恢复性回归

## 背景

快捷指令是 TermuxPro 针对手机端远程开发的增值服务：用户可以把 Git、tmux、AI CLI、测试等常用命令
按工作区保存，减少软键盘输入成本。备份/导入用于跨设备迁移这些本地配置，不应让用户误以为会执行
远端命令，也不应在失败时只给抽象错误。

## 主动观察

1. 剪贴板为空时，旧反馈只说明“没有可导入 JSON”，没有告诉用户下一步应先复制 TermuxPro 备份。
2. JSON 无效或包含疑似敏感命令时，旧反馈没有提醒常见原因：不是由“复制备份 JSON”生成，或含密码、
   Token、私钥、内嵌凭据。
3. 两类失败都没有在反馈里重新展示当前 `host:port · path`，用户在多工作区场景下难以确认自己正在
   给哪个目标导入。

## 改动范围

- 导入空剪贴板和导入无效/敏感 JSON 时，反馈展示当前目标、恢复动作和安全边界。
- 明确失败不会保存任何快捷指令，也不会执行远端命令。
- 补 Robolectric 回归，覆盖空剪贴板与敏感命令失败路径。

## 非目标

- 不改变备份 JSON 协议。
- 不放宽秘密/危险命令校验。
- 不执行任何远端命令。
- 不读取或保存剪贴板中的无效内容。

## 验收标准

- 用户能从失败反馈知道下一步该复制有效 TermuxPro 备份。
- 无效/敏感内容失败后本地快捷指令数量不变。
- 失败路径不会启动终端、不保存配置、不执行远端命令。

## 本地验证

- `git diff --check`：通过。
- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./scripts/release-window-guard.sh`：通过，当前稳定版 `v0.10.1` 未到下一次发布复评窗口。
- `TERMUXPRO_DRY_RUN=1 ./scripts/pre-push-smoke.sh origin/dev`：通过；映射到
  `CustomCommandsActivityTest`、`CustomCommandStoreTest`、`WorkspaceCommandBuilderTest`、
  `AiCliSessionCenterActivityTest`、`WorkspaceActivitySmokeTest` 和 `CustomLayoutsSmokeTest`。
- `:app:testDebugUnitTest --tests com.termux.app.CustomCommandsActivityTest`：本共享远程环境 300 秒内无输出并
  被 timeout 停止；未继续重试以避免占用 CPU/内存，完整 Robolectric 结果以 GitHub CI 为准。
