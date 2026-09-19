# AI 本地记录远端历史文案收敛验收（2026-09-19）

## 背景

负责人继续走查 AI CLI 会话中心后发现，本地启动记录区域已经大部分收敛为
“TermuxPro 本地启动记录”，但少量空态、下一步建议和无障碍描述仍使用“远端 AI 历史”
或“读取 AI 历史”这类泛化说法。

## 主动发现

| 观察 | 证据 | 影响旅程 | 增值服务分类 | 处理 |
|---|---|---|---|---|
| “远端 AI 历史”不如“Claude/Codex 远端历史”精确 | `values-zh-rCN/strings.xml` | 用户可能误以为 TermuxPro 管理某个泛化 AI 历史库 | AI CLI、远程工作区、移动端交互体验 | 本轮修复 |
| 英文资源仍有 `read AI history` / `remote AI history` | `values/strings.xml` | 英文系统或无障碍读法下仍会暗示读取远端 AI 历史 | AI CLI、无障碍体验 | 本轮修复 |
| 静态门禁只要求本地记录语义，没有阻止泛化历史文案回退 | `test/ai-launch-decision-copy-test.sh` | 后续迭代可能再次写回模糊说法 | QA/持续维护 | 本轮补门禁 |

## 本轮变更

- 中文提示统一为“Claude/Codex 远端历史”或“Claude/Codex 历史”。
- 英文提示统一为 `Claude/Codex remote history` 或 `Claude/Codex history`。
- 静态校验阻止 `远端 AI 历史`、`读取 AI 历史`、`remote AI history` 和 `read AI history`
  再次出现在用户可见资源中。
- 更新 Robolectric 断言，确保下一步建议和无障碍描述都使用精确语义。

## 非目标

- 不读取 Claude/Codex 私有历史。
- 不自动恢复历史、不自动进入 tmux。
- 不改变 SSH、终端、tmux 或 AI CLI 启动逻辑。

## 验收

- `test/ai-launch-decision-copy-test.sh`
- `test/android-string-resource-parity-test.sh`
- `git diff --check`

