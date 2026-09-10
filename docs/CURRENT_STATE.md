# TermuxPro 当前迭代检查点

> 本文件由 `scripts/update-context-checkpoint.sh` 生成。它不是发布说明，也不替代测试报告；
> 作用是在 Codex/Claude 会话恢复、模型切换或平台上下文压缩后，快速恢复当前事实。

## 生成时间

- UTC：2026-09-10T04:19:40Z

## 当前目标

持续维护 TermuxPro 增值服务：Android SSH 远程开发、Codex CLI/Claude Code 使用体验、
tmux/Git 可视化管理、自定义快捷指令、多工作区、终端上下文工具箱和移动端交互体验。
Termux 原始终端/PTY/包管理/本地 shell/基础会话/基础快捷键/基础文件能力只做“不受影响”兼容回归。

## 代码状态

- 当前分支：`dev_dailyIteration`
- 当前提交：`40c2fd441ca0`
- 最近提交：Merge pull request #294 from heydarey/dev_dailyIteration
- `origin/dev`：`40c2fd441ca0`
- `origin/master`：`43c7a5794fc8`
- 版本源：`0.10.0` / `100002`

### 工作树

```text
 M app/src/main/java/com/termux/app/TermuxActivity.java
 M app/src/main/res/layout/activity_termux.xml
 M app/src/main/res/values-zh-rCN/strings.xml
 M app/src/main/res/values/strings.xml
 M app/src/test/java/com/termux/app/CustomLayoutsSmokeTest.java
 M app/src/test/java/com/termux/app/TerminalProjectToolsMenuTest.java
 M docs/PRODUCT_BACKLOG.md
?? test/reports/terminal-touch-scroll-discoverability-20260909.md
```

## 额度与资源

### Codex 额度

```json
{
  "latest": {
    "timestamp": "2026-09-10T04:19:36.252Z",
    "usedPercent": 25,
    "remainingPercent": 75,
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
CPU：8 核，1 分钟负载：2.31
可用内存：12383 MiB
工作区可用磁盘：12340 MiB
工作区磁盘使用率：94%
KVM：不可用
资源守卫通过：保持单个重任务，Gradle 使用 --max-workers=2。
```

## GitHub 状态

### 打开的 dev PR

```json
当前没有以 dev 为目标的打开 PR。
```

### 最近 dev CI

```json
[{"conclusion":"success","createdAt":"2026-09-10T04:08:08Z","databaseId":34435983138,"displayTitle":"TermuxPro CI","headSha":"40c2fd441ca09b645542bb41ba1eb2d0dc97778b","status":"completed"},{"conclusion":"success","createdAt":"2026-09-10T03:49:40Z","databaseId":34434810532,"displayTitle":"TermuxPro CI","headSha":"c6119ad5361fa4445658751de3be64601b3c7589","status":"completed"},{"conclusion":"success","createdAt":"2026-09-10T03:30:22Z","databaseId":34433581438,"displayTitle":"TermuxPro CI","headSha":"04acc3ecfa1bf1f870508ef86a779caefb9565d8","status":"completed"}]
```

### 最近 Release

```text
TermuxPro v0.10.0	Latest	v0.10.0	2026-09-09T05:00:08Z
TermuxPro v0.10.0-rc.1	Pre-release	v0.10.0-rc.1	2026-09-09T04:16:32Z
TermuxPro v0.9.9-rc.1	Pre-release	v0.9.9-rc.1	2026-09-08T06:32:09Z
TermuxPro v0.9.8-rc.1	Pre-release	v0.9.8-rc.1	2026-09-08T04:47:37Z
TermuxPro v0.9.7		v0.9.7	2026-09-08T03:30:54Z
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
