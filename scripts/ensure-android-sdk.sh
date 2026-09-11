#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$project_dir/.tooling/android-sdk}}"
downloads_dir="$project_dir/.tooling/downloads"
cmdline_tools_version="${ANDROID_CMDLINE_TOOLS_VERSION:-15859902}"
cmdline_tools_url="${ANDROID_CMDLINE_TOOLS_URL:-https://dl.google.com/android/repository/commandlinetools-linux-${cmdline_tools_version}_latest.zip}"
cmdline_zip="$downloads_dir/commandlinetools-linux-${cmdline_tools_version}_latest.zip"
install_optional_emulator=0

usage() {
    cat <<'USAGE'
用法：scripts/ensure-android-sdk.sh [--with-emulator]

用途：
  在项目级 .tooling/android-sdk 中准备 TermuxPro 本地构建需要的 Android SDK。
  默认只安装低资源构建/单测所需组件，不安装 emulator 或系统镜像。

环境变量：
  ANDROID_SDK_ROOT / ANDROID_HOME        覆盖 SDK 目录。
  ANDROID_CMDLINE_TOOLS_VERSION          覆盖 command-line tools 版本号。
  ANDROID_CMDLINE_TOOLS_URL              覆盖 command-line tools 下载地址。
  TERMUXPRO_ANDROID_SDK_ACCEPT_LICENSES  设为 1 时自动接受 Android SDK 许可证。
USAGE
}

while (($#)); do
    case "$1" in
        --with-emulator)
            install_optional_emulator=1
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

mkdir -p "$downloads_dir" "$sdk_root/cmdline-tools"

if [[ ! -x "$sdk_root/cmdline-tools/latest/bin/sdkmanager" ]]; then
    echo "准备 Android command-line tools：$cmdline_tools_url"
    if [[ ! -f "$cmdline_zip" ]]; then
        partial_zip="$cmdline_zip.part"
        if ! curl --fail --location --retry 3 --retry-delay 2 \
            --connect-timeout 20 --max-time 180 \
            --output "$partial_zip" "$cmdline_tools_url"; then
            rm -f -- "$partial_zip"
            echo "Android command-line tools 下载失败或超时，请稍后重试，或通过 ANDROID_CMDLINE_TOOLS_URL 指定可访问镜像。" >&2
            exit 4
        fi
        mv "$partial_zip" "$cmdline_zip"
    fi
    tmp_dir="$(mktemp -d "$project_dir/.tooling/cmdline-tools.XXXXXX")"
    cleanup() {
        if [[ "$tmp_dir" == "$project_dir/.tooling/cmdline-tools."* && -d "$tmp_dir" ]]; then
            find "$tmp_dir" -mindepth 1 -delete
            rmdir "$tmp_dir" 2>/dev/null || true
        fi
    }
    trap cleanup EXIT
    unzip -q "$cmdline_zip" -d "$tmp_dir"
    if [[ "$sdk_root/cmdline-tools/latest" == "$project_dir/.tooling/android-sdk/cmdline-tools/latest" &&
          -d "$sdk_root/cmdline-tools/latest" ]]; then
        find "$sdk_root/cmdline-tools/latest" -mindepth 1 -delete
        rmdir "$sdk_root/cmdline-tools/latest" 2>/dev/null || true
    fi
    mv "$tmp_dir/cmdline-tools" "$sdk_root/cmdline-tools/latest"
fi

# shellcheck source=/dev/null
source "$project_dir/scripts/resolve-jdk17.sh"

sdkmanager="$sdk_root/cmdline-tools/latest/bin/sdkmanager"
packages=(
    "platforms;android-36"
    "build-tools;35.0.0"
    "platform-tools"
    "ndk;29.0.14206865"
)
if (( install_optional_emulator == 1 )); then
    packages+=("emulator" "system-images;android-35;google_apis;x86_64")
fi

if [[ "${TERMUXPRO_ANDROID_SDK_ACCEPT_LICENSES:-0}" == "1" ]]; then
    yes | "$sdkmanager" --licenses >/dev/null || true
fi
"$sdkmanager" --sdk_root="$sdk_root" "${packages[@]}"

escaped_sdk="${sdk_root//\\/\\\\}"
escaped_sdk="${escaped_sdk//:/\\:}"
printf 'sdk.dir=%s\n' "$escaped_sdk" > "$project_dir/local.properties"

echo "Android SDK 已准备完成：$sdk_root"
