#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
script="$project_dir/scripts/ensure-android-sdk.sh"
bootstrap="$project_dir/scripts/bootstrap-dev-env.sh"
development_doc="$project_dir/docs/DEVELOPMENT.md"

if [[ ! -x "$script" ]]; then
    echo "Android SDK 引导脚本必须存在且可执行。" >&2
    exit 1
fi

if grep -Eq 'rm[[:space:]]+-[A-Za-z]*r' "$script"; then
    echo "Android SDK 引导脚本禁止使用 rm -r/rf，避免误清理项目外目录。" >&2
    exit 1
fi

for required in \
    ".tooling/android-sdk" \
    "commandlinetools-linux-" \
    "ANDROID_CMDLINE_TOOLS_URL" \
    "TERMUXPRO_ANDROID_SDK_ACCEPT_LICENSES" \
    "--connect-timeout 20" \
    "--max-time 180" \
    ".part" \
    "platforms;android-36" \
    "build-tools;35.0.0" \
    "platform-tools" \
    "ndk;29.0.14206865" \
    "--with-emulator" \
    "resolve-jdk17.sh" \
    "local.properties"; do
    if ! grep -Fq -- "$required" "$script"; then
        echo "Android SDK 引导脚本缺少关键能力：$required" >&2
        exit 1
    fi
done

if ! grep -Fq "ensure-android-sdk.sh" "$bootstrap"; then
    echo "bootstrap-dev-env.sh 应在缺少 sdkmanager 时委托 ensure-android-sdk.sh。" >&2
    exit 1
fi

if ! grep -Fq "cleanup-generated-caches.sh" "$development_doc" ||
   ! grep -Fq "ensure-android-sdk.sh" "$development_doc"; then
    echo "开发文档应说明清理后如何恢复项目级 Android SDK。" >&2
    exit 1
fi

echo "Android SDK 引导脚本测试通过。"
