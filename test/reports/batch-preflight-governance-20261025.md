# 批量发布门禁治理验收（2026-10-25）

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| 批量窗口 7 个提交从未过任何 CI；每日绿灯全部是冻结 37 天的 master；Actions Gradle 缓存闲置 7 天即逐出，发布日首跑必为冷缓存。 | 测试与 CI 健康审计 | 10-26 发布列车全部门禁 | 自治研发流水线稳定性 | P0 | 本轮处理：在 wip_batch20261026 快照分支 workflow_dispatch 预检 CI（run 40186960685）与模拟器 UI（run 40186963478），提前验证批量代码并重建 Actions 缓存。 |
| auto-dev-pr 模拟器等待按“最后一个 UI 影响提交”SHA 匹配 run；批量推送的 ui-emulator run 挂在头提交 SHA 上，若头提交是 docs/chore 则死等 60 分钟超时红 X。 | 测试与 CI 健康审计 | 批量大 PR 的自动合并 | 自治研发流水线稳定性 | P0 | 本轮处理：复用旧 run 前先确认该 SHA 存在既有运行，不存在则回退等待头提交 SHA；增量单提交模式行为不变。 |
| auto-dev-pr job 预算 60 分钟，需串行等待完整 CI + 模拟器 UI + dev 收尾 CI（+可选候选 Release），冷缓存下极易被拦腰砍断。 | 测试与 CI 健康审计 | 发布列车 | 自治研发流水线稳定性 | P1 | 本轮处理：timeout-minutes 60→120。 |
| 7 个静态门禁（含今日新增 codex-quota-guard-test、ui-affordance-batch-test）不在 test-all.sh 与 CI 中，本地无 git hooks，纯靠自觉执行。 | 测试与 CI 健康审计 | 批量窗口“本地冒烟后统一提交”模式 | 自治研发流水线稳定性 | P1 | 本轮处理：8 个脚本全部纳入 scripts/test-all.sh 静态段；三处清单漂移的进一步收敛（抽 static-all）登记后续。 |
| ai.yml/auto-dev-pr 门禁失败时 exit 0 静默保留 PR，批量推送后若 CI 失败无显性红信号，检测延迟最长 1 天。 | 测试与 CI 健康审计 | 发布火车当天 | 自治研发流水线稳定性 | P1 | 暂缓：静默退出是对“重复失败邮件噪声”的既有治理决策；冻结时刻改为主动轮询 PR 检查状态，不改工作流。 |

## 本轮范围

- `scripts/test-all.sh` 静态段追加 codex-quota-guard-test、android-string-resource-parity-test、release-window-guard-test、context-checkpoint-test、goal-lifecycle-policy-test、generated-cache-cleanup-test、android-sdk-bootstrap-test、ui-affordance-batch-test（8 项本地全部实测通过）。
- `.github/workflows/auto-dev-pr.yml`：模拟器 UI 等待的批量推送死锁修复（复用前确认既有运行，否则回退头提交）；job timeout-minutes 60→120。
- 批量代码预检：wip_batch20261026 上 dispatch CI 与模拟器 UI 各一次。

## 非目标

- 不改变 auto-dev-pr 失败静默退出的既有噪声治理决策。
- 不在本轮抽取 test/static-all.sh 统一三处清单（登记后续切片）。
- 不处理 maintenance.yml 超时、artifact 保留期与 ui-emulator 每日调度噪声（P2，登记后续）。

## 验收

- 8 个静态脚本在当前工作树全部通过（含验证中英资源 key parity）。
- auto-dev-pr.yml YAML 解析通过；bash -n scripts/test-all.sh 通过。
- 预检 run 40186960685（CI）/ 40186963478（UI）结果在冻结前复核；失败则先修复再冻结。

## 结论

发布火车当天最可能的三类停摆（绿色盲区冷缓存、SHA 死等超时、门禁预算不足）已全部消除或缓解；静态门禁从“自觉执行”收敛为 CI 强制。
