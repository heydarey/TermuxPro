# AI 会话中心工具入口安全语义验收

## 结论

- 状态：本地实现完成，等待静态/Robolectric/CI 复核。
- 增值服务分类：AI CLI、远程工作区、tmux/Git 可视化、自定义快捷指令、移动端工作流效率。
- 本轮目标：让 AI 会话中心里的工具入口在读屏、大字体、滚动后脱离上下文或误触前确认时，仍能明确
  “只打开页面/只读检查/先确认/不会自动执行/不会误入 tmux”。

## 主动发现候选

1. **推进：AI 后续工具入口缺少操作边界**
   - 证据：`activity_ai_cli_session_center.xml` 中 Git、项目任务、快捷指令和服务器配置按钮只有短文本。
   - 影响旅程：用户从 Claude/Codex 完成后想检查 Git 或跑测试，若只聚焦按钮，无法判断是否会把命令
     直接打进当前 AI/TUI 终端。
   - 为什么现在做：用户主场景是手机 SSH 到远端使用 Codex/Claude，误输入和误操作成本高。
   - 为什么不是 Termux 原始能力重做：只增强 TermuxPro AI 会话中心工具入口语义，不改原始终端、PTY
     或 shell 行为。

2. **推进：启动前检查/tmux 管理入口缺少安全默认说明**
   - 证据：环境检查和 tmux 按钮未声明只读、显式选择、未归属会话不自动恢复。
   - 影响旅程：共享 Claude 账号和多人 tmux 环境下，用户可能担心点一下就自动进入或污染别人会话。
   - 为什么现在做：此前真实反馈已经确认自动进入 tmux 风险较高，入口层就应降低误解。

3. **暂缓：重新排序 AI 会话中心卡片**
   - 证据：`AI 完成后` 卡片目前在页面靠后，可能影响发现。
   - 暂缓理由：移动卡片会影响当前截图矩阵和用户已熟悉路径；本轮先用低风险语义补强，后续结合模拟器
     200% 字体截图再评估是否把“AI 完成后”前移。

## 改动范围

- `activity_ai_cli_session_center.xml`
  - 为环境检查、tmux 管理、Git、项目任务、快捷指令、服务器配置入口补 `contentDescription`。
- `values-zh-rCN/strings.xml`、`values/strings.xml`
  - 补充中英文说明，明确只读、不自动执行、不自动授权、不直接提交/推送/丢弃、不自动进入未归属 tmux。
- `AiCliSessionCenterActivityTest`
  - 断言所有工具入口说明包含安全边界，并保持原有路由行为。

## 验收标准

- 用户聚焦任一工具入口时可以知道该入口是否会执行命令或改变远端状态。
- Git 入口明确不提交、拉取、推送或丢弃修改。
- 项目任务入口明确会先展示目标与命令确认，不向当前 Claude/Codex 终端注入测试命令。
- tmux 入口明确必须显式选择，未归属会话不会被自动恢复、重命名或停止。
- 路由行为保持不变，不新增首页或 AI 中心视觉噪声。

## 回归与资源

- 本轮涉及 Android 资源与 Robolectric 测试，应跑相关 JVM/Robolectric 和静态门禁。
- 本地共享机 KVM 不可用，不启动本地模拟器；远端 GitHub Emulator UI 作为截图门禁。
- 不执行默认 `tmux kill-server`，不触碰真实 `hdr-TermuxPro日常迭代` 会话。

## 本地验证

- `test/workflow-trigger-policy-test.sh`：通过。
- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `test/dialog-readable-style-test.sh`：通过。
- `./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiCliSessionCenterActivityTest`：
  未跑到测试阶段；本机解析 Android Gradle Plugin `com.android.tools.build:gradle:8.13.2` 时访问
  `dl.google.com` 超时。该失败属于本机网络依赖下载问题，不是 Robolectric 断言失败；最终以 GitHub
  Runner 的完整 CI/Robolectric/模拟器门禁为准。
- `timeout --foreground 180s ./scripts/pre-push-smoke.sh`：脚本门禁已通过，进入本机 Gradle 阶段后未在
  180 秒内返回；为避免远程共享服务器持续占用，未继续本地重试。远端 PR CI 必须完成完整 Android
  门禁后才能合入。
