# Release workflow 步骤级超时治理验收

## 结论

- 状态：通过本地静态门禁，等待 GitHub Actions 远端轻量 CI 复核。
- 增值服务分类：移动端工作流效率、自治研发/发布流水线稳定性。
- 本轮目标：减少正式包发布阶段长时间 pending、失败邮件噪声和状态误读；让真正的构建、模拟器或
  GitHub Release 问题在限定时间内失败并留下可定位步骤。

## 主动发现候选

1. **推进：Release 重步骤缺少步骤级 timeout**
   - 证据：`.github/workflows/release.yml` 只有 job 级 `timeout-minutes: 90`，正式构建、模拟器安装、
     模拟器启动和覆盖升级验收没有单步上限。
   - 影响旅程：候选或正式包发布时，用户只能看到整条 Release 长时间运行，无法判断是构建、设备验收
     还是 GitHub Release 创建卡住。
   - 为什么现在做：用户多次反馈 GitHub/邮件里失败或 pending 噪声太多，发布流程必须先具备清晰失败
     边界，后续才能稳定发包。
   - 为什么不是 Termux 原始能力重做：这是 TermuxPro 自治交付链路，不改 PTY、shell、包管理或基础终端。

2. **推进：Release 轻步骤也需要短超时**
   - 证据：版本校验、签名材料恢复和 GitHub Release 创建虽然通常很快，但网络/API/Secret 异常时可能
     让整条流水线等待到 job 级超时。
   - 影响旅程：正式包失败原因不清晰，负责人后续排障和用户判断是否下载都会被拖慢。
   - 为什么现在做：短超时能把“配置/API 异常”和“Android 构建异常”区分开，降低重复失败邮件。
   - 暂缓项：不改变 Release 版本策略、不删除历史 Release、不触碰签名 secrets。

3. **暂缓：维护巡检 workflow 是否需要 job/step timeout**
   - 证据：`.github/workflows/maintenance.yml` 目前只创建去重 Issue，步骤很轻。
   - 暂缓理由：当前任务只覆盖发布包风险；维护 workflow 没有模拟器、Gradle 或正式资产创建，收益较低。
   - 下一动作：若后续巡检扩展到网络扫描、仓库清理或自动汇总，再补同类 timeout 门禁。

## 改动范围

- `.github/workflows/release.yml`
  - 版本源校验：2 分钟。
  - NDK 安装：5 分钟。
  - Release 签名材料恢复：2 分钟。
  - 正式 APK 构建：35 分钟。
  - 发布模拟器系统镜像安装：10 分钟。
  - 发布模拟器创建/启动：8 分钟。
  - 待发布签名 APK 覆盖升级验收：20 分钟。
  - GitHub Release 创建：3 分钟。
  - 模拟器关闭：2 分钟。
- `test/workflow-trigger-policy-test.sh`
  - 增加 Release 步骤级 timeout 静态门禁，避免以后回退。
- `docs/PRODUCT_BACKLOG.md`
  - 将 GitHub 门禁步骤级超时 P0 从“进行中”收口为“已完成”，并补 Release 证据入口。

## 验收标准

- Release workflow 不再只依赖 job 级 90 分钟超时。
- 构建、模拟器安装/启动、覆盖升级验收和 GitHub Release 创建均有明确步骤级上限。
- 静态测试能在未来发现 Release timeout 被误删或漏配。
- 不改变候选/稳定发布策略，不创建 Release，不发送飞书通知。

## 回归与资源

- 本轮不启动本地模拟器：当前远程共享服务器 KVM 不可用，且改动为 GitHub Actions YAML/静态门禁。
- 不执行默认 `tmux kill-server`，不触碰真实 `hdr-TermuxPro日常迭代` 会话。
- 不读写签名私钥或密码。

## 本地验证

- `test/workflow-trigger-policy-test.sh`：通过。
- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `test/dialog-readable-style-test.sh`：通过。
