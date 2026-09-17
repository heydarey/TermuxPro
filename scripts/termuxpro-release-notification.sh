#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat >&2 <<'EOF'
用法：
  termuxpro-release-notification.sh --version <版本> --kind <stable|candidate>
    --url <Release URL> --sha256 <APK SHA-256>
    --features <功能说明> --fixes <修复说明>
    --known-limits <已知限制> --gates <验收状态> --action <用户动作>
    [--send]

默认只输出结构化通知内容；只有传入 --send 时才调用 termuxpro-notify。
EOF
}

version=''
kind=''
release_url=''
sha256=''
features=''
fixes=''
known_limits=''
gates=''
action=''
send=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        --version) version="${2:-}"; shift 2 ;;
        --kind) kind="${2:-}"; shift 2 ;;
        --url) release_url="${2:-}"; shift 2 ;;
        --sha256) sha256="${2:-}"; shift 2 ;;
        --features) features="${2:-}"; shift 2 ;;
        --features-file) features="$(<"${2:-}")"; shift 2 ;;
        --fixes) fixes="${2:-}"; shift 2 ;;
        --fixes-file) fixes="$(<"${2:-}")"; shift 2 ;;
        --known-limits) known_limits="${2:-}"; shift 2 ;;
        --known-limits-file) known_limits="$(<"${2:-}")"; shift 2 ;;
        --gates) gates="${2:-}"; shift 2 ;;
        --gates-file) gates="$(<"${2:-}")"; shift 2 ;;
        --action) action="${2:-}"; shift 2 ;;
        --send) send=1; shift ;;
        -h|--help) usage; exit 0 ;;
        *)
            echo "未知参数：$1" >&2
            usage
            exit 2
            ;;
    esac
done

required_fields=(
    "version:$version"
    "kind:$kind"
    "release_url:$release_url"
    "sha256:$sha256"
    "features:$features"
    "fixes:$fixes"
    "known_limits:$known_limits"
    "gates:$gates"
    "action:$action"
)

for field in "${required_fields[@]}"; do
    name="${field%%:*}"
    value="${field#*:}"
    if [[ -z "$value" ]]; then
        echo "缺少必填字段：$name" >&2
        usage
        exit 2
    fi
done

case "$kind" in
    stable) kind_label='正式版' ;;
    candidate) kind_label='候选体验版' ;;
    *)
        echo "--kind 只能是 stable 或 candidate。" >&2
        exit 2
        ;;
esac

message="$(
    cat <<EOF
TermuxPro ${version} ${kind_label}已发布

版本：${version}
类型：${kind_label}
Release：${release_url}
APK SHA-256：${sha256}

新增能力：
${features}

修复问题：
${fixes}

已知限制：
${known_limits}

验收状态：
${gates}

需要你做什么：
${action}
EOF
)"

printf '%s\n' "$message"

if [[ "$send" == "1" ]]; then
    if ! command -v termuxpro-notify >/dev/null 2>&1; then
        echo "未找到 termuxpro-notify，已取消发送；上方为可复制的结构化通知内容。" >&2
        exit 127
    fi
    termuxpro-notify \
        --level release \
        --title "TermuxPro ${version} ${kind_label}已发布" \
        --message "$message" \
        --key "termuxpro-release-${version}-${kind}"
fi
