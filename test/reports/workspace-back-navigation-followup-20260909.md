# 工作区返回一致性跟进验收

## 背景

用户在 `0.10.0` 体验中反馈：进入“编辑连接”无法返回、空工作区无法删除、从终端进入工作台缺少上一页入口。
上一轮已修复主路径，但本轮主动走查发现同类风险仍存在于 AI CLI 会话中心：该页面的“工作区配置”和
多个无效工作区兜底路径直接打开工作台，没有告诉工作台“这是从上一页进入的”。结果是用户从 AI 会话
中心进入工作台后可能仍看不到顶部返回入口。

## 增值服务准入

- 分类：远程工作区、AI CLI、移动端工作流效率。
- 增值层：帮助手机用户在 AI CLI 会话中心和工作区之间安全往返，不改变 Termux 原始终端、PTY、包管理
  或本地 shell 行为。
- 非目标：不重做 Android 系统返回、不修改 Termux 基础会话抽屉、不启动真实 SSH/tmux。

## 本轮主动发现

| 观察 | 来源 | 影响旅程 | 优先级 | 处理 |
|---|---|---|---|---|
| AI CLI 会话中心打开工作区没有传递上一页语义 | 负责人代码走查 | AI CLI → 工作区配置 → 返回 AI 中心 | P1 | 本轮修复 |
| `EXTRA_SHOW_BACK_TO_TERMINAL` 命名过窄，后续非终端入口容易误用或漏用 | 负责人架构走查 | 所有增值页 → 工作区 | P2 | 本轮新增通用 `EXTRA_SHOW_BACK` 并保留旧字段兼容 |
| `FLAG_ACTIVITY_REORDER_TO_FRONT` 复用已有工作台实例时不会重新执行 `onCreate` | 负责人生命周期走查 | 终端/AI 中心 → 已存在工作台实例 → 返回上一页 | P1 | 本轮补 `onNewIntent` 读取新返回语义 |
| backlog 已验证项仍标记“进行中”，容易造成重复返工 | 流程走查 | 日常维护计划 | P2 | 本轮校准为“已完成”，并补当前证据链接 |

## 改动

1. `WorkspaceActivity` 新增通用 `EXTRA_SHOW_BACK`，旧 `EXTRA_SHOW_BACK_TO_TERMINAL` 继续兼容。
2. `TermuxActivity` 的工作台入口改用通用返回字段。
3. `AiCliSessionCenterActivity` 新增 `openWorkspaceWithBack()`，所有打开工作区或无效工作区兜底路径统一携带返回字段。
4. `WorkspaceActivity.onNewIntent()` 重新读取返回字段，覆盖 `REORDER_TO_FRONT` 复用已有工作台实例的场景。
5. `AiCliSessionCenterActivityTest` 断言从 AI 会话中心进入工作区必须携带返回字段。
6. `WorkspaceActivitySmokeTest` 同时覆盖新通用字段、旧终端字段和复用实例的新 Intent。

## 验收标准

- 桌面直接打开工作区：不显示多余返回按钮。
- 从终端进入工作区：显示返回按钮，点击后关闭工作区返回终端。
- 从 AI CLI 会话中心进入工作区：显示返回按钮，避免用户被困在配置页。
- AI CLI 会话中心在工作区无效时的 tmux/Git/诊断/项目任务/AI 启动兜底路径，也必须显示工作区返回按钮。
- 旧终端字段仍可用，避免升级后已有入口行为变化。

## 设备与证据限制

当前远程服务器 KVM 不可用，本轮不启动本机 Android 模拟器，避免影响共享机器资源。运行时触摸、输入法、
厂商 ROM 和真实设备返回栈手感不在本地证据范围内；本轮以 Robolectric Intent/视图状态和后续 GitHub
CI 作为低资源验收证据。
