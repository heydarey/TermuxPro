package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import androidx.core.view.MenuItemCompat;

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

        assertEquals("推荐操作", menu.getItem(0).getTitle().toString());
        assertFalse(menu.getItem(0).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_CENTER, menu.getItem(1).getItemId());
        assertEquals("AI CLI 会话中心", menu.getItem(1).getTitle().toString());
        assertDescription(menu, 1, "打开当前工作区的 Claude/Codex 会话中心，选择新建或历史入口。");
        assertEquals(TerminalProjectToolsMenu.TOOL_GIT_STATUS, menu.getItem(2).getItemId());
        assertEquals("Git 工作台", menu.getItem(2).getTitle().toString());
        assertDescription(menu, 2, "打开当前工作区的 Git 工作台，查看分支、改动、提交记录和同步状态。");
        assertEquals(TerminalProjectToolsMenu.TOOL_TMUX_SESSIONS, menu.getItem(3).getItemId());
        assertDescription(menu, 3, "查看并显式进入 tmux 会话；只有当前工作区的 TermuxPro 会话可重命名或停止。");
        assertEquals(TerminalProjectToolsMenu.TOOL_CUSTOM_COMMANDS, menu.getItem(4).getItemId());
        assertDescription(menu, 4, "打开当前工作区快捷指令，不会自动执行命令。");

        assertEquals("当前终端", menu.getItem(5).getTitle().toString());
        assertFalse(menu.getItem(5).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_PROMPT_COMPOSER, menu.getItem(6).getItemId());
        assertDescription(menu, 6, "打开提示词编辑器，编写或粘贴多行内容，确认后只发送到当前终端。");
        assertEquals(TerminalProjectToolsMenu.TOOL_SEARCH_OUTPUT, menu.getItem(7).getItemId());
        assertDescription(menu, 7, "在当前可用的终端历史中搜索文本，不读取远端文件或 AI 私有历史。");
        assertEquals(TerminalProjectToolsMenu.TOOL_TOUCH_SCROLL_MODE, menu.getItem(8).getItemId());
        assertEquals("改为控制 AI/TUI 面板", menu.getItem(8).getTitle().toString());
        assertDescription(menu, 8, "切到 AI/TUI 控制模式，手指上下滑动会优先控制支持鼠标事件的面板。");

        assertEquals("项目与 Git", menu.getItem(9).getTitle().toString());
        assertFalse(menu.getItem(9).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_GIT_DIFF, menu.getItem(10).getItemId());
        assertDescription(menu, 10, "打开当前工作区的 Git 改动审查，不直接提交、拉取或推送。");
        assertEquals(TerminalProjectToolsMenu.TOOL_REMOTE_FILES, menu.getItem(11).getItemId());
        assertEquals("远端文件", menu.getItem(11).getTitle().toString());
        assertDescription(menu, 11, "浏览当前工作区远端目录，只读查看文件，受大小限制保护。");
        assertEquals(TerminalProjectToolsMenu.TOOL_PROJECT_CHECK, menu.getItem(12).getItemId());
        assertEquals("项目任务 / 测试", menu.getItem(12).getTitle().toString());
        assertDescription(menu, 12, "打开当前工作区的项目任务和测试页；先展示远端目标与命令，由你确认后执行，不会直接输入当前终端。");
        assertEquals(TerminalProjectToolsMenu.TOOL_START_WEB_PREVIEW,
            menu.getItem(13).getItemId());
        assertEquals("启动 Web 隧道", menu.getItem(13).getTitle().toString());
        assertDescription(menu, 13, "为当前工作区启动本机可访问的 SSH Web 预览隧道。");
        assertEquals(TerminalProjectToolsMenu.TOOL_OPEN_WEB_PREVIEW,
            menu.getItem(14).getItemId());
        assertEquals("打开 Web 预览", menu.getItem(14).getTitle().toString());
        assertDescription(menu, 14, "用手机浏览器打开已配置的本机 Web 预览地址。");

        assertEquals("连接与安全", menu.getItem(15).getTitle().toString());
        assertFalse(menu.getItem(15).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_CONNECTION_DIAGNOSTIC,
            menu.getItem(16).getItemId());
        assertEquals("环境诊断", menu.getItem(16).getTitle().toString());
        assertDescription(menu, 16, "检查当前工作区 SSH、tmux、Git 和 AI CLI 环境，只读诊断不自动修复。");
        assertEquals(TerminalProjectToolsMenu.TOOL_SSH_KEYS, menu.getItem(17).getItemId());
        assertEquals("SSH 密钥", menu.getItem(17).getTitle().toString());
        assertDescription(menu, 17, "打开 SSH 密钥辅助页，查看配置指引，不保存密码或私钥。");

        assertEquals("AI 工作", menu.getItem(18).getTitle().toString());
        assertFalse(menu.getItem(18).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_CLAUDE, menu.getItem(19).getItemId());
        assertEquals("启动 Claude Code", menu.getItem(19).getTitle().toString());
        assertDescription(menu, 19, "在当前终端启动 Claude Code；启动前仍会确认新建或历史选择。");
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_CODEX, menu.getItem(20).getItemId());
        assertEquals("启动 Codex CLI", menu.getItem(20).getTitle().toString());
        assertDescription(menu, 20, "在当前终端启动 Codex CLI；启动前仍会确认新建或历史选择。");
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_CONFIRM, menu.getItem(21).getItemId());
        assertDescription(menu, 21, "只向当前 AI CLI 发送 Enter，请先确认高亮选项；应用不会自动授权。");
        assertEquals(TerminalProjectToolsMenu.TOOL_AI_REJECT, menu.getItem(22).getItemId());
        assertDescription(menu, 22, "向当前 AI CLI 发送 Esc，用于返回、拒绝或退出当前选择。");
        assertEquals(TerminalProjectToolsMenu.TOOL_INTERRUPT, menu.getItem(23).getItemId());
        assertDescription(menu, 23, "向当前终端发送 Ctrl+C，用于中断正在运行的命令或 AI CLI 操作。");

        assertEquals("键区切换", menu.getItem(24).getTitle().toString());
        assertFalse(menu.getItem(24).isEnabled());
        assertEquals(TerminalProjectToolsMenu.TOOL_KEYS_SHELL, menu.getItem(25).getItemId());
        assertDescription(menu, 25, "切换到常规 Shell 常用快捷键，不发送远端命令。");
        assertEquals(TerminalProjectToolsMenu.TOOL_KEYS_AI, menu.getItem(26).getItemId());
        assertDescription(menu, 26, "切换到 Claude/Codex 常用快捷键，不自动确认任何 AI 操作。");
        assertEquals(TerminalProjectToolsMenu.TOOL_KEYS_VIM, menu.getItem(27).getItemId());
        assertDescription(menu, 27, "切换到 Vim 常用快捷键，不改变当前终端内容。");
    }

    @Test
    public void toolboxShowsCurrentTouchScrollModeAction() {
        PopupMenu popup = new PopupMenu(RuntimeEnvironment.getApplication(),
            new View(RuntimeEnvironment.getApplication()));

        TerminalProjectToolsMenu.populate(RuntimeEnvironment.getApplication(), popup.getMenu(), true);

        assertEquals(TerminalProjectToolsMenu.TOOL_TOUCH_SCROLL_MODE,
            popup.getMenu().getItem(8).getItemId());
        assertEquals("改为查看终端历史（推荐）",
            popup.getMenu().getItem(8).getTitle().toString());
        assertDescription(popup.getMenu(), 8, "切回普通历史模式，手指上下滑动用于查看上方终端输出。");
    }

    @Test
    public void toolsButtonShowsCurrentTouchScrollMode() {
        assertEquals(com.termux.R.string.workspace_tools_scrollback_action,
            TerminalProjectToolsMenu.toolsButtonLabel(false));
        assertEquals(com.termux.R.string.workspace_tools_tui_scroll_action,
            TerminalProjectToolsMenu.toolsButtonLabel(true));
        assertEquals(com.termux.R.string.workspace_tools_scrollback_description,
            TerminalProjectToolsMenu.toolsButtonDescription(false));
        assertEquals(com.termux.R.string.workspace_tools_tui_scroll_description,
            TerminalProjectToolsMenu.toolsButtonDescription(true));
    }

    private static void assertDescription(Menu menu, int index, String expected) {
        assertEquals(expected, String.valueOf(
            MenuItemCompat.getContentDescription(menu.getItem(index))));
    }
}
