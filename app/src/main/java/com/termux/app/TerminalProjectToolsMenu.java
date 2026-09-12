package com.termux.app;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;

import com.termux.R;

/** 终端工具箱菜单的信息架构。 */
final class TerminalProjectToolsMenu {
    static final int TOOL_GIT_STATUS = 1;
    static final int TOOL_GIT_DIFF = 2;
    static final int TOOL_PROJECT_CHECK = 3;
    static final int TOOL_TMUX_SESSIONS = 4;
    static final int TOOL_INTERRUPT = 5;
    static final int TOOL_KEYS_SHELL = 6;
    static final int TOOL_KEYS_AI = 7;
    static final int TOOL_KEYS_VIM = 8;
    static final int TOOL_AI_CONFIRM = 9;
    static final int TOOL_AI_REJECT = 10;
    static final int TOOL_SEARCH_OUTPUT = 11;
    static final int TOOL_CUSTOM_COMMANDS = 12;
    static final int TOOL_TOUCH_SCROLL_MODE = 13;
    static final int TOOL_REMOTE_FILES = 14;
    static final int TOOL_CONNECTION_DIAGNOSTIC = 15;
    static final int TOOL_SSH_KEYS = 16;
    static final int TOOL_START_WEB_PREVIEW = 17;
    static final int TOOL_OPEN_WEB_PREVIEW = 18;
    static final int TOOL_AI_CLAUDE = 19;
    static final int TOOL_AI_CODEX = 20;
    static final int TOOL_AI_CENTER = 21;
    static final int TOOL_PROMPT_COMPOSER = 22;

    private TerminalProjectToolsMenu() {}

    static void populate(@NonNull Context context, @NonNull Menu menu) {
        populate(context, menu, false);
    }

    static void populate(@NonNull Context context, @NonNull Menu menu, boolean tuiTouchScrollMode) {
        menu.clear();
        addHeader(context, menu, R.string.terminal_tools_section_recommended);
        addTool(context, menu, TOOL_AI_CENTER, R.string.terminal_ai_cli_center_action,
            R.string.terminal_ai_cli_center_description);
        addTool(context, menu, TOOL_GIT_STATUS, R.string.workspace_git_status_action,
            R.string.workspace_git_status_description);
        addTool(context, menu, TOOL_TMUX_SESSIONS, R.string.workspace_tmux_sessions_action,
            R.string.workspace_tmux_sessions_description);
        addTool(context, menu, TOOL_CUSTOM_COMMANDS, R.string.terminal_custom_commands_action,
            R.string.terminal_custom_commands_description);

        addHeader(context, menu, R.string.terminal_tools_section_context);
        addTool(context, menu, TOOL_PROMPT_COMPOSER, R.string.workspace_prompt_action,
            R.string.workspace_prompt_description);
        addTool(context, menu, TOOL_SEARCH_OUTPUT, R.string.terminal_search_action,
            R.string.terminal_search_description);
        addTool(context, menu, TOOL_TOUCH_SCROLL_MODE, tuiTouchScrollMode ?
                R.string.terminal_touch_scroll_switch_to_scrollback :
                R.string.terminal_touch_scroll_switch_to_tui,
            tuiTouchScrollMode ? R.string.terminal_touch_scroll_scrollback_description :
                R.string.terminal_touch_scroll_tui_description);

        addHeader(context, menu, R.string.terminal_tools_section_project);
        addTool(context, menu, TOOL_GIT_DIFF, R.string.workspace_git_diff_action,
            R.string.workspace_git_diff_description);
        addTool(context, menu, TOOL_REMOTE_FILES, R.string.workspace_remote_files_action,
            R.string.workspace_remote_files_description);
        addTool(context, menu, TOOL_PROJECT_CHECK, R.string.workspace_project_tasks_action,
            R.string.workspace_project_tasks_description);
        addTool(context, menu, TOOL_START_WEB_PREVIEW, R.string.workspace_start_preview_action,
            R.string.workspace_start_preview_description);
        addTool(context, menu, TOOL_OPEN_WEB_PREVIEW, R.string.workspace_open_preview_action,
            R.string.workspace_open_preview_description);

        addHeader(context, menu, R.string.terminal_tools_section_connection);
        addTool(context, menu, TOOL_CONNECTION_DIAGNOSTIC, R.string.workspace_diagnostic_action,
            R.string.workspace_diagnostic_description);
        addTool(context, menu, TOOL_SSH_KEYS, R.string.workspace_ssh_keys_action,
            R.string.workspace_ssh_keys_description);

        addHeader(context, menu, R.string.terminal_tools_section_ai);
        addTool(context, menu, TOOL_AI_CLAUDE, R.string.terminal_ai_launch_claude_action,
            R.string.terminal_ai_launch_claude_description);
        addTool(context, menu, TOOL_AI_CODEX, R.string.terminal_ai_launch_codex_action,
            R.string.terminal_ai_launch_codex_description);
        addTool(context, menu, TOOL_AI_CONFIRM, R.string.ai_action_confirm_selection,
            R.string.ai_action_confirm_description);
        addTool(context, menu, TOOL_AI_REJECT, R.string.ai_action_reject_or_back,
            R.string.ai_action_reject_description);
        addTool(context, menu, TOOL_INTERRUPT, R.string.workspace_interrupt_action,
            R.string.workspace_interrupt_description);

        addHeader(context, menu, R.string.terminal_tools_section_keyboard);
        addTool(context, menu, TOOL_KEYS_SHELL, R.string.workspace_keys_shell_action,
            R.string.workspace_keys_shell_description);
        addTool(context, menu, TOOL_KEYS_AI, R.string.workspace_keys_ai_action,
            R.string.workspace_keys_ai_description);
        addTool(context, menu, TOOL_KEYS_VIM, R.string.workspace_keys_vim_action,
            R.string.workspace_keys_vim_description);
    }

    private static void addHeader(@NonNull Context context, @NonNull Menu menu, int title) {
        MenuItem header = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, context.getString(title));
        header.setEnabled(false);
    }

    private static void addTool(@NonNull Context context, @NonNull Menu menu, int id, int title,
                                int description) {
        MenuItem item = menu.add(Menu.NONE, id, Menu.NONE, title);
        MenuItemCompat.setContentDescription(item, context.getString(description));
    }

    static int toolsButtonLabel(boolean tuiTouchScrollMode) {
        return tuiTouchScrollMode ? R.string.workspace_tools_tui_scroll_action :
            R.string.workspace_tools_scrollback_action;
    }

    /** 顶部工具箱按钮保持短文本，完整语义交给无障碍说明，避免小屏拥挤。 */
    static int toolsButtonDescription(boolean tuiTouchScrollMode) {
        return tuiTouchScrollMode ? R.string.workspace_tools_tui_scroll_description :
            R.string.workspace_tools_scrollback_description;
    }
}
