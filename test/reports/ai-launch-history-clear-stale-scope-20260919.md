# AI 本地启动记录清空过期目标范围回归

## 场景

真实用户会把同一个远程工作区从旧服务器/旧目录改到新服务器/新目录。此时 TermuxPro 仍会保留本机本 App 的 AI CLI 启动记录，但其中一部分记录的目标已经不是当前工作区。

## 主动发现

- 过期记录已经能阻止直接再次打开，但“清空本地记录”按钮此前只显示记录总数。
- 当同一工作区同时存在当前目标记录和过期目标记录时，用户无法在清空前知道其中有多少条目标已变化。
- 这容易让共享 Claude/Codex/tmux 场景下的用户误解为 App 可能会删除远端 AI 历史、tmux 会话或终端输出。

## 本轮处理

- 清空按钮在存在过期目标时显示“清空 N 条记录（含 M 条过期）”。
- 清空按钮无障碍描述补充“其中 M 条目标已变化”，并明确只删除本机本 App 记录。
- 清空确认弹窗在存在过期目标时展示当前目标和过期数量，继续明确不会删除 Claude/Codex 远端历史、tmux 会话或终端输出。

## 回归验证

- `./test/android-string-resource-parity-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `git diff --check`：通过。
- `timeout 55s ./gradlew testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest --max-workers=2`：本机 55 秒内未完成，按共享远程低资源策略中止，交由 GitHub CI 完整验证。

## 后续候选

- 在“查看全部记录”弹窗标题或顶部补充当前工作区目标，进一步降低多工作区误判。
- 为启动记录增加筛选：全部 / 当前目标 / 过期目标。
- 若记录数量继续增长，考虑把清空动作改为“清空当前目标记录 / 清空全部本地记录”两级动作。
