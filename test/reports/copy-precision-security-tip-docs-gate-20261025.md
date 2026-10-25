# 工作区安全提示与文档AI历史表述收敛验收（2026-10-25）

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| zh `workspace_security_tip` 仍写“AI 历史会话”，且 en 版只提指纹、漏掉密码/密钥与 tmux/历史边界，中英文语义分叉。 | 文案/i18n 只读审计（本批量窗口） | 工作区首页安全提示条（共享账号用户判断隐私边界的第一触点） | 远程工作区、AI CLI | P0 | 本轮处理：zh 收敛为 Claude/Codex 历史会话，en 与中文语义对齐。 |
| 静态门禁只禁“远端 AI 历史/读取 AI 历史/AI 私有历史”三个模式，“AI 历史会话/续接 AI 历史/AI history sessions”变体漏网，且不扫 README 与 UX 质量门。 | 文案/i18n 只读审计 | CI 文案守护本身 | 自治研发流水线稳定性 | P0 | 本轮处理：门禁补禁用模式并纳入 README.md、docs/UX_QUALITY.md。 |
| README.md 与 docs/UX_QUALITY.md 仍用泛化“AI 历史/tmux/AI 历史会话”表述。 | 文案/i18n 只读审计 | GitHub 首屏新用户触点、UX 检查表 | 上下文工具箱 | P1 | 本轮处理：与资源同口径收敛。 |
| `TermuxSessionsListViewController` 会话兜底标题硬编码 "null session"，中文界面漏出英文。 | 文案/i18n 只读审计 | 终端会话列表异常态 | 原始能力兼容守护 | P2 | 本轮处理：资源化 `session_null_title`（未知会话 / Unknown session）。 |
| 终端工具栏文本输入 EditText 无 hint 且用 `tools:ignore="LabelFor"` 压制 lint。 | 文案/i18n 只读审计 | 终端 extra-keys 文本输入高频触点 | 移动端工作流效率 | P2 | 本轮处理：新增 `terminal_toolbar_input_hint` 并移除压制。 |
| `TextIOActivity` 标题兜底 "Text Input" 与 `SharedProperties` 属性加载失败 Toast 硬编码英文长句。 | 文案/i18n 只读审计 | 共享库文本输入页、属性文件损坏提示 | 原始能力兼容守护 | P2 | 本轮暂缓：均属 `termux-shared` 上游继承面，改动会加大后续上游同步冲突面；登记到 backlog 按上游同步窗口处理。 |
| 历史 `docs/RELEASE_NOTES_0.3.0-rc.1.md` 也含泛化表述。 | 文案/i18n 只读审计 | 已发布版本的历史记录 | 自治研发流水线稳定性 | P2 | 本轮结论：历史发布说明属不可变发布记录，不回溯改写，门禁扫描明确排除并注释原因。 |

## 本轮范围

- `workspace_security_tip` 中英文语义对齐并精确化；新增 `session_null_title`、`terminal_toolbar_input_hint` 资源。
- `TermuxSessionsListViewController` 兜底标题资源化；终端工具栏文本输入布局补 hint 并移除 LabelFor 压制。
- `test/ai-launch-decision-copy-test.sh` 新增变体禁用模式、新表述锁定断言，并把 README.md 与 docs/UX_QUALITY.md 纳入扫描（历史 RELEASE_NOTES 明确排除）。
- README.md 与 docs/UX_QUALITY.md 与资源同口径收敛为 Claude/Codex 表述。

## 非目标

- 不改 `termux-shared` 上游继承代码（TextIOActivity、SharedProperties 两处暂缓，理由见上表）。
- 不回溯改写历史 RELEASE_NOTES。
- 不改变任何终端行为、SSH 流程或 AI CLI 启动语义；本轮全部为表述与无障碍标注收敛。

## 验收

- `bash test/ai-launch-decision-copy-test.sh` 通过（含新增断言与文档扫描）。
- `grep -rn "AI 历史\|AI history" README.md docs/UX_QUALITY.md app/src/main/res/values*/strings.xml` 仅剩 Claude/Codex 前缀表述。

## 结论

工作区首页隐私边界提示在中文界面不再泛化，中英文语义一致；文案守护门禁覆盖到此前漏网的变体与用户可见文档；两处上游面硬编码登记暂缓，避免为小改动扩大上游同步冲突面。
