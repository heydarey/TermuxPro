#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$project_dir/.tooling/android-sdk}}"

if [[ ! -x "$sdk_root/cmdline-tools/latest/bin/sdkmanager" ]]; then
    exec "$project_dir/scripts/ensure-android-sdk.sh"
fi
if ! command -v java >/dev/null || ! java -version 2>&1 | head -n 1 | grep -q '"17\.' ||
   ! command -v javac >/dev/null || ! javac -version 2>&1 | grep -q '^javac 17\.'; then
    # shellcheck source=/dev/null
    source "$project_dir/scripts/resolve-jdk17.sh"
fi

"$sdk_root/cmdline-tools/latest/bin/sdkmanager" \
    "platforms;android-36" \
    "build-tools;35.0.0" \
    "platform-tools" \
    "ndk;29.0.14206865"

escaped_sdk="${sdk_root//\\/\\\\}"
escaped_sdk="${escaped_sdk//:/\\:}"
printf 'sdk.dir=%s\n' "$escaped_sdk" > "$project_dir/local.properties"
echo "Android 开发环境已准备完成。"
