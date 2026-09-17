#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

branch_file="${TERMUXPRO_AUDIT_BRANCHES_FILE:-}"
open_pr_file="${TERMUXPRO_AUDIT_OPEN_PRS_FILE:-}"
release_file="${TERMUXPRO_AUDIT_RELEASES_FILE:-}"

load_remote_branches() {
    if [[ -n "$branch_file" ]]; then
        sed 's/^ *//' "$branch_file"
    else
        git branch -r | sed 's/^ *//'
    fi
}

load_open_pr_heads() {
    if [[ -n "$open_pr_file" ]]; then
        sed -n 's/.*"headRefName"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$open_pr_file"
    else
        ./scripts/github-cli.sh pr list --state open --json headRefName --limit 100 \
            --jq '.[].headRefName'
    fi
}

load_releases() {
    if [[ -n "$release_file" ]]; then
        cat "$release_file"
    else
        ./scripts/github-cli.sh release list --limit 100
    fi
}

is_protected_branch() {
    case "$1" in
        master|dev|dev_dailyIteration) return 0 ;;
        *) return 1 ;;
    esac
}

is_candidate_release_branch() {
    [[ "$1" =~ ^dev_release[0-9]+Rc[1-9][0-9]*_[0-9]{8}$ ]]
}

is_stale_merged_branch() {
    local branch="$1"
    if [[ -n "$branch_file" ]]; then
        case "$branch" in
            *Merged*) return 0 ;;
            *) return 1 ;;
        esac
    fi
    git merge-base --is-ancestor "refs/remotes/origin/$branch" "refs/remotes/origin/dev" \
        >/dev/null 2>&1
}

declare -A open_pr_heads=()
while IFS= read -r pr_head; do
    [[ -n "$pr_head" ]] && open_pr_heads["$pr_head"]=1
done < <(load_open_pr_heads)

remote_branch_count=0
protected_count=0
open_pr_branch_count=0
candidate_release_branch_count=0
stale_merged_count=0
unknown_or_active_count=0
dev_hotfix_count=0
cleanup_candidates=()

while IFS= read -r ref; do
    [[ -z "$ref" || "$ref" == "origin/HEAD"* ]] && continue
    [[ "$ref" != origin/* ]] && continue
    branch="${ref#origin/}"
    ((remote_branch_count += 1))

    if [[ "$branch" == dev_* || "$branch" == hotfix_* ]]; then
        ((dev_hotfix_count += 1))
    fi

    if is_protected_branch "$branch"; then
        ((protected_count += 1))
        continue
    fi
    if [[ -n "${open_pr_heads[$branch]:-}" ]]; then
        ((open_pr_branch_count += 1))
        continue
    fi
    if is_candidate_release_branch "$branch"; then
        ((candidate_release_branch_count += 1))
        continue
    fi
    if [[ "$branch" == dev_* || "$branch" == hotfix_* ]]; then
        if is_stale_merged_branch "$branch"; then
            ((stale_merged_count += 1))
            cleanup_candidates+=("$branch")
        else
            ((unknown_or_active_count += 1))
        fi
    fi
done < <(load_remote_branches)

release_total=0
release_prerelease=0
release_stable=0
while IFS= read -r release_line; do
    [[ -z "$release_line" ]] && continue
    ((release_total += 1))
    if grep -Fq 'Pre-release' <<<"$release_line"; then
        ((release_prerelease += 1))
    else
        ((release_stable += 1))
    fi
done < <(load_releases)

cat <<EOF
# TermuxPro GitHub 噪声审计

- 远端分支总数：${remote_branch_count}
- dev/hotfix 命名分支数：${dev_hotfix_count}
- 保护分支数：${protected_count}
- 打开 PR 分支数：${open_pr_branch_count}
- 候选发布分支数：${candidate_release_branch_count}
- 已合并陈旧分支候选数：${stale_merged_count}
- 未确认或仍活跃 dev/hotfix 分支数：${unknown_or_active_count}
- Release 总数：${release_total}
- 稳定 Release 数：${release_stable}
- Pre-release 数：${release_prerelease}

## 治理结论
EOF

noise_reasons=()
if (( remote_branch_count > 50 )); then
    noise_reasons+=("远端分支超过 50 个")
fi
if (( stale_merged_count > 20 )); then
    noise_reasons+=("已合并陈旧分支候选超过 20 个")
fi
if (( candidate_release_branch_count > 3 )); then
    noise_reasons+=("候选发布分支超过 3 个")
fi
if (( release_prerelease > release_stable )); then
    noise_reasons+=("Pre-release 数量超过稳定 Release")
fi
if (( release_total > 30 )); then
    noise_reasons+=("Release 总数超过 30 个")
fi

if [[ "${#noise_reasons[@]}" -eq 0 ]]; then
    cat <<'EOF'

status=OK action=keep_current_cadence

- 当前 GitHub 噪声在可维护范围内；继续复用 dev_dailyIteration，避免无必要分支和无价值预发布。
EOF
else
    joined_reasons="$(IFS='；'; echo "${noise_reasons[*]}")"
    cat <<EOF

status=NOISE_HIGH action=plan_cleanup_review reason=${joined_reasons}

- 下一步应先生成清理复核清单，不直接删除远端分支、标签或 Release。
- 优先处理已合并陈旧 dev/hotfix 分支；每个候选删除前再次验证已合入 origin/dev、无打开 PR、无候选/稳定发布风险。
- 候选发布分支单独复核对应标签、Release 和发布报告后再处理；不得和普通 dev/hotfix 分支混删。
- 历史 Release 和附件默认保留，不删除用户可下载产物；后续通过发布窗口守卫减少无价值预发布。
EOF
fi

cat <<'EOF'

## 规则

- 本脚本只读审计，不删除远端分支、标签或 Release。
- 永远排除 master、dev、dev_dailyIteration 和打开 PR 的 head 分支。
- “已合并陈旧分支候选”只代表可进入人工/自治清理复核，不代表可直接删除。
- 删除远端分支前必须再次确认分支已合入 origin/dev，且无打开 PR、无候选/稳定发布风险。

## 已合并陈旧分支候选
EOF

if [[ "${#cleanup_candidates[@]}" -eq 0 ]]; then
    echo
    echo "当前没有可列出的候选。"
else
    printf '\n'
    printf -- '- %s\n' "${cleanup_candidates[@]}"
fi
