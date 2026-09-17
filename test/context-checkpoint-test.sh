#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
script="$project_dir/scripts/update-context-checkpoint.sh"

if [[ ! -x "$script" ]]; then
    echo "上下文检查点脚本必须存在且可执行。" >&2
    exit 1
fi

if grep -Eq '(^|[^A-Z_])HOME([^A-Z_]|$)|~/' "$script"; then
    echo "上下文检查点脚本不得依赖用户主目录或全局配置。" >&2
    exit 1
fi

for required in \
    "scripts/codex-quota-guard.sh" \
    "scripts/resource-guard.sh" \
    "scripts/github-cli.sh" \
    "scripts/release-window-guard.sh" \
    "docs/CURRENT_STATE.md" \
    "tmux kill-server" \
    "origin/dev"; do
    if ! grep -Fq "$required" "$script"; then
        echo "上下文检查点脚本缺少关键恢复信息：$required" >&2
        exit 1
    fi
done

checkpoint="$("$script" --stdout)"
for required in \
    "TermuxPro 当前迭代检查点" \
    "当前目标" \
    "代码状态" \
    "额度与资源" \
    "GitHub 状态" \
    "稳定版发布窗口" \
    "恢复步骤" \
    "安全红线" \
    "总剩余额度低于 15%" \
    "若稳定版已满 5 天，优先做发布评审" \
    "若已满" \
    "7 天，先发布正式版或落盘有证据的 HOLD" \
    "少发版不是不发版" \
    "一周至少一次正式稳定版发布"; do
    if ! grep -Fq "$required" <<<"$checkpoint"; then
        echo "上下文检查点输出缺少关键段落：$required" >&2
        exit 1
    fi
done

echo "上下文检查点脚本测试通过。"
