# 自动 PR 工作流可见性验收

## 背景

本轮维护 PR #260 时，GitHub 页面长期显示自动 PR workflow 停留在“创建或复用 dev PR”。实际状态是 PR
已经创建并合并，workflow 正在同一个脚本步骤中等待 CI、模拟器 UI、合并和 dev 收尾 CI。该展示容易被
误判为创建 PR 卡死，也会增加对 Actions 失败/等待状态的理解成本。

## 增值服务分类

- 持续交付与维护体验
- 移动开发工作流效率的交付保障

该改动不触碰 Termux 原始终端、PTY、本地 shell、包管理或基础会话能力。

## 改动

- 将自动 PR workflow 的可见步骤名从“创建或复用 dev PR”改为“创建 PR、等待门禁、合并并验证 dev”。
- 在 GitHub Step Summary 中说明该步骤串行覆盖：
  - 创建或复用 dev PR
  - 等待同提交完整 CI
  - 按需等待模拟器 UI
  - 合并到 dev
  - 触发并等待 dev 收尾 CI
- 增加 workflow 策略测试，防止后续退回成含义不清的单阶段展示。

## 验收标准

- GitHub Actions 页面能从步骤名直接判断自动 PR 当前步骤不是单纯“创建 PR”。
- Step Summary 解释长时间运行通常表示等待门禁，不代表创建 PR 卡死。
- 原有自动 PR 安全策略保持不变：仍由同一控制器等待门禁、使用 Pulls API 合并、按 merge commit 等待
  dev 收尾 CI，预合并 CI/模拟器失败时保留 PR 且不重复上报自动 PR 失败。

## 本地验证

- `./test/workflow-trigger-policy-test.sh`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./test/context-checkpoint-test.sh`：通过。
- `git diff --check`：通过。

说明：`bash -n .github/workflows/auto-dev-pr.yml` 不适用于 YAML 文件，本轮由仓库 workflow 策略测试覆盖
关键文本与自动 PR 控制器安全不变量。
