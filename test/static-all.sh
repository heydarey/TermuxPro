#!/usr/bin/env bash
set -euo pipefail

# 静态门禁唯一清单来源：scripts/test-all.sh、scripts/pre-push-smoke.sh 与 CI 共用。
# 新增静态门禁只改本文件，不得再在三处分别维护清单（2026-10-25 收敛决策，CI 审计 F3）。
project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

./scripts/validate-skills.sh
./test/version-metadata-test.sh
./test/select-stable-baseline-tag-test.sh
./test/github-cli-wrapper-test.sh
./test/github-check-suites-test.sh
./test/pre-push-smoke-test.sh
./test/workflow-trigger-policy-test.sh
./test/release-notification-format-test.sh
./test/github-noise-audit-test.sh
./test/ai-launch-decision-copy-test.sh
./test/terminal-touch-scrollback-test.sh
./test/terminal-session-actions-test.sh
./test/dialog-readable-style-test.sh
./test/skill-validator-bootstrap-test.sh
./test/ui-screenshot-manifest-test.sh
./test/release-apk-device-smoke-test.sh
./test/release-abi-gate-test.sh
./test/codex-quota-guard-test.sh
./test/android-string-resource-parity-test.sh
./test/release-window-guard-test.sh
./test/context-checkpoint-test.sh
./test/goal-lifecycle-policy-test.sh
./test/generated-cache-cleanup-test.sh
./test/android-sdk-bootstrap-test.sh
./test/ui-affordance-batch-test.sh
