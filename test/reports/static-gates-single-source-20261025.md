# 静态门禁唯一清单收敛验收（2026-10-25）

## 主动发现候选

| 候选 | 来源 | 影响 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| 静态门禁清单三处分别维护：test-all 25 项 / pre-push-smoke 14 项 / ci 直接引用 5 项，已经发生漂移（smoke 缺 11 项，含今日新增的 ui-affordance、codex-quota-guard、ai-launch-decision-copy）。 | CI 健康审计 F3 + 本轮复核 | 本地冒烟与 CI 门禁覆盖面不一致，新门禁可能只在一处生效 | 自治研发流水线稳定性 | P1 | 本轮处理：抽 test/static-all.sh 唯一清单源（24 项），test-all/pre-push-smoke/CI 三处全部改为调用它；新增门禁只改一处。 |
| pre-push-smoke 自测试以源码文本断言清单内容（validate-skills、parity 两处），直接搬清单会破坏保护意图。 | 实施中复核 | 断言失效风险 | 自治研发流水线稳定性 | P0 守护 | 本轮处理：断言迁移为"smoke 必须接线 static-all + static-all 必须含 validate-skills 与 parity"，保护意图不变且防再漂移。 |

## 本轮范围

- 新增 `test/static-all.sh`：24 项静态门禁唯一清单（含 2026-10-25 新增的 8 项）。
- `scripts/test-all.sh`、`scripts/pre-push-smoke.sh`（保留 git diff --check 前置）、`.github/workflows/ci.yml` 静态门禁步骤统一调用唯一清单。
- `test/pre-push-smoke-test.sh` 断言迁移：接线断言 + 清单内容断言。

## 非目标

- 不改变各门禁脚本本体与 gradle 测试选择逻辑。
- 不为清单引入按需分组（全量 24 项均为秒级静态脚本，实测本地总耗时可忽略）。

## 验收

- `bash test/static-all.sh` 本地 24 项全绿。
- `bash test/pre-push-smoke-test.sh` 通过（新断言）。
- CI 静态门禁步骤由 PR 流水线验证。

## 结论

静态门禁从三处漂移清单收敛为单一来源，本地冒烟与 CI 覆盖面强制一致；后续新增门禁只改 static-all.sh 一处。
