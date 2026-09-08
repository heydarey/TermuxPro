# Git 提交详情移动端切片验收记录

## 选题与边界

- 增值服务分类：Git 可视化、远程工作区、移动端工作流效率。
- 用户旅程：开发者在手机 Git 工作台看见近期提交后，需要确认某一条提交究竟由谁、何时、改了哪些文件，
  不应返回终端手输 `git show`。
- 非目标：不新增提交、推送、切分支、重置、变基或历史改写；不改动 Termux 原始终端、PTY 或本地 shell。

## 交互与安全决策

1. “提交记录”首先呈现可读列表：短哈希、相对时间和主题；标题下明确说明点选后仅查看详情。
2. 点选一条后在 Git 工作台内容区展示 `git show --format=fuller --stat` 的作者、精确时间、文件统计与改动。
3. 提交对象只能来自当前概览协议，命令构造器额外限制为 7–40 位十六进制哈希，再执行 Git 对象验证；任意
   分支名、Shell 片段或路径参数都不能进入该读取命令。
4. 详情失败明确说明当前分支、工作树和远端仓库没有变化；返回键与刷新键仍回到或重读当前详情，不制造假成功。

## 本地回归证据

- `git diff --check`：通过。
- `./scripts/validate-skills.sh`：通过。
- `./test/dialog-readable-style-test.sh`：通过，提交选择弹窗继续复用深色可读样式门禁。
- `:app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest --tests com.termux.app.WorkspaceCommandBuilderTest --rerun-tasks`：通过。
  - `GitDiffActivityTest`：9 项，0 failure / 0 error；覆盖提交选择列表、中文只读说明、深色取消按钮和条目内容。
  - `WorkspaceCommandBuilderTest`：28 项，0 failure / 0 error；覆盖受限哈希、对象验证、`git show` 只读参数和
    非法参数拒绝。

## 待远端验收

- 分支 CI、Emulator UI、自动 PR 与合并后 dev 收尾 CI 通过后补充运行编号。
