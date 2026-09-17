# SSH 公钥确认上下文验收报告（2026-09-17）

## 结论

状态：本地实现与静态门禁完成；首次 GitHub CI 暴露 Robolectric 点击后未等待主线程队列导致启动 Intent 读取为 null，已补同步断言，等待 GitHub CI 复验。

本轮补强 SSH 公钥管理页的关键确认文案：

- 生成密钥前明确这是新的本地终端交互，并且密钥创建在 TermuxPro 应用私有 OpenSSH 目录中。
- 安装公钥前明确远端目标为 `host:port`，展示即将执行的 `ssh-copy-id` 命令，并说明服务器密码认证仍在
  OpenSSH 交互中完成，TermuxPro 不读取或保存服务器密码。

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 增值服务分类 | 处理结论 |
| --- | --- | --- | --- | --- |
| SSH 公钥安装确认页只展示 host，不展示端口；多 SSH 端口或 SSH config 别名场景下，用户无法在确认前核对目标。 | 负责人主动走查 `SshKeysActivity.confirmInstall()`。 | 工作区 → 工具箱 → SSH 密钥 → 安装到服务器。 | SSH 远程开发、移动端交互体验。 | 本轮处理。 |
| SSH 密钥生成确认页只说“新终端”，未明确本地应用私有目录，用户可能误以为会操作远端服务器或被应用读取私钥。 | UI/安全走查 `SshKeysActivity.confirmGenerate()`。 | 首次 SSH 配置 → 生成 Ed25519 密钥。 | SSH 远程开发、安全边界。 | 本轮处理。 |
| SSH 密钥页底部两个次级按钮在普通字体并排；若后续出现目标摘要，需关注 200% 字体布局。 | 既有大字体适配代码和 UX 巡检。 | 大字体用户复制/安装公钥。 | 移动端交互体验。 | 暂缓；当前已有大字体纵向按钮保护，本轮只补确认上下文。 |

## 变更范围

- `SshKeysActivity`
  - 安装确认文案改为传入 `host`、`port` 和完整命令。
- 中英文资源：
  - `ssh_keys_generate_message`
  - `ssh_keys_install_message`
- `SshKeysNavigationTest`
  - 新增确认弹窗测试，覆盖生成密钥本地边界、安装目标 `host:port`、完整命令和服务器密码不保存说明。
  - 修复 GitHub CI 中点击确认按钮后未等待主线程队列导致 `getNextStartedActivity()` 偶发/稳定返回 null 的测试同步问题。

## 非目标

- 不读取、不展示、不复制私钥。
- 不自动接受服务器指纹。
- 不自动输入服务器密码或密钥口令。
- 不改变 `ssh-keygen` / `ssh-copy-id` 命令生成逻辑。
- 不触碰 Termux 原始终端、PTY、本地 shell 或基础会话行为。

## 验收标准

1. 生成密钥确认页明确本地终端、应用私有 OpenSSH 目录和不保存口令。
2. 安装公钥确认页展示 `host:port` 与完整 `ssh-copy-id` 命令。
3. 安装确认页说明认证仍在 OpenSSH 中完成，TermuxPro 不保存服务器密码。
4. 点击确认后仍只启动新的终端会话执行原有安全命令。
5. 中英文字符串 key 保持一致。

## 本地验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `source scripts/resolve-jdk17.sh && ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.SshKeysNavigationTest.keyConfirmDialogsShowLocalAndRemoteBoundaries`：
  未进入测试阶段；Gradle 配置阶段读取 `https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/8.13.2/gradle-8.13.2.pom`
  超时，错误为 `Read timed out`。
- `./scripts/pre-push-smoke.sh`：静态门禁全部通过；进入自动补跑 Gradle 阶段后在共享远程机连续约 90 秒无输出，
  按资源守卫中断，退出码 130。完整 Android/Robolectric 验证交由 GitHub CI。
- GitHub CI run `35221877646`：首次失败，`SshKeysNavigationTest.keyConfirmDialogsShowLocalAndRemoteBoundaries`
  在确认按钮点击后立刻读取启动 Intent，未等待 Robolectric 主线程队列，导致 `generateIntent` 为 null。
- 修复后复跑 `source scripts/resolve-jdk17.sh && timeout 180 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.SshKeysNavigationTest.keyConfirmDialogsShowLocalAndRemoteBoundaries`：
  仍未进入测试阶段；Gradle 配置阶段访问 `dl.google.com` 超时，错误为 `Connect to dl.google.com:443 failed: Read timed out`。
- 修复后 `./test/android-string-resource-parity-test.sh && ./scripts/validate-skills.sh && git diff --check`：通过。
- 修复后 `timeout 180 ./scripts/pre-push-smoke.sh`：静态门禁、资源守卫均通过；进入 Gradle 阶段后 180 秒无进一步输出，
  按远程共享机资源策略超时中断，退出码 124。完整 Android/Robolectric 验证继续以 GitHub CI 为准。

## 复盘

- 减少的真实负担：用户安装公钥前能直接确认服务器端口和命令，不需要回到工作区或终端里推断目标。
- 新增控件/流程噪声：无新增入口，只增强已有确认弹窗。
- Termux 原始能力影响：仅更新 TermuxPro 增值页文案和测试，原始终端/PTY 行为不变。
- 下一轮建议检查：继续审计 SSH 首次连接、诊断、SSH Key、工作区保存这些“认证/目标”相关页面的目标边界是否一致。
