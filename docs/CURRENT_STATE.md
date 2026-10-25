# TermuxPro 当前迭代检查点

> 本文件由 `scripts/update-context-checkpoint.sh` 生成。它不是发布说明，也不替代测试报告；
> 作用是在 Codex/Claude 会话恢复、模型切换或平台上下文压缩后，快速恢复当前事实。

## 生成时间

- UTC：2026-10-25T13:08:06Z

## 当前目标

持续维护 TermuxPro 增值服务：Android SSH 远程开发、Codex CLI/Claude Code 使用体验、
tmux/Git 可视化管理、自定义快捷指令、多工作区、终端上下文工具箱和移动端交互体验。
Termux 原始终端/PTY/包管理/本地 shell/基础会话/基础快捷键/基础文件能力只做“不受影响”兼容回归。

## 代码状态

- 当前分支：`dev_dailyIteration`
- 当前提交：`34701c14290b`
- 最近提交：chore(release): 发布0.10.2正式版
- `origin/dev`：`34701c14290b`
- `origin/master`：`34701c14290b`
- 版本源：`0.10.2` / `100104`

### 工作树

```text
干净
```

## 额度与资源

### Codex 额度

```json
{
  "driver": "zcode-batch-20261025",
  "codexQuotaObservable": false,
  "allowed": true,
  "policy": "非 Codex 驱动没有 Codex 额度概念；资源守卫与发布门禁仍然适用"
}
```

### 共享服务器资源

```text
环境：远程/共享
CPU：8 核，1 分钟负载：0.10
可用内存：15253 MiB
工作区可用磁盘：22976 MiB
工作区磁盘使用率：89%
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
[{"conclusion":"","createdAt":"2026-10-25T13:06:03Z","databaseId":40196073823,"displayTitle":"chore(release): 发布0.10.2正式版","headSha":"34701c14290bde067609f240332a61752f91a736","status":"in_progress"},{"conclusion":"success","createdAt":"2026-10-25T12:50:38Z","databaseId":40194914406,"displayTitle":"chore(release): 发布 0.10.2 正式版","headSha":"c4afe721987c8ac107b0ba367efd3b18237b0aac","status":"completed"},{"conclusion":"success","createdAt":"2026-10-25T12:50:35Z","databaseId":40194910165,"displayTitle":"TermuxPro CI","headSha":"c4afe721987c8ac107b0ba367efd3b18237b0aac","status":"completed"}]
```

### 最近 Release

```text
TermuxPro v0.10.2	Latest	v0.10.2	2026-10-25T13:04:51Z
TermuxPro v0.10.2-rc.1	Pre-release	v0.10.2-rc.1	2026-10-25T12:27:23Z
TermuxPro v0.10.1		v0.10.1	2026-09-17T08:17:31Z
TermuxPro v0.10.1-rc.1	Pre-release	v0.10.1-rc.1	2026-09-17T07:38:52Z
TermuxPro v0.10.0		v0.10.0	2026-09-09T05:00:08Z
```

### 稳定版发布窗口

```text
status=OK action=continue_daily_iteration latestTag=v0.10.2 publishedAt=2026-10-25T13:04:51Z ageDays=0 ageHours=0 reviewAfter=2026-10-30T13:04:51Z deadline=2026-11-01T13:04:51Z
```

## 恢复步骤

1. 读取 `AGENTS.md` 和 `.agents/skills/termuxpro-development/SKILL.md`。
2. 运行 `./scripts/codex-quota-guard.sh`，只有总剩余额度低于 15% 才暂停主动迭代。
3. 运行 `./scripts/resource-guard.sh`，共享服务器上保持单个重任务，Gradle 使用 `--max-workers=2`。
4. 检查 `git status --short --branch`、打开 PR、最近 CI 和 Release 状态。
5. 运行或读取 `./scripts/release-window-guard.sh` 结果；若稳定版已满 5 天，优先做发布评审；若已满
   7 天，先发布正式版或落盘有证据的 HOLD，不继续普通 P2/P3 切片。少发版不是不发版，连续评审几天
   无 P0/P1 且已有用户可感知价值时，必须推进一周至少一次正式稳定版发布。
6. 从 `docs/PRODUCT_BACKLOG.md` 中最高优先级的 TermuxPro 增值服务切片继续。

## 安全红线

- 禁止执行默认 `tmux kill-server`；隔离测试必须移除继承的 `TMUX` 并使用 `tmux -L` 或 `tmux -S`。
- 禁止提交 `.signing/`、私钥、密码、AI Token、公司源码和未脱敏终端输出。
- 不修改系统级 JDK、SDK、PATH、服务、软件源或其他用户文件。
- 普通分支、提交、PR、CI 和小改动默认不发送飞书通知。
