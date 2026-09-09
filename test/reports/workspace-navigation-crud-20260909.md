# 工作区导航与 CRUD 闭环回归

## 背景

用户在 `0.10.0` 正式版体验中反馈：

1. 首页点击“编辑连接”后页面没有返回按钮，用户无法直观看到如何回到摘要态。
2. 新建或空工作区无法删除，删除后仍被强制保留为空配置。
3. 从终端进入工作台时，工作台左上角缺少上一页返回入口。

这些问题属于 TermuxPro 增值层的移动端工作区 UX，不涉及重做 Termux 原始终端能力。

## 产品判断

- 允许进入编辑态，就必须提供保存、放弃修改、返回摘要的闭环。
- 允许新增工作区，就必须允许删除；最后一个工作区删除后应清空已保存目标，而不是持久化一个空工作区。
- 从终端等上一页进入工作台时，应显示明确返回入口；直接从桌面启动时不增加首屏噪声。

## 本轮改动

- 工作台顶部增加动态“返回”入口。
- 从终端点击“工作台”进入时传入来源标记，工作台显示左上角返回。
- 已配置工作区进入“编辑连接”后显示返回；有未保存修改时先确认放弃，再回到摘要态。
- 删除最后一个已保存工作区时清空 `profiles_v2`、`active_profile` 和旧版连接字段；界面只保留未保存草稿表单，`WorkspaceTargetStore.readActive` 返回空。
- 编辑态显示删除按钮，确保新建后误建的工作区也能删除。

## 回归用例

- `WorkspaceActivitySmokeTest.savedWorkspaceCollapsesEditorIntoConnectionSummary`
- `WorkspaceActivitySmokeTest.editingExistingWorkspaceCanReturnToSummaryWithoutLeavingPage`
- `WorkspaceActivitySmokeTest.workspaceOpenedFromTerminalShowsBackToPreviousPage`
- `WorkspaceActivitySmokeTest.deletingLastWorkspaceClearsSavedTargetInsteadOfKeepingEmptyProfile`
- `WorkspaceTargetStoreTest`

## 本地验证

```text
TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 \
  :app:testDebugUnitTest \
  --tests com.termux.app.WorkspaceActivitySmokeTest \
  --tests com.termux.app.WorkspaceTargetStoreTest

BUILD SUCCESSFUL in 48s
```

## 后续主动扫描准则

所有 TermuxPro 增值页继续按以下规则走查：

- 由上一页进入的页面必须有明确返回入口。
- 新增/复制/创建对象必须有删除、取消或恢复默认路径。
- 空态不能伪装成已保存对象。
- 危险动作不能和普通动作同视觉层级。
- 直接桌面启动路径不因返回入口增加首屏噪声。
