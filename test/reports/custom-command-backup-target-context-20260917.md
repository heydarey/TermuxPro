# 快捷指令备份与导入目标上下文验收（2026-09-17）

## 背景

快捷指令页已经在列表卡片和导入预览中展示当前工作区目标，但二级“备份与迁移 / 导入备份”弹窗仍只描述
本地配置和指令数量。多工作区、相似工作区名称或共享服务器场景下，用户在复制备份 JSON 或从剪贴板读取
备份前仍可能不知道当前操作会落到哪个远程目标。

## 增值服务准入

- 分类：自定义快捷指令、远程工作区、移动端工作流效率。
- 增值层：帮助手机用户在 SSH 远程开发场景下安全迁移和复用自定义命令。
- 非目标：不修改 Termux 原始终端、PTY、本地 shell、包管理或基础会话能力；不执行任何远端命令；不改变
  备份 JSON 协议和导入校验规则。

## 主动发现记录

| 候选 | 来源 | 影响旅程 | 结论 |
| --- | --- | --- | --- |
| 备份弹窗缺少当前目标 | 代码走查 `CustomCommandsActivity.showBackupActions` | 用户复制 JSON 前不知道备份属于哪台服务器和目录 | 本轮修复 |
| 空态导入弹窗缺少当前目标 | 空态旅程走查 | 新设备首次导入时可能误把备份导入相似工作区 | 本轮修复 |
| 导入预览已有目标但前一步没有 | Backlog 对照 | 用户要点进下一步才知道目标，反馈前置不足 | 本轮前移目标提示 |

## 改动

1. `custom_commands_backup_message` 增加当前 `host:port · path`。
2. `custom_commands_import_backup_message` 增加当前 `host:port · path`。
3. Robolectric 覆盖空态导入弹窗和有指令备份弹窗，确保都展示目标和“不执行远端命令”。

## 验收标准

- 空态点击“导入备份”时，弹窗显示当前目标。
- 已有指令点击“备份与迁移”时，弹窗显示当前目标和指令数量。
- 弹窗仍明确导入/备份只处理本地配置，不执行远端命令。
- 导出 JSON、导入预览、重名后缀、秘密检测和搜索清除逻辑保持不变。

## 回归面

- `CustomCommandsActivityTest.showsExplicitTargetAndEmptyCreationPath`
- `CustomCommandsActivityTest.exportsCurrentWorkspaceCommandsToClipboardWithoutExecuting`
- Android 字符串中英文 key 对齐与重复校验

## 本地验证记录

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.CustomCommandsActivityTest`：
  本地未进入测试执行阶段，Gradle 配置期从 `dl.google.com` 拉取 Android Gradle Plugin 超时。
- `./scripts/pre-push-smoke.sh`：静态门禁已通过；进入同一 Gradle 阶段后长时间无输出，为避免占用共享远程
  机器资源，人工中断。最终 Android/Robolectric 门禁以 GitHub CI 为准。
