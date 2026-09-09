# 终端 URL 选择弹窗深色可读性补强

日期：2026-09-08

## 用户问题

用户多次反馈 0.x 体验版本仍有深色页面/弹窗文字看不清。虽然主要增值页已接入
`TermuxProDialogStyle`，但终端高频入口仍存在手写 `setOnShowListener` 与 `dialog.show()` 的历史代码，
后续容易绕过统一样式。

## 增值服务准入

- 分类：原始能力兼容守护、移动端工作流效率。
- 处理口径：不重写 Termux 原有“选择 URL”能力，只保证 TermuxPro 深色主题和移动端可读性不破坏原始功能。

## 改动

- 终端“选择 URL”弹窗改为通过 `TermuxProDialogStyle.show()` 展示。
- 保留原有点击复制、长按打开 URL 行为。
- `test/dialog-readable-style-test.sh` 扩展覆盖 `TermuxTerminalViewClient.java`，禁止该高频终端客户端继续手写
  `setOnShowListener` 或直接 `dialog.show()`。

## 验收

- `./test/dialog-readable-style-test.sh`：通过。
- 静态复查：`TermuxTerminalViewClient.java` 不再包含手写 `setOnShowListener` 或直接 `dialog.show()`。

## 回归面

- URL 自动提取、点击复制、长按打开 URL。
- 深色主题弹窗标题、列表项、按钮和背景色。
- 原始终端输入/滚动/PTY 行为不得被修改。
