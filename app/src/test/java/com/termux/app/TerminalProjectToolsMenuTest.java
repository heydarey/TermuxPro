package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public class TerminalProjectToolsMenuTest {
    @Test
    public void toolboxUsesDeveloperTaskGroupsBeforeRawActions() {
        PopupMenu popup = new PopupMenu(RuntimeEnvironment.getApplication(),
            new View(RuntimeEnvironment.getApplication()));
        Menu menu = popup.getMenu();

        TerminalProjectToolsMenu.populate(RuntimeEnvironment.getApplication(), menu);

        assertEquals("当前上下文", menu.getItem(0).getTitle().toString());
        assertFalse(menu.getItem(0).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_PROMPT_COMPOSER, menu.getItem(1).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_TMUX_SESSIONS, menu.getItem(2).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_CUSTOM_COMMANDS, menu.getItem(3).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_SEARCH_OUTPUT, menu.getItem(4).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_TOUCH_SCROLL_MODE, menu.getItem(5).getItemId());
        assertEquals("滑动：终端历史（推荐）→ 切到 AI/TUI", menu.getItem(5).getTitle().toString());

        assertEquals("项目与 Git", menu.getItem(6).getTitle().toString());
        assertFalse(menu.getItem(6).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_GIT_STATUS, menu.getItem(7).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_GIT_DIFF, menu.getItem(8).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_REMOTE_FILES, menu.getItem(9).getItemId());
        assertEquals("远端文件", menu.getItem(9).getTitle().toString());
        assertEquals(TerminalProjectToolsMenu.TOOL_PROJECT_CHECK, menu.getItem(10).getItemId());
        assertEquals("项目任务", menu.getItem(10).getTitle().toString());
        assertEquals(TerminalProjectToolsMenu.TOOL_START_WEB_PREVIEW,
            menu.getItem(11).getItemId());
        assertEquals("启动 Web 隧道", menu.getItem(11).getTitle().toString());
        assertEquals(TerminalProjectToolsMenu.TOOL_OPEN_WEB_PREVIEW,
            menu.getItem(12).getItemId());
        assertEquals("打开 Web 预览", menu.getItem(12).getTitle().toString());

        assertEquals("连接与安全", menu.getItem(13).getTitle().toString());
        assertFalse(menu.getItem(13).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_CONNECTION_DIAGNOSTIC,
            menu.getItem(14).getItemId());
        assertEquals("环境诊断", menu.getItem(14).getTitle().toString());
        assertEquals(TerminalProjectToolsMenu.TOOL_SSH_KEYS, menu.getItem(15).getItemId());
        assertEquals("SSH 密钥", menu.getItem(15).getTitle().toString());

        assertEquals("AI 工作", menu.getItem(16).getTitle().toString());
        assertFalse(menu.getItem(16).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_CENTER, menu.getItem(17).getItemId());
        assertEquals("AI CLI 会话中心", menu.getItem(17).getTitle().toString());
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_CLAUDE, menu.getItem(18).getItemId());
        assertEquals("启动 Claude Code", menu.getItem(18).getTitle().toString());
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_CODEX, menu.getItem(19).getItemId());
        assertEquals("启动 Codex CLI", menu.getItem(19).getTitle().toString());
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_CONFIRM, menu.getItem(20).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_REJECT, menu.getItem(21).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_INTERRUPT, menu.getItem(22).getItemId());

        assertEquals("键区切换", menu.getItem(23).getTitle().toString());
        assertFalse(menu.getItem(23).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_KEYS_SHELL, menu.getItem(24).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_KEYS_AI, menu.getItem(25).getItemId());
        assertEquals(TerminalProjectToolsMenu.TOOL_KEYS_VIM, menu.getItem(26).getItemId());
    }

    @Test
    public void toolboxShowsCurrentTouchScrollModeAction() {
        PopupMenu popup = new PopupMenu(RuntimeEnvironment.getApplication(),
            new View(RuntimeEnvironment.getApplication()));

        TerminalProjectToolsMenu.populate(RuntimeEnvironment.getApplication(), popup.getMenu(), true);

        assertEquals(TerminalProjectToolsMenu.TOOL_TOUCH_SCROLL_MODE,
            popup.getMenu().getItem(5).getItemId());
        assertEquals("滑动：AI/TUI 面板 → 切到历史",
            popup.getMenu().getItem(5).getTitle().toString());
    }

    @Test
    public void toolsButtonShowsCurrentTouchScrollMode() {
        assertEquals(com.termux.R.string.workspace_tools_scrollback_action,
            TerminalProjectToolsMenu.toolsButtonLabel(false));
        assertEquals(com.termux.R.string.workspace_tools_tui_scroll_action,
            TerminalProjectToolsMenu.toolsButtonLabel(true));
    }
}
