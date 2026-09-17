# CI 空差异对齐门禁治理记录（2026-09-09）

## 背景

`dev_dailyIteration` 是长期日常研发分支。PR 合入 `dev` 后，为了避免后续重复 rebase，需要把
`dev_dailyIteration` 对齐到最新 `dev`。本轮发现自动 PR workflow 已能在相对 `dev` 无 ahead 提交时成功退出，
但 `TermuxPro CI` 仍会因为 `dev_dailyIteration` push 触发完整 Android 门禁。

这会带来两个问题：

- Actions 列表出现无实际产品变更的重型 CI，增加噪声。
- Android 构建和 APK 上传占用 GitHub Runner 时间，和“克制分支/发包/CI 噪声”的维护目标冲突。

## 主动发现记录

| 候选 | 来源 | 影响旅程 | 证据 | 优先级 | 准入分类 | 处理 |
| --- | --- | --- | --- | --- | --- | --- |
| 长期分支对齐触发完整 CI | 负责人合入后巡检 | 维护者查看 Actions、用户收失败/取消邮件 | `dev_dailyIteration` 对齐到 `be5e2c52` 后触发 run `34334834787`，自动 PR run `34334834754` 已空差异成功退出 | P1 | 移动端工作流效率 / 发布交付体验 | 本轮修复 |
| CI 空差异时仍按完整 Android 门禁 | 负责人规则审计 | 共享资源和 GitHub Runner 成本 | `.github/workflows/ci.yml` 只按文件 diff 判断，没有判断研发分支相对 `origin/dev` 是否 ahead | P1 | 发布交付体验 | 本轮修复 |
| 空差异策略缺少静态校验 | 负责人测试缺口审计 | 后续 workflow 维护可能回退 | `test/workflow-trigger-policy-test.sh` 没有断言 `origin/dev..HEAD` ahead 检测 | P2 | 质量门禁 | 本轮补测试 |

## 修复

- 在 `ci.yml` 的 Android 运行时门禁判定中增加 `is_dev_target_branch()`。
- 对 `dev_dailyIteration`、`dev_*`、`hotfix_*` 的 push，先 fetch `origin/dev`，计算
  `git rev-list --count origin/dev..HEAD`。
- 若 ahead 为 `0`，判定为分支对齐操作，设置 `android_runtime=false`，只保留静态门禁。
- 在 `test/workflow-trigger-policy-test.sh` 增加静态断言，防止该规则被回退。

## 验收标准

- 真实研发分支相对 `dev` 有 ahead 提交时，CI 继续按变更类型决定是否运行 Android 重门禁。
- 长期分支仅对齐到 `dev` 时，不再触发全模块测试、Lint 和 Debug APK 构建。
- 自动 PR 仍负责空差异成功退出，不创建空 PR。

## 本地验证

已执行：

```bash
./scripts/validate-skills.sh
./test/workflow-trigger-policy-test.sh
git diff --check
```

结果：全部通过。
