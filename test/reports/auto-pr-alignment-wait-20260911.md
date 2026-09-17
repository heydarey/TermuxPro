# 自动 PR 控制器完成后再对齐长期分支验收记录（2026-09-11）

## 结论

状态：`READY FOR DEV MERGE`。本轮修复的是维护流程噪声：研发 PR 已经合并、dev 收尾 CI 已经通过时，
如果马上把 `dev_dailyIteration` 对齐到 `dev`，会触发同一分支的新 `auto-dev-pr.yml` run，并由
concurrency 取消还在收尾的旧自动 PR 控制器。功能代码没有失败，但 GitHub 会留下 cancelled 记录，
容易被误读为失败邮件或不稳定迭代。

## 主动发现候选

| 候选观察 | 来源 | 影响旅程 | 分类 | 优先级 | 处理 |
|---|---|---|---|---|---|
| PR 已合并、dev CI 通过后，旧自动 PR 控制器仍可能短暂 `in_progress` | 本轮 PR #323 收尾复盘 | GitHub Actions 可读性、失败邮件降噪 | 发布交付治理 | P1 | 本轮修复 |
| 在旧控制器完成前对齐 `dev_dailyIteration` 会触发 concurrency 取消旧 run | 负责人主动审计 run `34666385439` 与后续空差异 run | 用户看到 cancelled 邮件/记录，误以为发布或 CI 失败 | 发布交付治理 | P1 | 本轮修复 |
| 既有规则只禁止“手工抢跑 PR/合并”，没有覆盖“合并后对齐长期分支”这个后置动作 | 负责人主动审计 AGENTS/skill | 后续 agent 容易重复制造同类噪声 | 可维护性治理 | P1 | 本轮新增门禁 |

## 增值服务准入检查

- 不属于 Termux 原始终端、PTY、包管理、本地 shell、基础会话或基础快捷键能力。
- 属于 TermuxPro 维护体验和发布交付治理，目标是降低 GitHub 噪声、避免用户误判失败。
- 不改 Android 运行时代码，不触碰真实 tmux、SSH 凭据、远端文件或用户数据。

## 改动范围

- `AGENTS.md`、`.agents/skills/termuxpro-development/SKILL.md`、`.claude/skills/termuxpro-development/SKILL.md`：
  研发 PR 合并且 dev 收尾 CI 成功后，必须等待对应 `auto-dev-pr.yml` 控制器自身完成，再对齐
  `dev_dailyIteration`。
- `test/workflow-trigger-policy-test.sh`：新增静态门禁，防止该规则退化。
- `docs/PRODUCT_BACKLOG.md`：记录该流程噪声和治理结果。

## 验收标准

1. 项目规则明确长期分支对齐前要等待自动 PR 控制器完成。
2. workflow 静态测试能拦截规则缺失。
3. 本轮不修改 workflow 并发语义：新 push 仍可取消真正过时的旧 run，只避免人为过早对齐造成噪声。

## 回滚方式

若未来自动 PR 控制器改为合并后立即退出，或长期分支对齐由 workflow 内部接管，可删除本轮规则和对应
`workflow-trigger-policy-test.sh` 断言。
