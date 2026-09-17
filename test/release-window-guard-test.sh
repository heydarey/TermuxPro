#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
guard="$project_dir/scripts/release-window-guard.sh"

fixture='[
  {
    "tagName": "v0.10.1",
    "isPrerelease": false,
    "publishedAt": "2026-09-17T08:17:31Z",
    "name": "TermuxPro v0.10.1"
  },
  {
    "tagName": "v0.10.2-rc.1",
    "isPrerelease": true,
    "publishedAt": "2026-09-23T08:17:31Z",
    "name": "TermuxPro v0.10.2-rc.1"
  }
]'

run_guard() {
    local now="$1"
    TERMUXPRO_RELEASE_LIST_JSON="$fixture" TERMUXPRO_RELEASE_NOW="$now" "$guard" 2>&1
}

ok_output="$(run_guard "2026-09-20T08:17:31Z")"
if [[ "$ok_output" != *"status=OK"* || "$ok_output" != *"latestTag=v0.10.1"* ]]; then
    echo "未满 5 天时应允许继续日常迭代，并忽略候选版。" >&2
    echo "$ok_output" >&2
    exit 1
fi

review_output="$(run_guard "2026-09-22T08:17:31Z")"
if [[ "$review_output" != *"status=REVIEW_SOON"* || "$review_output" != *"action=prioritize_release_review"* ]]; then
    echo "满 5 天时应提示优先发布评审。" >&2
    echo "$review_output" >&2
    exit 1
fi

set +e
overdue_output="$(run_guard "2026-09-24T08:17:32Z")"
overdue_status=$?
set -e
if [[ "$overdue_status" -ne 20 || "$overdue_output" != *"status=OVERDUE"* ]]; then
    echo "满 7 天时应阻断普通迭代，要求发布或落盘 HOLD。" >&2
    echo "exit=$overdue_status" >&2
    echo "$overdue_output" >&2
    exit 1
fi

echo "稳定版发布窗口守卫测试通过。"
