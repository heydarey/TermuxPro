# 快捷指令模板目标上下文验收记录

## 增值服务准入

- 分类：自定义快捷指令、远程工作区、移动端工作流效率。
- 增值层：帮助手机端开发者从 Codex、Claude、Git、tmux 和测试模板快速沉淀当前远程工作区的常用命令。
- 非目标：不改变 Termux 原始终端、PTY、本地 shell、基础会话或基础快捷键；不执行任何远端命令。

## 主动发现候选

| 候选观察 | 来源 | 影响旅程 | 本轮结论 |
|---|---|---|---|
| 模板选择弹窗只提示“选择模板不会执行”，未展示当前 `host:port · path`。多工作区或共享服务器下，用户选模板前仍要凭记忆判断目标。 | 负责人主动走查 `CustomCommandsActivity.showTemplates()` | 手机 SSH 工作区 → 快捷指令 → 选 Codex/Claude/Git/tmux 模板 | 本轮处理 |
| 模板列表仍是单层长列表，AI/Git/tmux/测试混排；当前只有 8 项还能接受，但后续模板增加后需要分组或二级选择。 | UI/UX 走查模板弹窗 | 首次创建快捷指令、降低手机扫读成本 | 暂缓：先补目标上下文，不增加新控件；后续结合模板数量扩展 |
| 旧的 `custom_commands_template_message` 仍可保留为无目标兜底，但当前有效工作区路径应优先使用带目标文案。 | 架构/资源走查 | 无效工作区按钮已禁用，正常路径应给强上下文 | 本轮处理带目标路径，保留兼容文案 |

## 本轮改动

- `showTemplates()` 的说明从通用文案改为带当前目标：
  - `当前目标：host:port · path`
  - 保存前不会落盘。
  - 选择模板不会执行命令。
- 模板列表行为不变：点击模板只预填编辑器，不保存、不启动 SSH、不向当前终端或 AI TUI 输入命令。
- 补充 Robolectric 断言，覆盖模板弹窗展示当前目标和安全边界。

## 验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `timeout --foreground 180s ./scripts/pre-push-smoke.sh origin/dev`：静态门禁、资源守卫和 Android 字符串/Skill/Workflow/Goal/GitHub 噪声等脚本均通过；进入 Gradle 阶段后 180 秒未完成，按共享远程低资源策略中止。
- `timeout 120s ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.CustomCommandsActivityTest`：本机共享远程 120 秒内未完成，按资源策略中止；完整 JVM/Android 门禁交由 GitHub CI 验证。

## 后续检查

- 当模板数量继续增加时，把模板弹窗升级为按场景分组或搜索，而不是继续堆单层列表。
- 继续检查快捷指令运行、导入、复制和模板编辑路径是否都能在最后一步看到当前工作区目标。
