# 快捷指令列表目标上下文验收记录（2026-09-15）

## 主动发现候选

| 候选观察 | 来源 | 影响旅程 | 分类 | 优先级 | 本轮结论 |
|---|---|---|---|---|---|
| 快捷指令列表每条卡片只展示场景和目录，未常驻展示 SSH 目标；用户在手机上滑动列表时需要点开“查看并运行”才能确认会打到哪台服务器。 | 负责人主动走查 `CustomCommandsActivity` 与列表布局 | 进入远程终端后从工具箱运行 Codex/Git/测试快捷指令 | 自定义快捷指令、远程工作区、移动端工作流效率 | P1 | 本轮处理 |
| 备份与迁移入口在空列表也常驻显示，首次创建路径存在一定视觉竞争。 | 负责人主动走查空态 | 首次创建快捷指令 | 自定义快捷指令、移动端交互体验 | P2 | 暂缓：当前可导入已有配置，且入口低于主按钮；后续结合信息架构统一评审 |
| Backlog 中多项“进行中”长期挂起，可能降低负责人判断下一步的效率。 | 负责人维护审计 | 日常迭代排期和发布评审 | 交付质量、可维护性 | P2 | 暂缓：作为治理切片处理，不打断本轮用户可见体验改进 |

## 问题

快捷指令是 TermuxPro 的增值服务，用来降低手机软键盘输入成本。但列表卡片之前只显示：

- 场景；
- 指令目录；
- 命令文本；

缺少当前 SSH 目标。多工作区或共享服务器场景下，用户需要进入二次确认弹窗才能确认目标，增加误执行风险。

## 本轮调整

- 快捷指令卡片摘要改为常驻展示：
  - 场景；
  - 工作目录或“工作区目录”；
  - SSH 目标 `host:port`。
- 不新增按钮，不改变执行策略：
  - `ALWAYS` 仍需确认；
  - 安全命令在 `DANGEROUS_ONLY` 下仍可直接运行；
  - 真正执行前仍通过 `WorkspaceCommandBuilder` 创建新的远程终端会话。

## 验收口径

- 用户在列表页无需点开弹窗，就能知道快捷指令属于哪个场景、哪个目录、哪台 SSH 目标。
- 改动属于 TermuxPro 增值层，不改变 Termux 原始终端、PTY、本地 shell 或包管理能力。
- 不保存、不展示密码、私钥、Token 或终端输出。
- 大字体下该摘要允许最多两行，仍优先保留主操作和命令文本。

## 验证

- `CustomCommandsActivityTest` 增加卡片摘要断言，覆盖默认工作区目录和 SSH 目标。
- 本地低资源验证：
  - `test/workflow-trigger-policy-test.sh`：通过。
  - `git diff --check`：通过。
  - `./scripts/validate-skills.sh`：通过。
  - `test/dialog-readable-style-test.sh`：通过。
  - `test/ai-launch-decision-copy-test.sh`：通过。
  - `./gradlew --offline --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.CustomCommandsActivityTest`：
    本机配置阶段失败，原因是共享远程机未配置 Android NDK `27.0.12077973`；按项目红线不在共享机安装全局 NDK。
  - `timeout --foreground 180s ./scripts/pre-push-smoke.sh`：脚本、版本、workflow、通知格式、GitHub 噪声、
    GitHub CLI、上下文检查点、Goal 生命周期、缓存清理、SDK 引导和资源守卫均通过；进入 Gradle 阶段后
    达到 180 秒本地上限，无业务断言失败。
- 完整 Android 测试、Lint、APK 和模拟器截图验收以后续 GitHub Runner 门禁为准。
