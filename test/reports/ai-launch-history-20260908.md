# AI CLI 启动记录切片验收

日期：2026-09-08

## 用户问题

用户的核心场景是手机 SSH 到远程服务器后使用 Codex CLI 和 Claude Code，并希望能看到历史会话、进入指定会话、
做增删改查。直接读取 Claude/Codex 私有历史存在共享账号泄露和误续接风险，尤其 Claude Code 在共享账号中
不是用户级隔离。

## 增值服务准入

- 分类：AI CLI、远程工作区、移动端工作流效率。
- 增值层：TermuxPro 在 AI 会话中心展示“本应用发起过的 AI 启动记录”，帮助用户回到最近一次工作方式。
- 非目标：不读取 Claude/Codex 内部历史目录，不解析远端账号的私有会话元数据，不保存终端输出、Prompt、
  AI Token 或公司源码。

## 设计决策

1. 启动记录按工作区 ID 隔离，同一手机上不同服务器/项目互不展示。
2. 只记录 TermuxPro 自己触发的工具、模式、工作区名、SSH 目标和项目路径。
   - 覆盖入口：工作区首页 AI 快捷启动、终端工具箱 AI 快捷启动、AI CLI 会话中心。
   - 普通“打开远程终端”不记入 AI 历史，避免把纯 SSH 操作混入 AI 工作流。
3. AI 会话中心展示最近 3 条记录，并提供“重复上次”“删除最近”和“清空记录”三个闭环操作。
4. “重复上次”仍基于当前工作区重新构造 `ssh_only` 启动命令，不自动进入 tmux，不自动恢复最近会话。
5. “清空记录”只删除当前工作区的本地启动元数据，不影响远端 Claude/Codex 历史或 tmux 会话。

## 验收

- `AiLaunchHistoryStoreTest`：
  - 按工作区隔离记录。
  - 每个工作区最多保留最近 5 条。
  - 清空当前工作区不影响其他工作区。
- `AiLaunchRecorderTest`：
  - 无有效工作区时不创建启动记录。
  - 有效工作区下可由任意 TermuxPro AI 入口统一写入同一工作区历史。
  - 可按工作区和启动时间删除单条记录，不影响同工作区其他记录。
- `AiCliSessionCenterActivityTest`：
  - 无有效工作区时提示记录按工作区隔离。
  - 启动 Claude 历史后生成本地启动记录。
  - 可从启动记录“重复上次”进入独立 SSH only 终端。
  - 可删除最近一条错误启动记录，保留同工作区其他记录。
  - 可清空当前工作区记录。
- 本地命令：
  `TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.AiLaunchHistoryStoreTest --tests com.termux.app.AiLaunchRecorderTest --tests com.termux.app.AiCliSessionCenterActivityTest`

## 回归面

- Claude/Codex 新建与历史选择命令不变。
- AI 快捷启动仍不自动进入 tmux。
- 记录展示不能泄露 AI 私有历史、终端输出或用户 Prompt。
- 多工作区切换后只能看到当前工作区的启动记录。
- 工作区首页、终端工具箱和 AI CLI 会话中心的 AI 启动记录必须一致进入同一历史存储。
- 删除最近与清空记录只处理本地非敏感元数据，不删除 Claude/Codex 历史、远端文件或 tmux 会话。
