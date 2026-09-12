#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

require_text() {
    local file="$1"
    local text="$2"
    if ! grep -Fq "$text" "$file"; then
        echo "$file 缺少长期 Goal 生命周期规则：$text" >&2
        exit 1
    fi
}

require_text "$project_dir/AGENTS.md" "TermuxPro 日常维护是长期持久 Goal"
require_text "$project_dir/AGENTS.md" "不得把长期 Goal 标记为 \`complete\`"
require_text "$project_dir/.agents/skills/termuxpro-development/SKILL.md" "不得因单个功能切片、修复切片、发布评审或回归切片完成就主动"
require_text "$project_dir/.agents/skills/termuxpro-development/SKILL.md" "只有用户明确终止项目托管或要求结束长期维护时"
require_text "$project_dir/.claude/skills/termuxpro-development/SKILL.md" "单个切片完成后不得把长期 Goal 标记为 \`complete\`"
require_text "$project_dir/.claude/skills/termuxpro-development/SKILL.md" "暂停等待"

if grep -RIn "update_goal.*complete\\|长期 Goal.*complete\\|Goal.*标记为.*complete\\|Goal.*置为.*complete" \
    "$project_dir/AGENTS.md" \
    "$project_dir/.agents/skills/termuxpro-development/SKILL.md" \
    "$project_dir/.claude/skills/termuxpro-development/SKILL.md" \
    "$project_dir/docs" \
    | grep -v "只有用户明确终止" \
    | grep -v "不得把长期 Goal 标记为" \
    | grep -v "不得因单个功能切片" \
    | grep -v "不允许结束长期 Goal" \
    | grep -v "单个切片完成后不得把长期 Goal 标记为" \
    | grep -v "长期 Goal 不得因单轮完成置为 complete" \
    >/dev/null; then
    echo "发现可能诱导把长期 Goal 置为 complete 的规则，请改为暂停或继续下一轮。" >&2
    exit 1
fi

echo "长期 Goal 生命周期规则校验通过。"
