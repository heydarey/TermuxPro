# 工作区导航与删除闭环回归（2026-09-09）

## 背景

用户在 `0.10.0` 正式版体验中反馈：

- 首页进入“编辑连接”后缺少明确返回入口，容易被困在编辑表单。
- 新建但未配置服务器的工作区无法删除，新增动作没有形成完整撤销/删除闭环。
- 从终端进入工作台时，只要存在上一页语义，都应显示返回按钮。

## 本轮修复

- 工作区编辑态统一显示左上角返回按钮，不再只限“已配置连接”的编辑态。
- 新建未配置工作区在编辑态允许删除；删除后回到剩余工作区摘要，不保留无效空配置。
- 保留最后一个已保存工作区删除后的安全兜底：清空持久化目标，回到空白可配置状态，不写入无效 SSH 目标。

## 验证

已新增并通过 Robolectric 用例：

- `unconfiguredWorkspaceEditorStillShowsBackControl`
- `newlyCreatedUnconfiguredWorkspaceCanBeDeleted`

已通过命令：

```bash
source ./scripts/resolve-jdk17.sh
TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.WorkspaceActivitySmokeTest
```

## 非目标

- 不修改 Termux 原始终端滚动、PTY、包管理或本地 shell 行为。
- 不发布正式包；本轮为 `dev` 日常迭代切片，待累计足够用户可见质量提升后再进入候选版发布。
