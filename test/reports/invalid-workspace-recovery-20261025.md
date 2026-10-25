# 无效工作区错误态恢复体验验收（2026-10-25）

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| 五个增值页（远端文件、文件预览、项目任务、连接诊断、tmux 管理、Git 工作台）无效工作区错误均为死胡同文案“工作区连接信息无效。”，不说明缺什么、怎么恢复。 | UI 走查审计（connection-diagnostic/remote-files/remote-file-preview/project-tasks 截图）+ 本轮实现时补充发现 tmux 管理页同病 | 新用户配置有误时首次进入增值页即撞死胡同 | 远程工作区 | P1 | 本轮处理：六处文案统一改为“缺少可用的 SSH 地址、端口或项目目录 + 返回工作区补全”的可恢复表述。 |
| 无效态下刷新按钮仍可点（项目任务页已禁用但无透明度反馈，其余页未禁用），点击必然再失败。 | UI 走查审计 | 错误态误导交互 | 远程工作区 | P1 | 本轮处理：远端文件、文件预览、连接诊断、Git 工作台无效态禁用刷新并降透明度 0.4；项目任务页补透明度，五页行为一致。 |
| 连接诊断页在无效工作区时零诊断内容，与页面名义职责不符。 | UI 走查审计 | 首次 SSH 排障 | 远程工作区 | P1 | 本轮处理（部分）：错误文案列明缺失项（地址/端口/目录），具备基础诊断语义；完整分阶段诊断属有效连接场景，不属无效态目标。 |

## 本轮范围

- `remote_files_invalid_workspace`、`project_tasks_invalid_workspace`、`connection_diagnostic_invalid_workspace`、`git_diff_invalid_workspace`、`task_sessions_invalid_workspace` 中英文统一为可恢复表述。
- RemoteFilesActivity、RemoteFilePreviewActivity、ConnectionDiagnosticActivity、GitDiffActivity 无效态禁用刷新按钮并 setAlpha(0.4f)；ProjectTasksActivity 补 setAlpha。
- `test/ui-affordance-batch-test.sh` 追加无效工作区段：旧死胡同文案失败关闭、新恢复表述与五页禁用逻辑必须存在。

## 非目标

- 不为无效态新增“编辑连接”直达入口（返回工作区按钮已承载该路径，且工作区编辑态闭环已在既有切片完成）。
- 不改变有效连接下的诊断流程与刷新行为。
- tmux 管理页刷新按钮（若存在）未在本轮处理范围，登记后续走查。

## 验收

- `bash test/ui-affordance-batch-test.sh` 通过（含新段）。
- `bash test/android-string-resource-parity-test.sh` 通过（六处中英 key 对齐）。
- 视觉行为由冻结时 ui-emulator 门禁的 invalid 工作区场景截图复验。

## 结论

无效工作区从“无原因无动作死胡同”变为“列明缺失项 + 一键回工作区补全 + 误导性刷新关闭”的可恢复错误态，五页行为一致。
