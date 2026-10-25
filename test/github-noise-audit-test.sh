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
    'status=OK action=keep_current_cadence'
    '继续复用 dev_dailyIteration，避免无必要分支和无价值预发布。'
    '本脚本只读审计，不删除远端分支、标签或 Release。'
    '永远排除 master、dev、dev_dailyIteration 和打开 PR 的 head 分支。'
    '删除远端分支前必须再次确认分支已合入 origin/dev，且无打开 PR、无候选/稳定发布风险。'
    '默认最多展示 50 个候选，避免审计报告本身制造噪声；设置 TERMUXPRO_AUDIT_CANDIDATE_LIMIT=0 可显示全部。'
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
if grep -Fq -- '- dev_release101Rc1_20260909' <<<"$output"; then
    echo "候选发布分支不得进入陈旧清理候选。" >&2
    exit 1
fi

noisy_branches="$temp_dir/noisy-branches.txt"
noisy_prs="$temp_dir/noisy-open-prs.json"
noisy_releases="$temp_dir/noisy-releases.txt"

{
    echo 'origin/master'
    echo 'origin/dev'
    echo 'origin/dev_dailyIteration'
    for index in $(seq 1 25); do
        printf 'origin/dev_oldFeature%02dMerged_20260901\n' "$index"
    done
    for index in $(seq 1 4); do
        printf 'origin/dev_release101Rc%d_20260909\n' "$index"
    done
} >"$noisy_branches"

cat >"$noisy_prs" <<'EOF'
[]
EOF

{
    for index in $(seq 1 12); do
        printf 'TermuxPro v0.%d.0\tLatest\tv0.%d.0\t2026-09-09T05:00:08Z\n' "$index" "$index"
    done
    for index in $(seq 1 19); do
        printf 'TermuxPro v0.10.%d-rc.1\tPre-release\tv0.10.%d-rc.1\t2026-09-09T04:16:32Z\n' "$index" "$index"
    done
} >"$noisy_releases"

noisy_output="$(
    TERMUXPRO_AUDIT_BRANCHES_FILE="$noisy_branches" \
    TERMUXPRO_AUDIT_OPEN_PRS_FILE="$noisy_prs" \
    TERMUXPRO_AUDIT_RELEASES_FILE="$noisy_releases" \
    "$project_dir/scripts/audit-github-noise.sh"
)"

noisy_patterns=(
    'status=NOISE_HIGH action=plan_cleanup_review'
    '已合并陈旧分支候选超过 20 个'
    '候选发布分支超过 3 个'
    'Pre-release 数量超过稳定 Release'
    'Release 总数超过 30 个'
    '下一步应先生成清理复核清单，不直接删除远端分支、标签或 Release。'
    '历史 Release 和附件默认保留，不删除用户可下载产物；后续通过发布窗口守卫减少无价值预发布。'
)

for pattern in "${noisy_patterns[@]}"; do
    if ! grep -Fq -- "$pattern" <<<"$noisy_output"; then
        echo "GitHub 噪声审计高噪声输出缺少：$pattern" >&2
        echo "$noisy_output" >&2
        exit 1
    fi
done

limited_output="$(
    TERMUXPRO_AUDIT_BRANCHES_FILE="$noisy_branches" \
    TERMUXPRO_AUDIT_OPEN_PRS_FILE="$noisy_prs" \
    TERMUXPRO_AUDIT_RELEASES_FILE="$noisy_releases" \
    TERMUXPRO_AUDIT_CANDIDATE_LIMIT=3 \
    "$project_dir/scripts/audit-github-noise.sh"
)"
limited_patterns=(
    '- dev_oldFeature01Merged_20260901'
    '- dev_oldFeature02Merged_20260901'
    '- dev_oldFeature03Merged_20260901'
    '……另有 22 个候选未显示；如需完整清单，设置 TERMUXPRO_AUDIT_CANDIDATE_LIMIT=0 后重跑。'
)
for pattern in "${limited_patterns[@]}"; do
    if ! grep -Fq -- "$pattern" <<<"$limited_output"; then
        echo "GitHub 噪声审计限量输出缺少：$pattern" >&2
        echo "$limited_output" >&2
        exit 1
    fi
done
if grep -Fq -- '- dev_oldFeature04Merged_20260901' <<<"$limited_output"; then
    echo "限量输出不应展示超过上限的候选。" >&2
    exit 1
fi

full_output="$(
    TERMUXPRO_AUDIT_BRANCHES_FILE="$noisy_branches" \
    TERMUXPRO_AUDIT_OPEN_PRS_FILE="$noisy_prs" \
    TERMUXPRO_AUDIT_RELEASES_FILE="$noisy_releases" \
    TERMUXPRO_AUDIT_CANDIDATE_LIMIT=0 \
    "$project_dir/scripts/audit-github-noise.sh"
)"
if ! grep -Fq -- '- dev_oldFeature25Merged_20260901' <<<"$full_output"; then
    echo "候选上限为 0 时应展示完整清单。" >&2
    exit 1
fi
if grep -Fq -- '候选未显示' <<<"$full_output"; then
    echo "候选上限为 0 时不应提示仍有未显示候选。" >&2
    exit 1
fi

echo "GitHub 噪声审计脚本测试通过。"
