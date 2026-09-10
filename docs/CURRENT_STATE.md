# TermuxPro 当前迭代检查点

> 本文件由 `scripts/update-context-checkpoint.sh` 生成。它不是发布说明，也不替代测试报告；
> 作用是在 Codex/Claude 会话恢复、模型切换或平台上下文压缩后，快速恢复当前事实。

## 生成时间

- UTC：2026-09-10T07:07:38Z

## 当前目标

持续维护 TermuxPro 增值服务：Android SSH 远程开发、Codex CLI/Claude Code 使用体验、
tmux/Git 可视化管理、自定义快捷指令、多工作区、终端上下文工具箱和移动端交互体验。
Termux 原始终端/PTY/包管理/本地 shell/基础会话/基础快捷键/基础文件能力只做“不受影响”兼容回归。

## 代码状态

- 当前分支：`dev_dailyIteration`
- 当前提交：`20496906446c`
- 最近提交：Merge pull request #303 from heydarey/dev_dailyIteration
- `origin/dev`：`20496906446c`
- `origin/master`：`43c7a5794fc8`
- 版本源：`0.10.0` / `100002`

### 工作树

```text
 M app/src/main/java/com/termux/app/TerminalFeedbackController.java
 M app/src/main/java/com/termux/app/TermuxActivity.java
 M app/src/main/res/values-zh-rCN/strings.xml
 M app/src/main/res/values/strings.xml
 M app/src/test/java/com/termux/app/TerminalFeedbackControllerTest.java
 M docs/PRODUCT_BACKLOG.md
?? test/reports/terminal-touch-scroll-persistent-hint-20260910.md
```

## 额度与资源

### Codex 额度

```json
{
  "latest": {
    "timestamp": "2026-09-10T07:07:30.380Z",
    "usedPercent": 27,
    "remainingPercent": 73,
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
CPU：8 核，1 分钟负载：2.39
可用内存：10553 MiB
工作区可用磁盘：10637 MiB
工作区磁盘使用率：95%
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
[{"conclusion":"success","createdAt":"2026-09-10T06:54:58Z","databaseId":34447395784,"displayTitle":"TermuxPro CI","headSha":"20496906446c9d6ae023f8187012c31b515f7af1","status":"completed"},{"conclusion":"success","createdAt":"2026-09-10T06:34:03Z","databaseId":34445778327,"displayTitle":"TermuxPro CI","headSha":"6df270c893e4c123bb0c0fa3a4217820cac850fb","status":"completed"},{"conclusion":"success","createdAt":"2026-09-10T06:16:24Z","databaseId":34444479119,"displayTitle":"TermuxPro CI","headSha":"6921c7a90a2c687b548b430917c853965297c837","status":"completed"}]
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
