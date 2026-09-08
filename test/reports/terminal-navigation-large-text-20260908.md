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

## 待远端验收

- 必须通过研发 CI 和 360dp 深色默认/200% 字体 Emulator UI，并人工复核顶栏所有关键入口及抽屉底部操作。
