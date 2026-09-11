#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
user_cache_root="${TERMUXPRO_USER_CACHE_ROOT:-${HOME:?}}"
apply=0
include_user_caches=0

usage() {
    cat <<'USAGE'
用法：scripts/cleanup-generated-caches.sh [--apply] [--include-user-caches]

默认只做 dry-run，列出可安全清理的可再生成目录。
--apply                 执行删除。
--include-user-caches   额外包含当前用户 npm/Gradle 缓存；只在磁盘守卫阻断迭代时使用。
USAGE
}

while (($#)); do
    case "$1" in
        --apply)
            apply=1
            ;;
        --include-user-caches)
            include_user_caches=1
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            usage >&2
            exit 2
            ;;
    esac
    shift
done

targets=(
    "$project_dir/app/build"
    "$project_dir/terminal-view/build"
    "$project_dir/terminal-emulator/build"
    "$project_dir/termux-shared/build"
    "$project_dir/build"
    "$project_dir/.gradle"
    "$project_dir/.tooling/downloads"
    "$project_dir/.tooling/android-sdk"
    "$project_dir/.tooling/jdk-deb"
)

if (( include_user_caches == 1 )); then
    targets+=(
        "$user_cache_root/.npm/_cacache"
        "$user_cache_root/.gradle/caches/8.9"
        "$user_cache_root/.gradle/caches/modules-2"
        "$user_cache_root/.gradle/.tmp"
    )
fi

is_allowed_target() {
    local target="$1"
    case "$target" in
        "$project_dir"/app/build|\
        "$project_dir"/terminal-view/build|\
        "$project_dir"/terminal-emulator/build|\
        "$project_dir"/termux-shared/build|\
        "$project_dir"/build|\
        "$project_dir"/.gradle|\
        "$project_dir"/.tooling/downloads|\
        "$project_dir"/.tooling/android-sdk|\
        "$project_dir"/.tooling/jdk-deb|\
        "$user_cache_root"/.npm/_cacache|\
        "$user_cache_root"/.gradle/caches/8.9|\
        "$user_cache_root"/.gradle/caches/modules-2|\
        "$user_cache_root"/.gradle/.tmp)
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

delete_tree_contents() {
    local target="$1"
    if [[ ! -d "$target" ]]; then
        return 0
    fi
    find "$target" -mindepth 1 -delete
    rmdir "$target" 2>/dev/null || true
}

df -h "$project_dir"
for target in "${targets[@]}"; do
    if ! is_allowed_target "$target"; then
        echo "拒绝非白名单清理目标：$target" >&2
        exit 64
    fi
    if [[ ! -e "$target" ]]; then
        continue
    fi
    size="$(du -shx "$target" 2>/dev/null | awk '{print $1}')"
    if (( apply == 1 )); then
        echo "清理：$target ($size)"
        delete_tree_contents "$target"
    else
        echo "DRY-RUN 可清理：$target ($size)"
    fi
done
df -h "$project_dir"
