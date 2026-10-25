# 非 Codex 驱动额度门禁治理验收（2026-10-25）

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 分类 | 优先级 | 处理结论 |
|---|---|---|---|---|---|
| ZCode 接管后 `codex-quota-guard.sh` 因缺少 `CODEX_THREAD_ID` 失败关闭，检查点额度段输出错误信息，非 Codex 驱动每轮都被误导为"额度不可核算"。 | 负责人接管走查 `scripts/update-context-checkpoint.sh` 输出 | 会话恢复 → 检查点刷新 → 续跑决策 | 自治研发流水线稳定性 | P1 | 本轮处理。 |
| `AGENTS.md` 与 SKILL.md 的恢复步骤要求先跑 Codex 额度守卫，非 Codex 驱动按步骤执行只会得到失败，没有声明自身身份的通道。 | 负责人走查恢复文档 | 每次会话恢复/模型切换 | 自治研发流水线稳定性 | P1 | 本轮处理。 |
| 若简单删除失败关闭逻辑，Codex 会话在配置损坏时可能绕过额度门禁继续消耗。 | 架构评审 | Codex 驱动的额度安全 | 自治研发流水线稳定性 | P0 守护 | 本轮不改 Codex 行为：仅当驱动显式声明 `TERMUXPRO_DRIVER_ID` 且缺少 `CODEX_THREAD_ID` 时放行。 |

## 本轮范围

- `scripts/codex-quota-guard.mjs` 新增非 Codex 驱动通道：未设置 `CODEX_THREAD_ID` 且显式设置
  `TERMUXPRO_DRIVER_ID` 时，输出 `codexQuotaObservable=false` 的结构化 JSON 并以退出码 0 放行；
  其余情况（含 Codex 环境变量存在但无效）保持原有失败关闭行为不变。
- `test/codex-quota-guard-test.sh` 新增两组用例：声明驱动标识后放行且 JSON 字段完整；未声明且缺少
  `CODEX_THREAD_ID` 时仍失败关闭。
- `AGENTS.md` 额度门禁段落与 SKILL.md 开发环境红线新增非 Codex 驱动规则，恢复步骤不再误导非 Codex 驱动。

## 非目标

- 不改变 Codex 驱动的额度核算与 15% 门禁行为。
- 不为额度读数引入任何猜测或伪造来源。
- 不放宽资源守卫、发布门禁和质量门禁。

## 验收

- `bash test/codex-quota-guard-test.sh` 通过（含新增两组用例）。
- `TERMUXPRO_DRIVER_ID=zcode-batch-test ./scripts/codex-quota-guard.sh` 输出 `codexQuotaObservable=false`
  且退出码 0；不带声明时退出码非 0。

## 结论

非 Codex 驱动获得明确的身份声明与额度豁免通道，Codex 驱动的额度门禁行为保持不变，恢复步骤对所有驱动
一致可用。托管方切换不再制造"额度不可核算"的假阻塞。
