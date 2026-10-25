# GitHub 噪声审计治理报告（2026-09-17）

## 背景

用户反馈 GitHub 分支和发布包数量偏多，要求维护节奏更克制：日常开发尽量复用
`dev_dailyIteration`，正式版稳定后再发布，不要因为每个小改动都创建分支或发包。

本轮不删除任何远端分支、标签或 Release，只补强只读审计脚本，让负责人在日常巡检时能得到明确的
`OK / NOISE_HIGH` 结论和下一步治理动作。

## 主动发现

- 产品/维护体验：仓库首页和 Actions/Release 列表会被大量历史分支、候选分支和预发布包淹没，降低真实用户判断“哪个版本可安装、哪个分支活跃”的效率。
- 证据来源：运行 `./scripts/audit-github-noise.sh`。
- 增值服务分类：发布交付体验、可维护性治理。不是 Termux 原始终端能力重做。

## 当前审计结果

```text
远端分支总数：207
dev/hotfix 命名分支数：205
保护分支数：3
打开 PR 分支数：0
候选发布分支数：21
已合并陈旧分支候选数：174
未确认或仍活跃 dev/hotfix 分支数：9
Release 总数：42
稳定 Release 数：18
Pre-release 数：24
```

脚本结论：

```text
status=NOISE_HIGH action=plan_cleanup_review
```

命中原因：

- 远端分支超过 50 个。
- 已合并陈旧分支候选超过 20 个。
- 候选发布分支超过 3 个。
- Pre-release 数量超过稳定 Release。
- Release 总数超过 30 个。

## 本轮改动

- `scripts/audit-github-noise.sh` 新增“治理结论”区块。
- 低噪声时输出 `status=OK action=keep_current_cadence`。
- 高噪声时输出 `status=NOISE_HIGH action=plan_cleanup_review` 和命中原因。
- 明确下一步只能先生成清理复核清单，不直接删除远端分支、标签或 Release。
- 明确历史 Release 和附件默认保留，不删除用户可下载产物；后续靠发布窗口守卫减少无价值预发布。
- 默认最多展示 50 个已合并陈旧分支候选，避免审计报告本身变成新的噪声；需要完整清单时设置
  `TERMUXPRO_AUDIT_CANDIDATE_LIMIT=0` 后重跑。
- `test/github-noise-audit-test.sh` 补充低噪声与高噪声 fixture。
- `test/github-noise-audit-test.sh` 补充候选清单限量与完整清单开关回归。

## 验证

```text
./test/github-noise-audit-test.sh
./scripts/audit-github-noise.sh | sed -n '1,90p'
```

验证结论：通过。真实仓库当前输出 `NOISE_HIGH`，默认只展示前 50 个候选，并提示另有 124 个候选未显示；
本轮没有执行任何删除动作。

## 下一步

1. 生成远端陈旧分支清理复核清单。
2. 排除 `master`、`dev`、`dev_dailyIteration`、打开 PR、候选/稳定发布和仍活跃分支。
3. 每个候选删除前再次确认已合入 `origin/dev` 且可由 Git 历史恢复。
4. 候选发布分支单独复核对应标签、Release 和发布报告后再处理。
5. Release 附件默认保留，除非未来有明确安全风险或用户明确要求。
