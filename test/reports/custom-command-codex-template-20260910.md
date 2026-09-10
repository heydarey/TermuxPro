# 自定义快捷指令 Codex 模板与复制文案审计

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| Codex 快捷指令模板只有历史恢复，没有新建独立会话。 | 负责人主动走查 `CustomCommandsActivity` 模板列表 | 手机远程工作区 → 快捷指令 → 启动 Codex | 自定义快捷指令、AI CLI | P1 | 本轮处理。用户高频使用 Codex CLI，模板能力应与 Claude 保持新建/历史对称。 |
| 管理菜单“复制”含义不清，实际是复制为一条新快捷指令。 | 负责人主动走查管理菜单文案 | 快捷指令 CRUD → 复制/复用命令 | 自定义快捷指令、移动端交互 | P1 | 本轮处理。改为“复制为新指令”，降低误解为复制文本到剪贴板的概率。 |
| 模板列表把名称和完整命令放在同一弹窗列表项里，命令较长时手机扫描负担偏高。 | 负责人主动走查模板弹窗 | 快捷指令首次创建 | 自定义快捷指令、移动端交互 | P2 | 暂缓。需要设计更完整的模板分类/搜索，不应在本轮小切片里继续堆控件。 |

## 本轮范围

- 新增“Codex：新建独立会话 / Codex: new isolated session”模板，命令为 `codex`。
- 将原 Codex 历史模板文案调整为“Codex：选择历史会话 / Codex: choose a past session”，命令仍为
  `codex resume`。
- 将快捷指令管理菜单的“复制 / Copy”改为“复制为新指令 / Duplicate as new command”。

## 非目标

- 不读取 Codex 或 Claude 的私有历史。
- 不自动恢复最近会话。
- 不自动执行模板命令；模板仍只预填编辑器，用户保存后运行。
- 不新增远端写操作或绕过危险命令校验。

## 验收

- `CustomCommandsActivityTest.templatesPrefillEditorWithoutSavingOrExecuting`
  验证 Codex 新建和历史模板同时存在，选择模板只预填编辑器，不保存、不执行。
- `CustomCommandsActivityTest.managementCopyLabelExplainsThatItCreatesANewCommand`
  验证复制文案表达为创建新指令。

## 结论

本切片提升了手机端自定义快捷指令对 Codex CLI 的覆盖度，也降低了管理菜单歧义。它属于 TermuxPro
增值层，不改动 Termux 原始终端、PTY、基础会话或包管理能力。由于本机 KVM 不可用，本轮设备级体验
仍以后续 GitHub Runner Emulator UI 或候选版设备验收为准。
