# 自动 PR Checks 兜底验收报告（2026-09-17）

## 结论

状态：本地静态门禁完成，等待 GitHub Actions 通过真实 PR 验证。

本轮在 PR #383 合入过程中主动发现：`TermuxPro CI` 与 `TermuxPro Emulator UI` 均已成功，PR 也已经满足
实际预合并门禁，但自动研发 PR 控制器仍长时间停留在“创建 PR、等待门禁、合并并验证 dev”单步中。为避免
控制器空等到 job 超时并产生失败邮件，本轮给等待逻辑补充 GitHub Checks 兜底。

## 主动发现候选

| 候选 | 来源 | 影响旅程 | 增值服务分类 | 处理结论 |
| --- | --- | --- | --- | --- |
| 自动 PR 控制器在 CI/UI 已绿后仍可能空等，需要人工接管并取消 run。 | 负责人处理 PR #383 时的 GitHub 状态巡检。 | 自治研发闭环 → PR 合并 → dev 收尾 CI → 长期分支对齐。 | 移动端工作流效率、发布交付治理。 | 本轮处理。 |
| `gh run list` 依赖 workflow、branch、event 和 headSha 的组合查询，遇到 GitHub 刷新延迟或分支匹配异常时缺少第二证据源。 | 复核 `.github/workflows/auto-dev-pr.yml` 的 `wait_for_workflow()`。 | 自动等待预合并 CI / 模拟器 UI。 | CI 成本与失败噪声治理。 | 本轮处理。 |
| 当前 `docs/CURRENT_STATE.md` 是旧时间生成，恢复后若只读旧文件会低估最新 PR/Release 状态。 | 会话恢复巡检发现当前文件仍停留在 `cc95d2fd`。 | 会话恢复 → 选择下一切片。 | 自治研发可靠性。 | 暂缓：本轮先处理控制器空等；下轮考虑在 PR 闭环后刷新检查点。 |

## 变更范围

- `.github/workflows/auto-dev-pr.yml`
  - 增加 `checks: read` 权限。
  - 新增 `workflow_check_name()`，将 `ci.yml` 映射到 `test` check，将 `ui-emulator.yml` 映射到
    `emulator-ui` check。
  - 新增 `wait_for_check_run()`，当 `gh run list` 查询失败或暂未命中 run 时，查询目标提交上的
    GitHub Actions check run；若 check 已成功则放行，若失败则保留 PR。
- `test/workflow-trigger-policy-test.sh`
  - 增加静态回归，要求自动 PR 控制器必须保留 Checks 兜底和 `test` / `emulator-ui` 映射。

## 非目标

- 不改变产品 Android 运行时代码。
- 不改变候选/稳定 Release 的发布门禁。
- 不降低预合并 CI、模拟器 UI 或 dev 收尾 CI 的质量要求。
- 不把自动 PR 控制器自身的 `create` check 当作合并放行证据。

## 验收标准

1. `wait_for_workflow()` 在 `gh run list` 超时或未命中时会尝试 GitHub Checks 兜底。
2. CI 兜底只接受目标提交上的 `test` check 成功。
3. 模拟器 UI 兜底只接受目标提交上的 `emulator-ui` check 成功。
4. check 失败时仍保留 PR，不自动合并。
5. 静态策略测试能防止兜底逻辑被删除。

## 本地验证

- `./test/workflow-trigger-policy-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./test/android-string-resource-parity-test.sh`：通过。
- `git diff --check`：通过。
- 从 `.github/workflows/auto-dev-pr.yml` 抽取 `run: |` shell 代码后执行 `bash -n`：通过。
- `./scripts/pre-push-smoke.sh`：通过；本轮只涉及 workflow/文档/测试策略，静态门禁完成，未启动本地 Android 重任务。

## 复盘

- 减少的真实负担：避免负责人在 CI/UI 已绿时还要人工判断、手动合并、取消卡住的控制器 run。
- 新增控件/流程噪声：无用户侧入口；仅增加自动化兜底日志。
- Termux 原始能力影响：无，未触碰终端、PTY、本地 shell 或基础会话。
- 下一轮建议检查：自动 PR 控制器的单 step 日志仍需更可观测，必要时拆分为多个可见步骤或写更完整 Step Summary。
