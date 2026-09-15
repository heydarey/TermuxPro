#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

python3 - <<'PY'
from pathlib import Path
import re
import sys

paths = {
    "默认英文资源": Path("app/src/main/res/values/strings.xml"),
    "简体中文资源": Path("app/src/main/res/values-zh-rCN/strings.xml"),
}

resources = {}
ok = True
for label, path in paths.items():
    text = path.read_text(encoding="utf-8")
    names = re.findall(r'<(?:string|string-array|plurals)\s+name="([^"]+)"', text)
    duplicates = sorted({name for name in names if names.count(name) > 1})
    if duplicates:
        ok = False
        print(f"{label}存在重复资源 key：{', '.join(duplicates)}", file=sys.stderr)
    resources[label] = set(names)

default_keys = resources["默认英文资源"]
zh_keys = resources["简体中文资源"]

missing_in_default = sorted(zh_keys - default_keys)
missing_in_zh = sorted(default_keys - zh_keys)

if missing_in_default:
    ok = False
    print("默认英文资源缺少简体中文中存在的 key：", file=sys.stderr)
    for name in missing_in_default:
        print(f"- {name}", file=sys.stderr)

if missing_in_zh:
    ok = False
    print("简体中文资源缺少默认英文中存在的 key：", file=sys.stderr)
    for name in missing_in_zh:
        print(f"- {name}", file=sys.stderr)

if not ok:
    sys.exit(1)

print("Android 字符串资源重复与中英文 key 对齐校验通过。")
PY
