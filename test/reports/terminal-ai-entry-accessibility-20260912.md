# 终端顶部 AI 入口无障碍语义回归（2026-09-12）

## 主动发现记录

| 候选 | 来源 | 优先级 | 增值服务分类 | 影响旅程 | 结论 |
| --- | --- | --- | --- | --- | --- |
| 终端顶部“AI”按钮的无障碍说明过短 | 负责人主动走查终端顶栏 | P1 | AI CLI、上下文工具箱、移动端交互体验 | 小屏上可见文本只能保留“AI”，TalkBack/无障碍说明如果也只说“AI CLI 会话中心”，用户仍不知道会进入 Claude/Codex 新建或历史入口 | 本轮处理：复用已有完整说明 |
| 终端顶部“AI”按钮可见文本改成长文 | UI/UX 取舍复核 | P2 | 移动端交互体验 | 长文本会挤压 360dp/200% 字体顶栏，反而破坏高频终端空间 | 暂缓：保持短文本，只增强 contentDescription |
| 顶栏全部入口重新设计为图标导航 | 产品假设挑战 | P2 | 上下文工具箱、移动端交互体验 | 可能提升美观，但会扩大改动范围并影响既有可发现性回归 | 暂缓：等下一轮完整顶栏信息架构审计 |

## 本轮改动

- `terminal_ai_center_button` 可见文本仍为“AI”，保持窄屏顶栏稳定。
- `contentDescription` 改为“打开当前工作区的 Claude/Codex 会话中心，选择新建或历史入口。”
- 不改变点击行为，不自动启动 AI，不发送任何命令。

## 非目标

- 不改 Termux 原始终端、PTY、滚动和本地会话行为。
- 不重新设计整个终端顶栏。
- 不新增首页或工具箱常驻入口。

## 回归范围

- `CustomLayoutsSmokeTest.terminalNavigationUsesPersistentLabelsAndAccessibleTargets`
- 360dp / 200% 字体布局仍由 GitHub Emulator UI 门禁覆盖。
