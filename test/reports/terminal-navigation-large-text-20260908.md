# 终端导航大字体可达性审计

## 截图发现

- 增值服务分类：移动端工作流效率、AI CLI、终端上下文工具箱。
- `termuxpro-emulator-ui-244` 的 200% 字体截图显示，旧顶栏把“输入 / Claude / Codex / 工具箱”塞进横向
  滚动区；首屏只看到“输入 / Claude”，Codex 与工具箱被裁切。抽屉底部“新建会话”也只显示为不完整标签。
- 这会让用户误以为功能缺失，并直接违背“无需猜测即可找到会话、工作台和工具箱”的核心旅程。

## 修复设计

1. 顶栏收敛为始终可见的“会话 / 工作台 / AI / 工具箱”。AI 打开会话中心，由该页明确提供 Claude 与
   Codex 的安全默认新建；不再在顶栏横向堆叠多个会被裁切的命令按钮。
2. 多行提示编辑没有删除，迁入工具箱“当前上下文”分组，保留手机软键盘编辑能力。
3. 抽屉底部采用短可见标签“键盘 / 关闭 / 新建”，同时保留完整 TalkBack `contentDescription`，并使用
   `0dp + layout_weight` 使三项均分空间。
4. 真实 Android 截图测试增加顶栏四项与抽屉“新建”按钮的可见区域断言；任一控件被裁切到半高以下即失败。

## 本地验证

- `:app:testDebugUnitTest --tests com.termux.app.CustomLayoutsSmokeTest --tests
  com.termux.app.TerminalProjectToolsMenuTest --rerun-tasks`：9 项，0 failure / 0 error。
- `test/terminal-touch-scrollback-test.sh`、`scripts/validate-skills.sh` 与 `git diff --check`：通过。

## 远端验收结论

- 研发 PR #234：`fix(terminal): 修复大字体导航裁切` 已合并至 `dev`，合并提交
  `3472aa43b5ae2c1e58b5f8e34dd9bec7b91f17ca`。
- 分支完整 CI `34209567840`：通过；覆盖隔离 SSH/tmux fixture、全模块测试、Lint 和 Debug APK 构建。
- 360dp 深色 Emulator UI `34209567949`：通过；默认字体与 200% 字体的
  `captureCriticalDarkPages` 均为 1 test / 0 failure，验收产物为
  `termuxpro-emulator-ui-246`。
- 人工截图复核：默认与 200% 字体下，“会话 / 工作台 / AI / 工具箱”均在无需横向滚动的首屏完整可见；
  抽屉底部“键盘 / 关闭 / 新建”完整显示。200% 字体下正文可正常换行，未挤压顶部关键入口。
- `dev` 合并后收尾 CI `34210411176`：通过；自动 PR 收尾 `34209567804`：通过。

结论：通过。旧版横向滚动导致的首屏关键入口裁切已关闭；后续页面变更仍必须保留该截图和可视区域断言。
