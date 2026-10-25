# 工作流噪声治理验收（2026-10-25）

## 主动发现候选

| 候选 | 来源 | 影响 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| maintenance.yml 无 job 级 timeout（默认 360 分钟）且无 concurrency，gh 调用卡住可挂 6 小时。 | 测试与 CI 健康审计（对照自家"重步骤必须超时"策略） | Runner 分钟空耗、失败检测延迟 | 自治研发流水线稳定性 | P2 | 本轮处理：job timeout-minutes: 10 + concurrency 组。 |
| ci.yml "上传测试报告/上传 Debug APK" 未设 retention-days（默认 90 天）。 | 测试与 CI 健康审计 | artifact 存储空耗 | 自治研发流水线稳定性 | P2 | 本轮处理：两处 retention-days: 14。 |
| ui-emulator.yml 每日 schedule 在冻结 master 上空跑并常驻约 30 个零信息 artifact。 | 测试与 CI 健康审计 | Runner 分钟与存储空耗 | 自治研发流水线稳定性 | P2 | 暂缓：每日截图是发布证据连续性的一部分，改调度或加 headSha 短路涉及证据口径，登记为独立设计切片。 |

## 本轮范围

- maintenance.yml：concurrency 组 + create-maintenance-task job timeout-minutes: 10。
- ci.yml：termuxpro-test-reports 与 termuxpro-debug-apk 两个 upload 步骤补 retention-days: 14。

## 非目标

- 不改 ui-emulator 每日调度（登记独立切片）。
- 不改 test-all/pre-push-smoke/ci 三处静态清单的结构（static-all 收敛为独立切片）。

## 验收

- YAML 语法与门禁由 PR CI 验证；maintenance 下次调度按 10 分钟超时生效。
- 本切片为纯 workflow 治理，无代码行为变更。
