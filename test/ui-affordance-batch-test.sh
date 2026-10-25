#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
zh="$project_dir/app/src/main/res/values-zh-rCN/strings.xml"
en="$project_dir/app/src/main/res/values/strings.xml"
menu="$project_dir/app/src/main/java/com/termux/app/TerminalProjectToolsMenu.java"
termux="$project_dir/app/src/main/java/com/termux/app/TermuxActivity.java"
workspace="$project_dir/app/src/main/java/com/termux/app/WorkspaceActivity.java"
ssh_keys="$project_dir/app/src/main/java/com/termux/app/SshKeysActivity.java"
git_diff="$project_dir/app/src/main/java/com/termux/app/GitDiffActivity.java"

require_contains() {
    local file="$1" pattern="$2" message="$3"
    if ! grep -Fq "$pattern" "$file"; then
        echo "$message" >&2
        exit 1
    fi
}

forbid_contains() {
    local file="$1" pattern="$2" message="$3"
    if grep -Fq "$pattern" "$file"; then
        echo "$message" >&2
        exit 1
    fi
}

# 大字体：终端顶部工具按钮只保留模式词，SSH 地址提示使用极简变体。
require_contains "$zh" 'name="workspace_tools_scrollback_action_compact">历史<' \
    "大字体终端工具按钮必须提供“历史”极简变体。"
require_contains "$zh" 'name="workspace_tools_tui_scroll_action_compact">TUI<' \
    "大字体终端工具按钮必须提供“TUI”极简变体。"
require_contains "$en" 'name="workspace_tools_scrollback_action_compact">History<' \
    "英文大字体终端工具按钮必须提供 History 极简变体。"
require_contains "$zh" 'name="workspace_host_hint_minimal">' \
    "大字体 SSH 地址输入提示必须存在极简变体，避免 200% 字体截断。"
require_contains "$menu" "toolsButtonLabel(boolean tuiTouchScrollMode, boolean largeFont)" \
    "终端工具按钮标签必须支持大字体重载。"
require_contains "$termux" "toolsButtonLabel(tuiMode, largeFont)" \
    "终端页必须按 fontScale 传入大字体参数。"
require_contains "$workspace" "workspace_host_hint_minimal" \
    "工作区大字体布局必须使用极简 SSH 地址提示。"

# SSH 公钥：不可用时按钮必须有肉眼可辨的禁用视觉。
require_contains "$ssh_keys" "mCopyButton.setAlpha(available ? 1f : 0.4f)" \
    "复制公钥按钮禁用时必须降低透明度，仅 setEnabled 在深色主题下不可辨。"
require_contains "$ssh_keys" "mInstallButton.setAlpha(installable ? 1f : 0.4f)" \
    "安装公钥按钮禁用时必须降低透明度。"

# Git 工作台：状态摘要不得复述推荐动作，主按钮与建议区是唯一下一步入口。
forbid_contains "$zh" "推荐动作：%3\$s" \
    "Git 状态摘要不得再复述推荐动作，同屏三次重复 CTA 已被治理。"
forbid_contains "$en" "Recommended action: %3\$s" \
    "英文 Git 状态摘要不得再复述 Recommended action。"
forbid_contains "$git_diff" "recommendedAction" \
    "GitDiffActivity 不得保留 recommendedAction 复述路径。"
forbid_contains "$zh" 'name="git_workbench_recommend_' \
    "中文资源不得残留已无引用的 git_workbench_recommend_* 字符串。"
forbid_contains "$en" 'name="git_workbench_recommend_' \
    "英文资源不得残留已无引用的 git_workbench_recommend_* 字符串。"

# 无效工作区错误态：必须说明缺失项与恢复路径，刷新按钮不可用。
for key in remote_files_invalid_workspace project_tasks_invalid_workspace connection_diagnostic_invalid_workspace git_diff_invalid_workspace task_sessions_invalid_workspace; do
    if ! grep -Fq "name=\"$key\"" "$zh"; then
        echo "缺少资源 $key。" >&2; exit 1
    fi
done
require_contains "$zh" "请点“返回工作区”补全后再打开本页" \
    "无效工作区错误必须给出“返回工作区补全”的恢复路径，不能只报“连接信息无效”。"
require_contains "$en" "SSH host, port, or project path is missing" \
    "英文无效工作区错误必须列明缺失项。"
forbid_contains "$zh" ">工作区连接信息无效。<" \
    "无效工作区错误不能停留在无原因无动作的死胡同文案。"
require_contains "$project_dir/app/src/main/java/com/termux/app/RemoteFilesActivity.java" "refresh.setAlpha(0.4f)" \
    "远端文件页无效工作区态必须禁用刷新按钮并降低透明度。"
require_contains "$project_dir/app/src/main/java/com/termux/app/RemoteFilePreviewActivity.java" "refresh.setAlpha(0.4f)" \
    "远端文件预览页无效工作区态必须禁用刷新按钮。"
require_contains "$project_dir/app/src/main/java/com/termux/app/ConnectionDiagnosticActivity.java" "refresh.setAlpha(0.4f)" \
    "连接诊断页无效工作区态必须禁用刷新按钮。"
require_contains "$project_dir/app/src/main/java/com/termux/app/ProjectTasksActivity.java" "mRefresh.setAlpha(0.4f)" \
    "项目任务页无效工作区态必须同步降低刷新按钮透明度。"

# 终端会话抽屉：空态引导与关闭按钮危险色。
layout_drawer="$project_dir/app/src/main/res/layout/activity_termux.xml"
termux_activity="$project_dir/app/src/main/java/com/termux/app/TermuxActivity.java"
require_contains "$zh" "terminal_sessions_empty" \
    "终端会话空态必须有一行可行动的引导文案。"
require_contains "$en" "terminal_sessions_empty" \
    "英文终端会话空态引导必须存在。"
require_contains "$layout_drawer" '@+id/terminal_sessions_empty' \
    "终端会话列表必须挂空态视图。"
require_contains "$termux_activity" "setEmptyView(findViewById(R.id.terminal_sessions_empty))" \
    "终端会话空态必须完成 setEmptyView 接线。"
require_contains "$termux_activity" "R.color.tp_danger" \
    "关闭会话按钮必须使用 tp_danger 危险色与“新建”区分，不得回退为等权重中性色。"

echo "UI 审计快赢静态校验通过。"
