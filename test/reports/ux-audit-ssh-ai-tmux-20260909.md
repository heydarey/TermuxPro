# SSH + AI CLI + tmux 核心旅程 UX 审计（2026-09-09）

## 审计范围

- 来源：GitHub Actions 模拟器 UI 证据 `34333564700`。
- 截图目录：`test-artifacts/product-audit-20260909/termuxpro-emulator-ui-278/test-artifacts/emulator-ui/`。
- 旅程：手机开发者配置远程工作区 → 打开 AI CLI 会话中心 → 查看 Git 工作台 → 查看 tmux 会话中心 → 从终端页进入工具。
- 限制：本轮使用模拟器截图与 Robolectric，不声称覆盖真实输入法、弱网、厂商 ROM 或真实 SSH 手感。

## 步骤与健康度

1. 工作区首页：一般。首页已收敛到连接路径，但 `tmux 连接方式` 按钮缺少当前策略摘要，200% 字体下 SSH
   地址占位符被截断。
2. AI CLI 会话中心：一般。安全边界清楚，但首屏文案密度高；“Claude 历史 / Codex 历史”容易被理解成
   TermuxPro 真正管理历史 CRUD，而实际只是打开 CLI 原生选择器。
3. Git 工作台：较好。当前分支、目标、未提交数量和推荐操作清楚；底部危险操作 `删除本地分支` 视觉权重
   偏高，后续应下沉到更多操作或危险区。
4. tmux 会话中心：一般。当前工作区优先展示正确；但非当前归属/未知归属会话的进入动作需要更强确认，
   不能只靠“只允许进入”文案表达风险。
5. 终端页工具入口：一般。顶部入口可发现性高于旧图标，但占据终端空间；后续应评估底部/侧边上下文工具条，
   降低对原始终端视野的干扰。

## 本轮落地项

优先处理 tmux 非当前归属会话进入风险：

- 点击其他工作区、其他使用者、归属未知或标记不完整会话时，不再展示普通列表项动作。
- 改为显式确认弹窗，只提供“取消”和“确认进入（不管理）”。
- 文案明确：进入前需确认属于当前任务；TermuxPro 不会重命名、停止或写入该会话的归属标记。

## 本地验证

已执行：

```bash
./scripts/resource-guard.sh
source ./scripts/resolve-jdk17.sh
TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 \
  :app:testDebugUnitTest \
  --tests com.termux.app.TaskSessionsActivityTest \
  --tests com.termux.app.CustomLayoutsSmokeTest
./scripts/validate-skills.sh
git diff --check
```

结果：全部通过。

## 后续候选

| 候选 | 优先级 | 原因 |
| --- | --- | --- |
| 工作区首页展示当前 tmux 策略摘要 | P1 | 降低连接前的上下文判断成本，避免误以为按钮只是高级设置 |
| AI 历史按钮改名为 CLI 原生选择 | P1 | 避免用户期待 TermuxPro 管理 Claude/Codex 历史 CRUD |
| Git 危险操作下沉 | P2 | 降低误触风险，保持 Git 工作台首屏聚焦审查与同步 |
| 终端顶部工具入口形态重评估 | P2 | 需要结合触摸滚动、软键盘和屏幕高度做更完整方案 |
