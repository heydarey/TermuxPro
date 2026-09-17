#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
script="$project_dir/scripts/termuxpro-release-notification.sh"

output="$("$script" \
    --version 0.10.1 \
    --kind stable \
    --url https://github.com/heydarey/TermuxPro/releases/tag/v0.10.1 \
    --sha256 0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef \
    --features $'- Git 工作台支持安全快进拉取\\n- AI 会话中心支持删除最近启动记录' \
    --fixes $'- 修复深色弹窗文字不可读\\n- 修复终端滚动误触命令历史' \
    --known-limits '模拟器可覆盖 Android 通用行为，厂商 ROM 差异仍需后续样本补充。' \
    --gates 'CI、模拟器 UI、Release 签名 APK 校验均通过。' \
    --action '可下载正式包体验；如遇 SSH/AI CLI 场景问题，保留脱敏截图或步骤。')"

required_patterns=(
    'TermuxPro 0.10.1 正式版已发布'
    '版本：0.10.1'
    '类型：正式版'
    'Release：https://github.com/heydarey/TermuxPro/releases/tag/v0.10.1'
    'APK SHA-256：0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef'
    '新增能力：'
    '修复问题：'
    '已知限制：'
    '验收状态：'
    '需要你做什么：'
)

for pattern in "${required_patterns[@]}"; do
    if ! grep -Fq "$pattern" <<<"$output"; then
        echo "发布通知缺少结构化字段：$pattern" >&2
        exit 1
    fi
done

if "$script" --version 0.10.1 --kind stable >/tmp/termuxpro-notification-missing.log 2>&1; then
    echo "缺少必填字段时脚本必须失败。" >&2
    exit 1
fi
grep -Fq '缺少必填字段' /tmp/termuxpro-notification-missing.log

if "$script" \
    --version 0.10.1 \
    --kind preview \
    --url https://github.com/heydarey/TermuxPro/releases/tag/v0.10.1 \
    --sha256 0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef \
    --features '-' \
    --fixes '-' \
    --known-limits '-' \
    --gates '-' \
    --action '-' >/tmp/termuxpro-notification-kind.log 2>&1; then
    echo "非法发布类型必须失败。" >&2
    exit 1
fi
grep -Fq -- '--kind 只能是 stable 或 candidate' /tmp/termuxpro-notification-kind.log

echo "发布通知格式校验通过。"
