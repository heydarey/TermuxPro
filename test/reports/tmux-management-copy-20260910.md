# tmux 管理入口与操作文案审计

## 背景

用户在 `0.10.0` 体验中指出 tmux 页面“只能查看历史会话，无法 CRUD”。代码层已经具备当前工作区
TermuxPro 会话的新建、进入、重命名和停止能力，并对其他工作区、其他使用者或归属未知会话保持只读
保护；问题集中在入口和页面文案仍叫“tmux 会话”，容易让用户理解成只读列表。

## 本轮改动

- 工作区首页和终端工具箱入口统一从“tmux 会话 / tmux sessions”改为“tmux 管理 / tmux manager”。
- tmux 页面标题统一改为“tmux 管理”，表达这是会话生命周期管理入口，而不是只读历史列表。
- 新建按钮从“新建会话”改为“新建当前工作区会话”，明确创建范围，避免共享账号下误建到不清楚的上下文。
- 就绪说明改为直接说明当前页支持新建、进入、重命名和停止；同时强调只有当前工作区 TermuxPro
  会话可重命名或停止，非当前归属仍只能确认进入。

## 安全边界

- 本轮不新增远端删除能力，不扩大已有停止范围。
- 停止仍只通过 `WorkspaceCommandBuilder.buildStopTaskSessionRemoteCommand(...)` 针对单个匹配 owner
  与工作区指纹的会话执行。
- 未触碰真实 tmux，会话测试仍必须使用隔离套接字，禁止默认 `tmux kill-server`。

## 验收

- `TaskSessionsActivityTest.pageCopyCommunicatesTmuxManagementInsteadOfReadOnlyList`
  验证页面标题、创建按钮和可管理说明。
- `TaskSessionsActivityTest.ownedSessionSeparatesDangerActionAndUnknownSessionHasAttachOnly`
  继续验证自有会话的危险操作隔离，以及非当前归属会话只允许确认进入。

## 结论

通过低资源 JVM/Robolectric 验证后，本切片可证明页面文案和操作边界更符合用户对 CRUD 闭环的认知。
由于本机 KVM 不可用，本轮不声明完整设备验收；正式候选版本仍需 GitHub Runner 模拟器或真实/云设备补
触摸、字体和页面可见性证据。
