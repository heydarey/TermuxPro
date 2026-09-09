# Git 提交前上下文确认回归（2026-09-08）

## 增值服务准入

- 分类：Git 可视化、远程工作区、移动端工作流效率。
- 非目标：不重做 Git 命令、不改变 Termux 原始终端/PTY/本地 shell 行为。

## 用户问题

手机端同时连接多个服务器、多个项目或多个分支时，提交是高风险写操作。旧确认弹窗只提示已暂存和未暂存
文件数量，没有在最终确认点展示“本次提交会落到哪台机器、哪个目录、哪个分支”，用户需要回忆上一屏
上下文，容易误提交。

## 本轮改动

- Git 工作台“提交已暂存”确认弹窗新增当前分支。
- 同一弹窗新增远程目标：`host:port · path`。
- 保留既有安全边界：只提交已暂存文件，不自动暂存、不推送远端、不改变未暂存文件。

## 验收

- Robolectric 覆盖提交弹窗中分支、主机、端口和项目路径。
- 该切片只增强 Git 可视化增值层，不修改远端 Git 命令构造或 Termux 原始终端能力。

## 已执行验证

```text
source ./scripts/resolve-jdk17.sh
TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest
```

待执行：推送前冒烟、GitHub CI 和模拟器 UI（如路径门禁要求）。
