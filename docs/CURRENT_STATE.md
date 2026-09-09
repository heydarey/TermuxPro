# TermuxPro 当前迭代检查点

> 本文件由 `scripts/update-context-checkpoint.sh` 生成。它不是发布说明，也不替代测试报告；
> 作用是在 Codex/Claude 会话恢复、模型切换或平台上下文压缩后，快速恢复当前事实。

## 生成时间

- UTC：2026-09-09T02:54:24Z

## 当前目标

持续维护 TermuxPro 增值服务：Android SSH 远程开发、Codex CLI/Claude Code 使用体验、
tmux/Git 可视化管理、自定义快捷指令、多工作区、终端上下文工具箱和移动端交互体验。
Termux 原始终端/PTY/包管理/本地 shell/基础会话/基础快捷键/基础文件能力只做“不受影响”兼容回归。

## 代码状态

- 当前分支：`dev_contextCheckpoint_20260908`
- 当前提交：`346d340fdbc6`
- 最近提交：chore(agent): 增加上下文检查点机制
- `origin/dev`：`71748aaafad2`
- `origin/master`：`1721cfb6627a`
- 版本源：`0.9.9-rc.1` / `90901`

### 工作树

```text
干净
```

## 额度与资源

### Codex 额度

```json
{
  "latest": {
    "timestamp": "2026-09-09T02:54:18.402Z",
    "usedPercent": 13,
    "remainingPercent": 87,
    "resetsAt": 1789435930,
    "windowMinutes": 10080
  },
  "minimumRemainingPercent": 15,
  "allowed": true,
  "policy": "不按自然日限额；总剩余额度不少于阈值时持续运行"
}
```

### 共享服务器资源

```text
环境：远程/共享
CPU：8 核，1 分钟负载：3.46
可用内存：13512 MiB
工作区可用磁盘：17567 MiB
工作区磁盘使用率：91%
KVM：不可用
资源守卫通过：保持单个重任务，Gradle 使用 --max-workers=2。
```

## GitHub 状态

### 打开的 dev PR

```json
[{"headRefName":"dev_gitReviewPriority_20260908","number":248,"title":"feat(git): 优先展示修改审查入口","url":"https://github.com/heydarey/TermuxPro/pull/248"}]
```

### 最近 dev CI

```json
[{"conclusion":"success","createdAt":"2026-09-09T02:44:26Z","databaseId":34304412347,"displayTitle":"TermuxPro CI","headSha":"71748aaafad2380f2b7c3fff1682d6ba3aaa1963","status":"completed"},{"conclusion":"success","createdAt":"2026-09-08T13:11:44Z","databaseId":34230478883,"displayTitle":"TermuxPro CI","headSha":"ad9473390e8ae4b1c9bead7d0aab0ecf9050695d","status":"completed"},{"conclusion":"success","createdAt":"2026-09-08T12:58:43Z","databaseId":34229181186,"displayTitle":"TermuxPro CI","headSha":"d090041dab9af8a157453cf2e40b3778ae03f6b6","status":"completed"}]
```

### 最近 Release

```text
TermuxPro v0.9.9-rc.1	Pre-release	v0.9.9-rc.1	2026-09-08T06:32:09Z
TermuxPro v0.9.8-rc.1	Pre-release	v0.9.8-rc.1	2026-09-08T04:47:37Z
TermuxPro v0.9.7	Latest	v0.9.7	2026-09-08T03:30:54Z
TermuxPro v0.9.7-rc.1	Pre-release	v0.9.7-rc.1	2026-09-08T02:51:40Z
TermuxPro v0.9.6		v0.9.6	2026-09-02T01:43:58Z
```

## 恢复步骤

1. 读取 `AGENTS.md` 和 `.agents/skills/termuxpro-development/SKILL.md`。
2. 运行 `./scripts/codex-quota-guard.sh`，只有总剩余额度低于 15% 才暂停主动迭代。
3. 运行 `./scripts/resource-guard.sh`，共享服务器上保持单个重任务，Gradle 使用 `--max-workers=2`。
4. 检查 `git status --short --branch`、打开 PR、最近 CI 和 Release 状态。
5. 从 `docs/PRODUCT_BACKLOG.md` 中最高优先级的 TermuxPro 增值服务切片继续。

## 安全红线

- 禁止执行默认 `tmux kill-server`；隔离测试必须移除继承的 `TMUX` 并使用 `tmux -L` 或 `tmux -S`。
- 禁止提交 `.signing/`、私钥、密码、AI Token、公司源码和未脱敏终端输出。
- 不修改系统级 JDK、SDK、PATH、服务、软件源或其他用户文件。
- 普通分支、提交、PR、CI 和小改动默认不发送飞书通知。
