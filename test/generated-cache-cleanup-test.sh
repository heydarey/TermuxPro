#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
script="$project_dir/scripts/cleanup-generated-caches.sh"
resource_guard="$project_dir/scripts/resource-guard.sh"

if [[ ! -x "$script" ]]; then
    echo "生成缓存清理脚本必须存在且可执行。" >&2
    exit 1
fi

if grep -Eq 'rm[[:space:]]+-[A-Za-z]*r' "$script"; then
    echo "生成缓存清理脚本禁止使用 rm -r/rf，必须使用白名单和 find -delete。" >&2
    exit 1
fi

for required in \
    "DRY-RUN" \
    "--apply" \
    "--include-user-caches" \
    "is_allowed_target" \
    "find \"\$target\" -mindepth 1 -delete" \
    "TERMUXPRO_USER_CACHE_ROOT" \
    ".tooling/android-sdk"; do
    if ! grep -Fq -- "$required" "$script"; then
        echo "生成缓存清理脚本缺少关键安全能力：$required" >&2
        exit 1
    fi
done

dry_run_output="$("$script")"
if [[ "$(wc -l <<<"$dry_run_output" | tr -d ' ')" -lt 2 ]]; then
    echo "生成缓存清理脚本 dry-run 应输出磁盘状态。" >&2
    exit 1
fi

if grep -Fq "$HOME/.npm/_cacache" <<<"$dry_run_output"; then
    echo "默认 dry-run 不应扫描用户级缓存，避免误导全局清理。" >&2
    exit 1
fi

if ! grep -Fq "cleanup-generated-caches.sh" "$resource_guard"; then
    echo "资源守卫磁盘失败时应提示使用安全清理脚本。" >&2
    exit 1
fi

echo "生成缓存清理脚本测试通过。"
