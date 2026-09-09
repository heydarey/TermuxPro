#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
temp_dir="$(mktemp -d)"
trap 'rm -rf "$temp_dir"' EXIT

branches="$temp_dir/branches.txt"
prs="$temp_dir/open-prs.json"
releases="$temp_dir/releases.txt"

cat >"$branches" <<'EOF'
origin/HEAD -> origin/master
origin/master
origin/dev
origin/dev_dailyIteration
origin/dev_oldFeatureMerged_20260901
origin/dev_activeFeature_20260909
origin/hotfix_crashMerged_20260901
origin/dev_release101Rc1_20260909
EOF

cat >"$prs" <<'EOF'
[
  {"headRefName": "dev_activeFeature_20260909"}
]
EOF

cat >"$releases" <<'EOF'
TermuxPro v0.10.0	Latest	v0.10.0	2026-09-09T05:00:08Z
TermuxPro v0.10.0-rc.1	Pre-release	v0.10.0-rc.1	2026-09-09T04:16:32Z
EOF

output="$(
    TERMUXPRO_AUDIT_BRANCHES_FILE="$branches" \
    TERMUXPRO_AUDIT_OPEN_PRS_FILE="$prs" \
    TERMUXPRO_AUDIT_RELEASES_FILE="$releases" \
    "$project_dir/scripts/audit-github-noise.sh"
)"

required_patterns=(
    '远端分支总数：7'
    'dev/hotfix 命名分支数：5'
    '保护分支数：3'
    '打开 PR 分支数：1'
    '候选发布分支数：1'
    '已合并陈旧分支候选数：2'
    'Release 总数：2'
    '稳定 Release 数：1'
    'Pre-release 数：1'
    '本脚本只读审计，不删除远端分支、标签或 Release。'
    '永远排除 master、dev、dev_dailyIteration 和打开 PR 的 head 分支。'
    '- dev_oldFeatureMerged_20260901'
    '- hotfix_crashMerged_20260901'
)

for pattern in "${required_patterns[@]}"; do
    if ! grep -Fq -- "$pattern" <<<"$output"; then
        echo "GitHub 噪声审计输出缺少：$pattern" >&2
        exit 1
    fi
done

if grep -Fq -- '- dev_activeFeature_20260909' <<<"$output"; then
    echo "打开 PR 分支不得进入陈旧清理候选。" >&2
    exit 1
fi
if grep -Fq -- '- dev_dailyIteration' <<<"$output"; then
    echo "长期研发分支不得进入陈旧清理候选。" >&2
    exit 1
fi

echo "GitHub 噪声审计脚本测试通过。"
