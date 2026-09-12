package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;

import com.termux.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public class AiCliSessionCenterActivityTest {

    @Before
    public void setUp() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit().clear().commit();
        RuntimeEnvironment.getApplication().getSharedPreferences(
            AiLaunchHistoryStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void showsSafeEmptyStateWithoutReadingPrivateAiHistory() {
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        assertEquals("AI CLI 会话中心", text(activity, R.id.ai_cli_center_title));
        assertEquals("未选择有效远程工作区", text(activity, R.id.ai_cli_center_target));
        assertTrue(text(activity, R.id.ai_cli_center_target_detail).contains("请先回到工作台"));
        assertEquals("当前上下文", text(activity, R.id.ai_cli_center_context_title));
        assertEquals("开始 AI 工作", text(activity, R.id.ai_cli_center_start_title));
        assertTrue(text(activity, R.id.ai_cli_center_start_hint).contains("不会自动进入会话或 tmux"));
        assertTrue(text(activity, R.id.ai_cli_center_ai_risk).contains("不会猜服务器"));
        assertEquals("启动前先确认", text(activity, R.id.ai_cli_center_prepare_title));
        assertTrue(text(activity, R.id.ai_cli_center_prepare_hint).contains("共享服务器"));
        assertEquals("AI 完成后", text(activity, R.id.ai_cli_center_next_title));
        assertTrue(text(activity, R.id.ai_cli_center_next_hint).contains("优先查看 Git 改动"));
        assertTrue(text(activity, R.id.ai_cli_center_next_hint).contains("运行项目任务"));
        assertEquals("最近 AI 启动", text(activity, R.id.ai_cli_center_history_title));
        assertTrue(text(activity, R.id.ai_cli_center_history_hint).contains("不读取 Claude/Codex 私有历史"));
        assertTrue(text(activity, R.id.ai_cli_center_history_scope).contains("未选择工作区"));
        assertTrue(text(activity, R.id.ai_cli_center_history_scope).contains("不会猜测服务器"));
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("请先选择有效工作区"));
        assertTrue(text(activity, R.id.ai_cli_center_history_next_step).contains("先回到“服务器与项目”"));
        assertTrue(text(activity, R.id.ai_cli_center_claude_commands).contains("claude --resume"));
        assertTrue(text(activity, R.id.ai_cli_center_codex_commands).contains("codex resume"));
        assertEquals("新建 Claude", text(activity, R.id.ai_cli_center_claude_new));
        assertEquals("选择 Claude 历史", text(activity, R.id.ai_cli_center_claude_history));
        assertEquals("新建 Codex", text(activity, R.id.ai_cli_center_codex_new));
        assertEquals("选择 Codex 历史", text(activity, R.id.ai_cli_center_codex_history));
        assertTrue(activity.findViewById(R.id.ai_cli_center_claude_new).getContentDescription()
            .toString().contains("不自动恢复历史"));
        assertTrue(activity.findViewById(R.id.ai_cli_center_claude_history).getContentDescription()
            .toString().contains("共享账号请确认会话归属"));
        assertTrue(activity.findViewById(R.id.ai_cli_center_codex_history).getContentDescription()
            .toString().contains("不自动恢复"));

        activity.findViewById(R.id.ai_cli_center_claude_new).performClick();
        assertNextActivity(activity, WorkspaceActivity.class);
    }

    @Test
    public void showsActiveWorkspaceAndRoutesToValueAddedTools() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\",\"connectionPolicy\":\"attach_session\",\"sessionName\":\"safe-ai\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        assertEquals("远程开发", text(activity, R.id.ai_cli_center_target));
        String detail = text(activity, R.id.ai_cli_center_target_detail);
        assertTrue(detail.contains("hdr@192.168.1.153:22 · ~/project"));
        assertTrue(!detail.contains("工作区连接策略"));
        String policy = text(activity, R.id.ai_cli_center_policy_summary);
        assertTrue(policy.contains("工作区连接策略：仅进入指定 tmux：safe-ai"));
        assertTrue(policy.contains("AI 快捷启动策略：始终只建立 SSH"));
        String risk = text(activity, R.id.ai_cli_center_ai_risk);
        assertTrue(risk.contains("工作区默认 tmux：safe-ai"));
        assertTrue(risk.contains("AI 启动不会自动进入它"));
        assertTrue(risk.contains("共享 Claude/tmux 会话"));
        assertTrue(text(activity, R.id.ai_cli_center_history_scope).contains("记录范围：远程开发"));
        assertTrue(text(activity, R.id.ai_cli_center_history_scope)
            .contains("hdr@192.168.1.153:22 · ~/project"));
        assertTrue(text(activity, R.id.ai_cli_center_history_scope).contains("不跨工作区"));

        activity.findViewById(R.id.ai_cli_center_open_workspace).performClick();
        assertNextActivity(activity, WorkspaceActivity.class);

        assertEquals("快捷指令与模板", text(activity, R.id.ai_cli_center_open_templates));
        activity.findViewById(R.id.ai_cli_center_open_templates).performClick();
        assertNextActivity(activity, CustomCommandsActivity.class);

        activity.findViewById(R.id.ai_cli_center_open_diagnostic).performClick();
        assertNextActivity(activity, ConnectionDiagnosticActivity.class);

        activity.findViewById(R.id.ai_cli_center_open_tmux).performClick();
        assertNextActivity(activity, TaskSessionsActivity.class);

        activity.findViewById(R.id.ai_cli_center_open_git).performClick();
        Intent gitIntent = shadowOf(activity).getNextStartedActivity();
        assertEquals(GitDiffActivity.class.getName(), gitIntent.getComponent().getClassName());
        assertTrue(!gitIntent.getBooleanExtra(GitDiffActivity.EXTRA_START_IN_DIFF, true));

        activity.findViewById(R.id.ai_cli_center_open_project_tasks).performClick();
        assertNextActivity(activity, ProjectTasksActivity.class);
    }

    @Test
    public void aiActionsOpenIndependentSshOnlyTerminalWithoutAutoTmux() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        activity.findViewById(R.id.ai_cli_center_claude_history).performClick();

        assertNull(shadowOf(activity).getNextStartedActivity());
        AlertDialog historyConfirm = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(historyConfirm);
        assertEquals("打开 Claude Code 历史选择？", shadowOf(historyConfirm).getTitle());
        String message = ((TextView) historyConfirm.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(message.contains("hdr@192.168.1.153:22 · ~/project"));
        assertTrue(message.contains("执行命令：claude --resume"));
        assertTrue(message.contains("只打开 CLI 原生选择器"));
        assertTrue(message.contains("不会自动选择历史"));
        assertTrue(message.contains("不会自动进入 tmux"));
        assertTrue(message.contains("共享账号"));
        assertEquals("打开历史选择",
            historyConfirm.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        historyConfirm.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(TermuxActivity.class.getName(), intent.getComponent().getClassName());
        Bundle extras = intent.getExtras();
        assertTrue(extras.getBoolean(TermuxActivity.EXTRA_NEW_SESSION));
        String startup = extras.getString(TermuxActivity.EXTRA_STARTUP_COMMAND);
        assertTrue(startup.contains("claude --resume"));
        assertTrue(startup.contains("ssh -t"));
        assertTrue(startup.contains("hdr@192.168.1.153"));
        assertTrue(startup.contains("exec claude --resume"));
        assertTrue(!startup.contains("tmux attach-session"));
        assertTrue(!startup.contains("tmux new-session"));
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("Claude Code"));
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("历史选择"));
    }

    @Test
    public void repeatLastHistoryLaunchRequiresTargetConfirmation() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        AiLaunchHistoryStore store = new AiLaunchHistoryStore(RuntimeEnvironment.getApplication());
        store.record(new WorkspaceTarget("workspace-a", "远程开发", "hdr@192.168.1.153", 22,
                "~/project"),
            AiCliLaunchCommand.Tool.CODEX, AiCliLaunchCommand.Mode.PICK_HISTORY);
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        activity.findViewById(R.id.ai_cli_center_repeat_last).performClick();

        assertNull(shadowOf(activity).getNextStartedActivity());
        AlertDialog historyConfirm = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(historyConfirm);
        assertEquals("打开 Codex CLI 历史选择？", shadowOf(historyConfirm).getTitle());
        String message = ((TextView) historyConfirm.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(message.contains("执行命令：codex resume"));
        assertTrue(message.contains("hdr@192.168.1.153:22 · ~/project"));
        historyConfirm.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        Intent repeated = shadowOf(activity).getNextStartedActivity();
        assertNotNull(repeated);
        assertEquals(TermuxActivity.class.getName(), repeated.getComponent().getClassName());
        assertTrue(repeated.getStringExtra(TermuxActivity.EXTRA_STARTUP_COMMAND)
            .contains("exec codex resume"));
    }

    @Test
    public void repeatsDeletesAndClearsOnlyCurrentWorkspaceLaunchHistory() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        AiLaunchHistoryStore store = new AiLaunchHistoryStore(RuntimeEnvironment.getApplication());
        store.record(new WorkspaceTarget("workspace-a", "远程开发", "hdr@192.168.1.153", 22,
                "~/project"),
            AiCliLaunchCommand.Tool.CLAUDE, AiCliLaunchCommand.Mode.NEW_SESSION);
        store.record(new WorkspaceTarget("workspace-a", "远程开发", "hdr@192.168.1.153", 22,
                "~/project"),
            AiCliLaunchCommand.Tool.CODEX, AiCliLaunchCommand.Mode.PICK_HISTORY);
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("Codex CLI"));
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("历史选择"));
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("启动时间："));
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("最近 2 条启动"));
        assertTrue(text(activity, R.id.ai_cli_center_history_next_step).contains("可重复上次 Codex CLI · 历史选择"));
        assertTrue(text(activity, R.id.ai_cli_center_history_next_step).contains("不会删除远端 AI 历史"));
        assertEquals("重复：Codex CLI · 历史选择",
            text(activity, R.id.ai_cli_center_repeat_last));
        assertEquals("删除最近：Codex CLI · 历史选择",
            text(activity, R.id.ai_cli_center_delete_latest));

        activity.findViewById(R.id.ai_cli_center_delete_latest).performClick();
        AlertDialog deleteConfirm = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(deleteConfirm);
        assertEquals("删除最近这条 AI 启动记录？", shadowOf(deleteConfirm).getTitle());
        String deleteMessage = ((TextView) deleteConfirm.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(deleteMessage.contains("Codex CLI · 历史选择"));
        assertTrue(deleteMessage.contains("启动时间："));
        assertTrue(deleteMessage.contains("hdr@192.168.1.153:22 · ~/project"));
        assertTrue(deleteMessage.contains("不会删除 Claude/Codex 远端历史"));
        assertEquals("删除本地记录",
            deleteConfirm.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        deleteConfirm.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("Codex CLI"));

        activity.findViewById(R.id.ai_cli_center_delete_latest).performClick();
        deleteConfirm = ShadowAlertDialog.getLatestAlertDialog();
        deleteConfirm.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        String afterDelete = text(activity, R.id.ai_cli_center_history_summary);
        assertTrue(afterDelete.contains("Claude Code"));
        assertTrue(!afterDelete.contains("Codex CLI"));
        assertTrue(text(activity, R.id.ai_cli_center_history_next_step).contains("可重复上次 Claude Code · 新建会话"));
        assertEquals("重复：Claude Code · 新建会话",
            text(activity, R.id.ai_cli_center_repeat_last));
        assertEquals("删除最近：Claude Code · 新建会话",
            text(activity, R.id.ai_cli_center_delete_latest));

        activity.findViewById(R.id.ai_cli_center_repeat_last).performClick();
        Intent repeated = shadowOf(activity).getNextStartedActivity();
        assertEquals(TermuxActivity.class.getName(), repeated.getComponent().getClassName());
        assertTrue(repeated.getStringExtra(TermuxActivity.EXTRA_STARTUP_COMMAND)
            .contains("exec claude"));

        activity.findViewById(R.id.ai_cli_center_clear_history).performClick();
        AlertDialog confirm = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(confirm);
        assertEquals("清空当前工作区的 AI 启动记录？", shadowOf(confirm).getTitle());
        String message = ((TextView) confirm.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(message.contains("远程开发"));
        assertTrue(message.contains("2 条本地启动记录"));
        assertTrue(message.contains("不会删除 Claude/Codex 远端历史"));
        assertEquals("清空本地记录",
            confirm.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        confirm.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("Claude Code"));

        activity.findViewById(R.id.ai_cli_center_clear_history).performClick();
        confirm = ShadowAlertDialog.getLatestAlertDialog();
        confirm.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("还没有 AI 启动记录"));
        assertTrue(text(activity, R.id.ai_cli_center_history_next_step).contains("如果要开始新任务"));
        assertEquals("重复上次", text(activity, R.id.ai_cli_center_repeat_last));

        activity.findViewById(R.id.ai_cli_center_manage_history).performClick();
        AlertDialog empty = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(empty);
        assertEquals("当前工作区 AI 启动记录", shadowOf(empty).getTitle());
    }

    @Test
    public void legacyLaunchHistoryWithoutTimestampShowsUnknownTime() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        RuntimeEnvironment.getApplication().getSharedPreferences(
            AiLaunchHistoryStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString("entries_v1",
                "[{\"workspaceId\":\"workspace-a\",\"workspaceName\":\"远程开发\","
                    + "\"host\":\"hdr@192.168.1.153\",\"port\":22,\"path\":\"~/project\","
                    + "\"tool\":\"CODEX\",\"mode\":\"PICK_HISTORY\"}]")
            .commit();

        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("启动时间：未记录"));
    }

    @Test
    public void managesEveryLocalAiLaunchRecordWithoutReadingPrivateHistory() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        AiLaunchHistoryStore store = new AiLaunchHistoryStore(RuntimeEnvironment.getApplication());
        WorkspaceTarget workspace = new WorkspaceTarget("workspace-a", "远程开发",
            "hdr@192.168.1.153", 22, "~/project");
        store.record(workspace, AiCliLaunchCommand.Tool.CLAUDE,
            AiCliLaunchCommand.Mode.NEW_SESSION);
        store.record(workspace, AiCliLaunchCommand.Tool.CODEX,
            AiCliLaunchCommand.Mode.NEW_SESSION);
        store.record(workspace, AiCliLaunchCommand.Tool.CLAUDE,
            AiCliLaunchCommand.Mode.PICK_HISTORY);
        store.record(workspace, AiCliLaunchCommand.Tool.CODEX,
            AiCliLaunchCommand.Mode.PICK_HISTORY);
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        assertEquals("查看全部记录", text(activity, R.id.ai_cli_center_manage_history));
        assertTrue(activity.findViewById(R.id.ai_cli_center_manage_history).getContentDescription()
            .toString().contains("重复或删除"));
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("还有 1 条已折叠"));

        activity.findViewById(R.id.ai_cli_center_manage_history).performClick();
        AlertDialog list = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(list);
        assertEquals("AI 启动记录：远程开发", shadowOf(list).getTitle());
        assertEquals(4, list.getListView().getAdapter().getCount());
        assertTrue(list.getListView().getAdapter().getItem(3).toString()
            .contains("Claude Code · 新建会话"));

        list.getListView().performItemClick(null, 3,
            list.getListView().getAdapter().getItemId(3));
        AlertDialog action = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(action);
        assertEquals("重复或删除启动记录", shadowOf(action).getTitle());
        String message = ((TextView) action.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(message.contains("Claude Code · 新建会话"));
        assertTrue(message.contains("启动时间："));
        assertTrue(message.contains("不会删除 Claude/Codex 远端历史"));
        assertEquals("重复启动",
            action.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        assertEquals("删除本地记录",
            action.getButton(AlertDialog.BUTTON_NEUTRAL).getText().toString());
        action.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        AlertDialog deleteConfirm = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(deleteConfirm);
        assertEquals("删除这条 AI 启动记录？", shadowOf(deleteConfirm).getTitle());
        String deleteMessage = ((TextView) deleteConfirm.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(deleteMessage.contains("Claude Code · 新建会话"));
        assertTrue(deleteMessage.contains("启动时间："));
        assertTrue(deleteMessage.contains("hdr@192.168.1.153:22 · ~/project"));
        assertTrue(deleteMessage.contains("不会删除 Claude/Codex 远端历史"));
        deleteConfirm.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        assertTrue(text(activity, R.id.ai_cli_center_history_summary).contains("还有 1 条已折叠"));

        activity.findViewById(R.id.ai_cli_center_manage_history).performClick();
        list = ShadowAlertDialog.getLatestAlertDialog();
        list.getListView().performItemClick(null, 3,
            list.getListView().getAdapter().getItemId(3));
        action = ShadowAlertDialog.getLatestAlertDialog();
        action.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        deleteConfirm = ShadowAlertDialog.getLatestAlertDialog();
        deleteConfirm.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        String afterDelete = text(activity, R.id.ai_cli_center_history_summary);
        assertTrue(afterDelete.contains("最近 3 条启动"));
        assertTrue(!afterDelete.contains("还有 1 条已折叠"));

        activity.findViewById(R.id.ai_cli_center_manage_history).performClick();
        list = ShadowAlertDialog.getLatestAlertDialog();
        assertEquals(3, list.getListView().getAdapter().getCount());
    }

    @Test
    public void repeatsSelectedAiLaunchRecordFromManageDialog() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        AiLaunchHistoryStore store = new AiLaunchHistoryStore(RuntimeEnvironment.getApplication());
        WorkspaceTarget workspace = new WorkspaceTarget("workspace-a", "远程开发",
            "hdr@192.168.1.153", 22, "~/project");
        store.record(workspace, AiCliLaunchCommand.Tool.CLAUDE,
            AiCliLaunchCommand.Mode.NEW_SESSION);
        store.record(workspace, AiCliLaunchCommand.Tool.CODEX,
            AiCliLaunchCommand.Mode.PICK_HISTORY);
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        activity.findViewById(R.id.ai_cli_center_manage_history).performClick();
        AlertDialog list = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(list);
        list.getListView().performItemClick(null, 0,
            list.getListView().getAdapter().getItemId(0));
        AlertDialog action = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(action);
        action.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        AlertDialog historyConfirm = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(historyConfirm);
        assertEquals("打开 Codex CLI 历史选择？", shadowOf(historyConfirm).getTitle());
        String confirmMessage = ((TextView) historyConfirm.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(confirmMessage.contains("执行命令：codex resume"));
        assertTrue(confirmMessage.contains("不会自动进入 tmux"));
        historyConfirm.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        Intent repeated = shadowOf(activity).getNextStartedActivity();
        assertNotNull(repeated);
        assertEquals(TermuxActivity.class.getName(), repeated.getComponent().getClassName());
        assertTrue(repeated.getStringExtra(TermuxActivity.EXTRA_STARTUP_COMMAND)
            .contains("exec codex resume"));
    }

    @Test
    public void historySummaryShowsCollapsedLocalCountWhenMoreThanThreeLaunchesExist() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        AiLaunchHistoryStore store = new AiLaunchHistoryStore(RuntimeEnvironment.getApplication());
        WorkspaceTarget workspace = new WorkspaceTarget("workspace-a", "远程开发",
            "hdr@192.168.1.153", 22, "~/project");
        store.record(workspace, AiCliLaunchCommand.Tool.CLAUDE,
            AiCliLaunchCommand.Mode.NEW_SESSION);
        store.record(workspace, AiCliLaunchCommand.Tool.CODEX,
            AiCliLaunchCommand.Mode.NEW_SESSION);
        store.record(workspace, AiCliLaunchCommand.Tool.CLAUDE,
            AiCliLaunchCommand.Mode.PICK_HISTORY);
        store.record(workspace, AiCliLaunchCommand.Tool.CODEX,
            AiCliLaunchCommand.Mode.PICK_HISTORY);
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        String summary = text(activity, R.id.ai_cli_center_history_summary);
        assertTrue(summary.contains("最近 3 条启动"));
        assertTrue(summary.contains("还有 1 条已折叠"));
        assertTrue(summary.contains("仅保存在当前工作区本地记录中"));
        assertEquals("重复：Codex CLI · 历史选择",
            text(activity, R.id.ai_cli_center_repeat_last));
    }

    @Test
    public void aiRiskCueExplainsPlainSshPolicyBeforeLaunch() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\",\"connectionPolicy\":\"ssh_only\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        String risk = text(activity, R.id.ai_cli_center_ai_risk);
        assertTrue(risk.contains("仅连接 SSH"));
        assertTrue(risk.contains("不自动进入 tmux"));
        assertTrue(risk.contains("恢复历史"));
    }

    @Test
    public void tmuxActionFallsBackToWorkbenchWhenWorkspaceIsIncomplete() {
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        activity.findViewById(R.id.ai_cli_center_open_tmux).performClick();

        assertNextActivity(activity, WorkspaceActivity.class);
    }

    @Test
    public void gitActionFallsBackToWorkbenchWhenWorkspaceIsIncomplete() {
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        activity.findViewById(R.id.ai_cli_center_open_git).performClick();

        assertNextActivity(activity, WorkspaceActivity.class);
    }

    @Test
    public void diagnosticActionFallsBackToWorkbenchWhenWorkspaceIsIncomplete() {
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        activity.findViewById(R.id.ai_cli_center_open_diagnostic).performClick();

        assertNextActivity(activity, WorkspaceActivity.class);
    }

    @Test
    public void projectTasksActionFallsBackToWorkbenchWhenWorkspaceIsIncomplete() {
        AiCliSessionCenterActivity activity = Robolectric.buildActivity(
            AiCliSessionCenterActivity.class).setup().get();

        activity.findViewById(R.id.ai_cli_center_open_project_tasks).performClick();

        assertNextActivity(activity, WorkspaceActivity.class);
    }

    private static String text(AiCliSessionCenterActivity activity, int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }

    private static void assertNextActivity(AiCliSessionCenterActivity activity,
                                           Class<?> expectedClass) {
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(expectedClass.getName(), intent.getComponent().getClassName());
        if (expectedClass == WorkspaceActivity.class) {
            assertTrue("从 AI 会话中心进入工作区必须显示上一页返回入口",
                intent.getBooleanExtra(WorkspaceActivity.EXTRA_SHOW_BACK, false));
        }
    }
}
