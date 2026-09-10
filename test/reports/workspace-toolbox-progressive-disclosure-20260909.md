# 首页工具箱渐进展示验收

## 背景

用户真实体验反馈指出：首页不应该平铺一大堆低频控件。核心路径是先选择服务器和项目，再打开远程终端或
启动 Claude/Codex；Git、文件、项目任务、tmux、诊断、SSH 密钥和 Web 预览应作为终端开发中的按需工具。

## 调整

- 已保存工作区首页默认只展示连接摘要、打开远程终端、AI CLI 快速启动和一个明确的“打开工具箱”入口。
- `开发工具`、`Web 项目预览` 和 `打开本地终端` 默认隐藏，用户点击“打开工具箱”后才展开，再次点击可收起。
- 未配置 SSH 或正在编辑连接时不显示工具箱，避免用户在无效目标上误点 Git/tmux/文件等远程工具。
- 进入编辑、切换/重建工作区绑定或保存后，工具箱展开状态自动清空，避免上一个上下文的展开状态污染新的首页。
- 不改变 SSH 命令、tmux 策略、Git 写操作边界、Web 隧道命令或 Termux 原始终端能力。

## 验收

- 首次或未配置状态：工具箱入口和低频工具均隐藏。
- 已保存工作区：工具箱入口可见，低频工具默认隐藏。
- 点击“打开工具箱”：Git/文件/任务/tmux/诊断/SSH Key/Web 预览/本地终端入口可见。
- 点击“收起工具箱”：低频工具再次隐藏，首页回到连接优先状态。
- 展开工具箱后进入“编辑连接”再保存：回到摘要页时仍为折叠状态。

## 本地验证

```text
source ./scripts/resolve-jdk17.sh && TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.WorkspaceActivitySmokeTest

BUILD SUCCESSFUL in 31s
```

工具箱状态隔离补测：

```text
source ./scripts/resolve-jdk17.sh && TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.WorkspaceActivitySmokeTest

BUILD SUCCESSFUL in 31s
```

```text
./scripts/update-context-checkpoint.sh && git diff --check && source ./scripts/resolve-jdk17.sh && TERMUXPRO_USE_CHINA_MIRROR=1 ./scripts/pre-push-smoke.sh origin/dev

BUILD SUCCESSFUL in 29s
```
