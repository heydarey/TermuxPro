#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

usage() {
    cat <<'EOF'
用法：scripts/release-window-guard.sh

用途：
  检查 TermuxPro 稳定版发布窗口，避免“少发版”演变成“长期不发版”。

规则：
  - 最新稳定版未满 5 天：通过，继续日常迭代。
  - 最新稳定版满 5 天但未满 7 天：通过但提示应优先做发布评审。
  - 最新稳定版满 7 天：失败，必须先推进正式版，或在发布评审中落盘明确 HOLD。

测试环境变量：
  TERMUXPRO_RELEASE_LIST_JSON  使用指定 GitHub Release JSON，跳过网络查询。
  TERMUXPRO_RELEASE_NOW        指定当前 UTC 时间，例如 2026-09-24T08:18:00Z。
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
fi

release_json="${TERMUXPRO_RELEASE_LIST_JSON:-}"
if [[ -z "$release_json" ]]; then
    release_json="$("$project_dir/scripts/github-cli.sh" release list \
        --limit 50 \
        --json tagName,isPrerelease,publishedAt,name)"
fi

now_value="${TERMUXPRO_RELEASE_NOW:-}"

TERMUXPRO_RELEASE_LIST_JSON_PAYLOAD="$release_json" python3 - "$now_value" <<'PY'
import datetime as dt
import json
import os
import re
import sys

now_arg = sys.argv[1].strip()
raw = os.environ.get("TERMUXPRO_RELEASE_LIST_JSON_PAYLOAD", "").strip()

def parse_time(value: str) -> dt.datetime:
    normalized = value.replace("Z", "+00:00")
    parsed = dt.datetime.fromisoformat(normalized)
    if parsed.tzinfo is None:
        parsed = parsed.replace(tzinfo=dt.timezone.utc)
    return parsed.astimezone(dt.timezone.utc)

try:
    releases = json.loads(raw)
except json.JSONDecodeError as exc:
    print(f"status=ERROR reason=release_json_invalid detail={exc}", file=sys.stderr)
    sys.exit(64)

now = parse_time(now_arg) if now_arg else dt.datetime.now(dt.timezone.utc)
stable_pattern = re.compile(r"^v\d+\.\d+\.\d+$")
stable = []
for item in releases:
    tag = str(item.get("tagName") or "")
    if item.get("isPrerelease") is True:
        continue
    if not stable_pattern.match(tag):
        continue
    published_at = item.get("publishedAt")
    if not published_at:
        continue
    stable.append((parse_time(str(published_at)), tag))

if not stable:
    print("status=BLOCKED reason=no_stable_release latestTag=none ageDays=unknown nextReview=now")
    sys.exit(21)

published_at, tag = max(stable, key=lambda pair: pair[0])
age = now - published_at
age_seconds = max(0, int(age.total_seconds()))
age_days = age_seconds // 86400
age_hours = age_seconds // 3600
review_at = published_at + dt.timedelta(days=5)
deadline = published_at + dt.timedelta(days=7)

common = (
    f"latestTag={tag} "
    f"publishedAt={published_at.isoformat().replace('+00:00', 'Z')} "
    f"ageDays={age_days} "
    f"ageHours={age_hours} "
    f"reviewAfter={review_at.isoformat().replace('+00:00', 'Z')} "
    f"deadline={deadline.isoformat().replace('+00:00', 'Z')}"
)

if age >= dt.timedelta(days=7):
    print(f"status=OVERDUE action=release_or_record_hold {common}")
    sys.exit(20)

if age >= dt.timedelta(days=5):
    print(f"status=REVIEW_SOON action=prioritize_release_review {common}")
    sys.exit(0)

print(f"status=OK action=continue_daily_iteration {common}")
PY
