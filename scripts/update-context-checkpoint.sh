#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
output_file="$project_dir/docs/CURRENT_STATE.md"
mode="write"

usage() {
    cat <<'EOF'
用法：scripts/update-context-checkpoint.sh [--stdout|--output <file>]

用途：
  生成 TermuxPro 日常迭代的仓库级上下文检查点。用于 Codex/Claude 会话恢复、
  模型切换或平台上下文压缩后快速回到当前事实状态。
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --stdout)
            mode="stdout"
            shift
            ;;
        --output)
            output_file="${2:?缺少 --output 参数值}"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "未知参数：$1" >&2
            usage >&2
            exit 64
            ;;
    esac
done

cd "$project_dir"

timestamp="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
branch="$(git branch --show-current 2>/dev/null || printf 'unknown')"
head_sha="$(git rev-parse --short=12 HEAD 2>/dev/null || printf 'unknown')"
head_subject="$(git log -1 --pretty=%s 2>/dev/null || printf 'unknown')"
status_exclude="${output_file#$project_dir/}"
status_porcelain="$(git status --short 2>/dev/null | awk -v excluded="$status_exclude" '
    $2 != excluded && $2 !~ (" -> " excluded "$") { print }
' || true)"
if [[ -z "$status_porcelain" ]]; then
    status_summary="干净"
else
    status_summary="$(printf '%s\n' "$status_porcelain" | sed -n '1,40p')"
fi

origin_dev="$(git rev-parse --short=12 origin/dev 2>/dev/null || printf 'unknown')"
origin_master="$(git rev-parse --short=12 origin/master 2>/dev/null || printf 'unknown')"
version_name="$(./scripts/termuxpro-version.sh name 2>/dev/null || printf 'unknown')"
version_code="$(./scripts/termuxpro-version.sh code 2>/dev/null || printf 'unknown')"

quota_json="不可观测"
if quota_output="$(./scripts/codex-quota-guard.sh 2>&1)"; then
    quota_json="$quota_output"
else
    quota_json="$quota_output"
fi

resource_summary="未执行"
if resource_output="$(./scripts/resource-guard.sh 2>&1)"; then
    resource_summary="$resource_output"
else
    resource_summary="$resource_output"
fi

open_prs="GitHub CLI 不可用或网络不可用，恢复后需重新查询。"
if pr_output="$(./scripts/github-cli.sh pr list --base dev --state open \
    --json number,title,headRefName,url 2>/dev/null)"; then
    if [[ "$pr_output" == "[]" ]]; then
        open_prs="当前没有以 dev 为目标的打开 PR。"
    else
        open_prs="$pr_output"
    fi
fi

latest_dev_ci="GitHub CLI 不可用或网络不可用，恢复后需重新查询。"
if ci_output="$(./scripts/github-cli.sh run list --branch dev --workflow "TermuxPro CI" \
    --limit 3 --json databaseId,status,conclusion,headSha,createdAt,displayTitle 2>/dev/null)"; then
    latest_dev_ci="$ci_output"
fi

latest_release="GitHub CLI 不可用或网络不可用，恢复后需重新查询。"
if release_output="$(./scripts/github-cli.sh release list --limit 5 2>/dev/null)"; then
    latest_release="$release_output"
fi

checkpoint="$(
cat <<EOF
# TermuxPro 当前迭代检查点

> 本文件由 \`scripts/update-context-checkpoint.sh\` 生成。它不是发布说明，也不替代测试报告；
> 作用是在 Codex/Claude 会话恢复、模型切换或平台上下文压缩后，快速恢复当前事实。

## 生成时间

- UTC：$timestamp

## 当前目标

持续维护 TermuxPro 增值服务：Android SSH 远程开发、Codex CLI/Claude Code 使用体验、
tmux/Git 可视化管理、自定义快捷指令、多工作区、终端上下文工具箱和移动端交互体验。
Termux 原始终端/PTY/包管理/本地 shell/基础会话/基础快捷键/基础文件能力只做“不受影响”兼容回归。

## 代码状态

- 当前分支：\`$branch\`
- 当前提交：\`$head_sha\`
- 最近提交：$head_subject
- \`origin/dev\`：\`$origin_dev\`
- \`origin/master\`：\`$origin_master\`
- 版本源：\`$version_name\` / \`$version_code\`

### 工作树

\`\`\`text
$status_summary
\`\`\`

## 额度与资源

### Codex 额度

\`\`\`json
$quota_json
\`\`\`

### 共享服务器资源

\`\`\`text
$resource_summary
\`\`\`

## GitHub 状态

### 打开的 dev PR

\`\`\`json
$open_prs
\`\`\`

### 最近 dev CI

\`\`\`json
$latest_dev_ci
\`\`\`

### 最近 Release

\`\`\`text
$latest_release
\`\`\`

## 恢复步骤

1. 读取 \`AGENTS.md\` 和 \`.agents/skills/termuxpro-development/SKILL.md\`。
2. 运行 \`./scripts/codex-quota-guard.sh\`，只有总剩余额度低于 15% 才暂停主动迭代。
3. 运行 \`./scripts/resource-guard.sh\`，共享服务器上保持单个重任务，Gradle 使用 \`--max-workers=2\`。
4. 检查 \`git status --short --branch\`、打开 PR、最近 CI 和 Release 状态。
5. 从 \`docs/PRODUCT_BACKLOG.md\` 中最高优先级的 TermuxPro 增值服务切片继续。

## 安全红线

- 禁止执行默认 \`tmux kill-server\`；隔离测试必须移除继承的 \`TMUX\` 并使用 \`tmux -L\` 或 \`tmux -S\`。
- 禁止提交 \`.signing/\`、私钥、密码、AI Token、公司源码和未脱敏终端输出。
- 不修改系统级 JDK、SDK、PATH、服务、软件源或其他用户文件。
- 普通分支、提交、PR、CI 和小改动默认不发送飞书通知。
EOF
)"

if [[ "$mode" == "stdout" ]]; then
    printf '%s\n' "$checkpoint"
else
    output_dir="$(dirname "$output_file")"
    mkdir -p "$output_dir"
    temp_file="$(mktemp "$output_dir/.context-checkpoint.XXXXXX")"
    printf '%s\n' "$checkpoint" > "$temp_file"
    mv "$temp_file" "$output_file"
    printf '已更新 %s\n' "${output_file#$project_dir/}"
fi
