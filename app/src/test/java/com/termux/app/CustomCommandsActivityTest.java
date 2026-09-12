package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public class CustomCommandsActivityTest {

    @Before
    public void setUp() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, 0).edit().clear()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"移动端\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a").commit();
        RuntimeEnvironment.getApplication().getSharedPreferences(
            "termuxpro_custom_commands", 0).edit().clear().commit();
    }

    @Test
    public void showsExplicitTargetAndEmptyCreationPath() {
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();

        assertEquals("移动端", ((TextView) activity.findViewById(
            R.id.custom_commands_target)).getText().toString());
        assertTrue(((TextView) activity.findViewById(R.id.custom_commands_target_details))
            .getText().toString().contains("hdr@192.168.1.153:22"));
        assertEquals(View.VISIBLE, activity.findViewById(R.id.custom_commands_empty).getVisibility());
        assertEquals(View.VISIBLE, activity.findViewById(
            R.id.custom_commands_template_hint).getVisibility());
        assertEquals(View.GONE, activity.findViewById(
            R.id.custom_commands_action_feedback).getVisibility());
        assertEquals(View.GONE, activity.findViewById(
            R.id.custom_commands_scenario_hint).getVisibility());
        assertEquals(View.VISIBLE, activity.findViewById(
            R.id.custom_commands_backup).getVisibility());
        assertEquals("选用模板", ((TextView) activity.findViewById(
            R.id.custom_commands_templates)).getText().toString());
        assertEquals("新建指令", ((TextView) activity.findViewById(
            R.id.custom_commands_add)).getText().toString());
        TextView templateHint = activity.findViewById(R.id.custom_commands_template_hint);
        assertEquals(activity.getString(R.string.custom_commands_template_hint),
            templateHint.getContentDescription().toString());
        assertEquals(R.string.custom_commands_template_hint,
            CustomCommandsActivity.templateHintResForFontScale(1.49f));
        assertEquals(R.string.custom_commands_template_hint_compact,
            CustomCommandsActivity.templateHintResForFontScale(1.5f));

        activity.findViewById(R.id.custom_commands_add).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        assertEquals(activity.getColor(R.color.tp_primary),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).getCurrentTextColor());
        assertEquals(activity.getColor(R.color.tp_text_secondary),
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getCurrentTextColor());
        ((EditText) dialog.findViewById(R.id.custom_command_name_input)).setText("查看状态");
        ((EditText) dialog.findViewById(R.id.custom_command_value_input)).setText("git status --short");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        LinearLayout list = activity.findViewById(R.id.custom_commands_list);
        assertEquals(2, list.getChildCount());
        assertEquals("未分组 场景", ((TextView) list.getChildAt(0)).getText().toString());
        assertEquals(View.GONE, activity.findViewById(R.id.custom_commands_empty).getVisibility());
        assertEquals(View.GONE, activity.findViewById(
            R.id.custom_commands_template_hint).getVisibility());
        assertEquals(View.VISIBLE, activity.findViewById(
            R.id.custom_commands_scenario_hint).getVisibility());
        assertEquals(View.VISIBLE, activity.findViewById(
            R.id.custom_commands_backup).getVisibility());
        TextView feedback = activity.findViewById(R.id.custom_commands_action_feedback);
        assertEquals(View.VISIBLE, feedback.getVisibility());
        assertTrue(feedback.getText().toString().contains("已保存“查看状态”"));
        assertTrue(feedback.getText().toString().contains("查看并运行"));
        assertEquals(feedback.getText().toString(), feedback.getContentDescription().toString());
    }

    @Test
    public void exportsCurrentWorkspaceCommandsToClipboardWithoutExecuting() throws Exception {
        CustomCommandStore store = new CustomCommandStore(RuntimeEnvironment.getApplication());
        store.save("workspace-a", new CustomCommand("git-status", "Git 状态",
            "git status --short", "", "Git", true,
            CustomCommand.Confirmation.DANGEROUS_ONLY));
        store.save("workspace-a", new CustomCommand("codex-resume", "Codex 历史",
            "codex resume", "apps/mobile", "AI", true,
            CustomCommand.Confirmation.ALWAYS));
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();

        View backupButton = activity.findViewById(R.id.custom_commands_backup);
        assertEquals(View.VISIBLE, backupButton.getVisibility());
        assertEquals(activity.getString(R.string.custom_commands_backup_description),
            backupButton.getContentDescription().toString());
        backupButton.performClick();
        shadowOf(Looper.getMainLooper()).idle();
        AlertDialog actionDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertEquals(activity.getString(R.string.custom_commands_backup_title),
            shadowOf(actionDialog).getTitle());
        ListView actionList = actionDialog.getListView();
        assertEquals("复制备份 JSON", actionList.getAdapter().getItem(0).toString());
        assertEquals("从剪贴板导入 JSON", actionList.getAdapter().getItem(1).toString());
        actionList.performItemClick(actionList.getAdapter().getView(0, null, actionList), 0,
            actionList.getAdapter().getItemId(0));
        shadowOf(Looper.getMainLooper()).idle();

        ClipboardManager clipboard = (ClipboardManager) RuntimeEnvironment.getApplication()
            .getSystemService(Context.CLIPBOARD_SERVICE);
        assertNotNull(clipboard);
        assertNotNull(clipboard.getPrimaryClip());
        String text = clipboard.getPrimaryClip().getItemAt(0).coerceToText(activity).toString();
        JSONObject backup = new JSONObject(text);
        assertEquals("termuxpro.customCommands.v1", backup.getString("schema"));
        assertEquals("workspace-a", backup.getString("workspaceId"));
        assertEquals("移动端", backup.getString("workspaceName"));
        assertEquals("hdr@192.168.1.153", backup.getJSONObject("workspace").getString("host"));
        assertEquals(22, backup.getJSONObject("workspace").getInt("port"));
        assertEquals("~/project", backup.getJSONObject("workspace").getString("path"));
        JSONArray commands = backup.getJSONArray("commands");
        assertEquals(2, commands.length());
        assertEquals("Git 状态", commands.getJSONObject(0).getString("name"));
        assertEquals("git status --short", commands.getJSONObject(0).getString("command"));
        assertEquals("DANGEROUS_ONLY", commands.getJSONObject(0).getString("confirmation"));
        assertEquals("Codex 历史", commands.getJSONObject(1).getString("name"));
        assertEquals("apps/mobile", commands.getJSONObject(1).getString("workingDirectory"));
        assertEquals(null, shadowOf(activity).getNextStartedActivity());
        TextView feedback = activity.findViewById(R.id.custom_commands_action_feedback);
        assertTrue(feedback.getText().toString().contains("已复制 2 条快捷指令 JSON"));
        assertTrue(feedback.getText().toString().contains("不会执行远端命令"));
    }

    @Test
    public void importsClipboardBackupAfterPreviewWithoutExecuting() throws Exception {
        CustomCommandStore store = new CustomCommandStore(RuntimeEnvironment.getApplication());
        store.save("workspace-a", new CustomCommand("git-existing", "Git 状态",
            "git status --short", "", "Git", true,
            CustomCommand.Confirmation.DANGEROUS_ONLY));
        WorkspaceTarget source = new WorkspaceTarget("workspace-old", "旧项目",
            "dev@example.com", 2222, "~/old");
        String backup = CustomCommandsActivity.buildExportJson(source, Arrays.asList(
            new CustomCommand("git-old", "Git 状态", "git status --short --branch", "",
                "Git", true, CustomCommand.Confirmation.DANGEROUS_ONLY),
            new CustomCommand("ai-old", "继续 Codex", "codex resume", "apps/mobile",
                "AI", false, CustomCommand.Confirmation.ALWAYS)
        )).toString();
        ClipboardManager clipboard = (ClipboardManager) RuntimeEnvironment.getApplication()
            .getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("backup", backup));
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();

        activity.findViewById(R.id.custom_commands_backup).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        ListView actionList = ShadowAlertDialog.getLatestAlertDialog().getListView();
        assertEquals("复制备份 JSON", actionList.getAdapter().getItem(0).toString());
        assertEquals("从剪贴板导入 JSON", actionList.getAdapter().getItem(1).toString());
        actionList.performItemClick(actionList.getAdapter().getView(1, null, actionList), 1,
            actionList.getAdapter().getItemId(1));
        shadowOf(Looper.getMainLooper()).idle();
        AlertDialog preview = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(preview);
        assertEquals(activity.getString(R.string.custom_commands_import_preview_title),
            shadowOf(preview).getTitle());
        String message = ((TextView) preview.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(message.contains("旧项目"));
        assertTrue(message.contains("移动端"));
        assertTrue(message.contains("不会执行远端命令"));
        assertEquals(1, new CustomCommandStore(RuntimeEnvironment.getApplication())
            .list("workspace-a").size());
        assertEquals(null, shadowOf(activity).getNextStartedActivity());

        preview.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        List<CustomCommand> imported = new CustomCommandStore(RuntimeEnvironment.getApplication())
            .list("workspace-a");
        assertEquals(3, imported.size());
        assertEquals("Git 状态", imported.get(0).name);
        assertEquals("Git 状态（导入）", imported.get(1).name);
        assertEquals("git status --short --branch", imported.get(1).command);
        assertEquals("继续 Codex", imported.get(2).name);
        assertFalse(imported.get(2).enabled);
        assertEquals(null, shadowOf(activity).getNextStartedActivity());
        TextView feedback = activity.findViewById(R.id.custom_commands_action_feedback);
        assertTrue(feedback.getText().toString().contains("已导入 2 条快捷指令"));
    }

    @Test
    public void rejectsInvalidOrSensitiveClipboardImport() {
        ClipboardManager clipboard = (ClipboardManager) RuntimeEnvironment.getApplication()
            .getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("bad",
            "{\"schema\":\"termuxpro.customCommands.v1\",\"commands\":[{\"name\":\"危险\",\"command\":\"export TOKEN=abc\"}]}"));
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();

        activity.findViewById(R.id.custom_commands_backup).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        ListView actionList = ShadowAlertDialog.getLatestAlertDialog().getListView();
        assertEquals(1, actionList.getAdapter().getCount());
        assertEquals("从剪贴板导入 JSON", actionList.getAdapter().getItem(0).toString());
        actionList.performItemClick(actionList.getAdapter().getView(0, null, actionList), 0,
            actionList.getAdapter().getItemId(0));
        shadowOf(Looper.getMainLooper()).idle();

        assertEquals(0, new CustomCommandStore(RuntimeEnvironment.getApplication())
            .list("workspace-a").size());
        TextView feedback = activity.findViewById(R.id.custom_commands_action_feedback);
        assertTrue(feedback.getText().toString().contains("不是有效的 TermuxPro 快捷指令备份"));
        assertEquals(null, shadowOf(activity).getNextStartedActivity());
    }

    @Test
    public void importedDuplicateNamesRemainReadableAndBounded() {
        HashSet<String> names = new HashSet<>();
        names.add("运行测试");

        assertEquals("运行测试（导入）",
            CustomCommandsActivity.uniqueImportedName("运行测试", names));
        assertTrue(names.contains("运行测试（导入）"));
    }

    @Test
    public void templatesPrefillEditorWithoutSavingOrExecuting() {
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();

        activity.findViewById(R.id.custom_commands_templates).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        AlertDialog templateDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(templateDialog);
        assertEquals(activity.getString(R.string.custom_commands_template_title),
            shadowOf(templateDialog).getTitle());
        ListView listView = templateDialog.getListView();
        assertTrue(listView.getAdapter().getCount() >= 8);
        assertEquals("Codex：新建独立会话", listView.getAdapter().getItem(0).toString());
        assertEquals("Codex：选择历史会话", listView.getAdapter().getItem(1).toString());
        assertEquals("Claude：新建独立会话", listView.getAdapter().getItem(2).toString());
        assertEquals("Claude：选择历史会话", listView.getAdapter().getItem(3).toString());
        assertFalse(listView.getAdapter().getItem(0).toString().contains("\n"));
        assertFalse(listView.getAdapter().getItem(3).toString().contains("claude --resume"));
        assertEquals(0, new CustomCommandStore(RuntimeEnvironment.getApplication())
            .list("workspace-a").size());
        assertEquals(null, shadowOf(activity).getNextStartedActivity());

        listView.performItemClick(listView.getAdapter().getView(0, null, listView), 0,
            listView.getAdapter().getItemId(0));
        shadowOf(Looper.getMainLooper()).idle();
        AlertDialog editor = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(editor);
        assertEquals(activity.getString(R.string.custom_commands_create_title),
            shadowOf(editor).getTitle());
        assertEquals("Codex：新建独立会话", ((EditText) editor.findViewById(
            R.id.custom_command_name_input)).getText().toString());
        assertEquals("codex", ((EditText) editor.findViewById(
            R.id.custom_command_value_input)).getText().toString());
        assertEquals("AI", ((EditText) editor.findViewById(
            R.id.custom_command_group_input)).getText().toString());
        assertEquals(0, new CustomCommandStore(RuntimeEnvironment.getApplication())
            .list("workspace-a").size());

        editor.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(1, new CustomCommandStore(RuntimeEnvironment.getApplication())
            .list("workspace-a").size());
        assertEquals(null, shadowOf(activity).getNextStartedActivity());
    }

    @Test
    public void previewNamesTargetAndStartsNewSshSessionOnlyAfterConfirmation() {
        CustomCommandStore store = new CustomCommandStore(RuntimeEnvironment.getApplication());
        store.save("workspace-a", CustomCommand.create("查看状态", "git status --short", "", "Git",
            CustomCommand.Confirmation.ALWAYS));
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();
        LinearLayout list = activity.findViewById(R.id.custom_commands_list);

        list.getChildAt(1).findViewById(R.id.custom_command_run).performClick();
        AlertDialog preview = ShadowAlertDialog.getLatestAlertDialog();
        String message = ((TextView) preview.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(message.contains("hdr@192.168.1.153:22"));
        assertTrue(message.contains("~/project"));
        assertTrue(message.contains("git status --short"));
        assertEquals(null, shadowOf(activity).getNextStartedActivity());

        preview.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(TermuxActivity.class.getName(), intent.getComponent().getClassName());
        assertTrue(intent.getBooleanExtra(TermuxActivity.EXTRA_NEW_SESSION, false));
        assertTrue(intent.getStringExtra(TermuxActivity.EXTRA_STARTUP_COMMAND)
            .contains("hdr@192.168.1.153"));
    }

    @Test
    public void safeCommandCanRunDirectlyWhenConfiguredForDangerousOnly() {
        CustomCommandStore store = new CustomCommandStore(RuntimeEnvironment.getApplication());
        store.save("workspace-a", new CustomCommand("safe", "查看状态", "git status --short", "",
            "Git", true, CustomCommand.Confirmation.DANGEROUS_ONLY));
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();
        LinearLayout list = activity.findViewById(R.id.custom_commands_list);

        assertEquals("Git 场景", ((TextView) list.getChildAt(0)).getText().toString());
        assertEquals("git status --short", ((TextView) list.getChildAt(1)
            .findViewById(R.id.custom_command_value)).getText().toString());
        assertEquals(activity.getString(R.string.custom_commands_run_now),
            ((TextView) list.getChildAt(1).findViewById(R.id.custom_command_run)).getText().toString());
        assertEquals("编辑/更多", ((TextView) list.getChildAt(1)
            .findViewById(R.id.custom_command_manage)).getText().toString());
        list.getChildAt(1).findViewById(R.id.custom_command_run).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertNotNull(intent);
        assertTrue(intent.getStringExtra(TermuxActivity.EXTRA_STARTUP_COMMAND)
            .contains("git status --short"));
    }

    @Test
    public void filtersCommandsByNameGroupCommandAndDirectoryWithoutChangingStoreOrder() {
        CustomCommand git = new CustomCommand("git-status", "查看状态", "git status --short", "",
            "Git", true, CustomCommand.Confirmation.DANGEROUS_ONLY);
        CustomCommand ai = new CustomCommand("ai-resume", "继续 Codex", "codex resume", "apps/mobile",
            "AI", true, CustomCommand.Confirmation.ALWAYS);
        CustomCommand test = new CustomCommand("frontend-test", "前端检查", "pnpm test", "apps/web",
            "测试", true, CustomCommand.Confirmation.ALWAYS);
        CustomCommand build = new CustomCommand("android-build", "构建 Android", "./gradlew assembleDebug",
            "", "构建", true, CustomCommand.Confirmation.ALWAYS);
        List<CustomCommand> commands = Arrays.asList(git, ai, test, build);

        assertEquals(Arrays.asList(ai), CustomCommandsActivity.filterCommands(commands, "codex"));
        assertEquals(Arrays.asList(git), CustomCommandsActivity.filterCommands(commands, "git"));
        assertEquals(Arrays.asList(ai), CustomCommandsActivity.filterCommands(commands, "mobile"));
        assertEquals(Arrays.asList(test), CustomCommandsActivity.filterCommands(commands, "测试"));
        assertEquals(commands, CustomCommandsActivity.filterCommands(commands, "  "));
        assertTrue(CustomCommandsActivity.filterCommands(commands, "not-found").isEmpty());

        CustomCommandStore store = new CustomCommandStore(RuntimeEnvironment.getApplication());
        for (CustomCommand command : commands) store.save("workspace-a", command);
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();
        EditText search = activity.findViewById(R.id.custom_commands_search_input);
        search.setText("codex");
        shadowOf(Looper.getMainLooper()).idle();

        assertEquals("已显示 1 / 4 条快捷指令", ((TextView) activity.findViewById(
            R.id.custom_commands_search_summary)).getText().toString());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.custom_commands_clear_search).getVisibility());
        LinearLayout list = activity.findViewById(R.id.custom_commands_list);
        assertEquals(2, list.getChildCount());
        assertTrue(((TextView) list.getChildAt(1).findViewById(R.id.custom_command_name)).getText()
            .toString().contains("Codex"));

        search.setText("not-found");
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(View.VISIBLE, activity.findViewById(R.id.custom_commands_search_empty).getVisibility());
        activity.findViewById(R.id.custom_commands_clear_search).performClick();
        assertEquals("", search.getText().toString());
        assertEquals(8, list.getChildCount());
    }

    @Test
    public void keepsSmallShortcutListsFocusedOnExecutionInsteadOfShowingUnusedSearch() {
        CustomCommandStore store = new CustomCommandStore(RuntimeEnvironment.getApplication());
        store.save("workspace-a", CustomCommand.create("查看状态", "git status --short", "", "Git",
            CustomCommand.Confirmation.ALWAYS));
        store.save("workspace-a", CustomCommand.create("继续 Codex", "codex resume", "", "AI",
            CustomCommand.Confirmation.ALWAYS));
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();

        assertEquals(View.GONE, activity.findViewById(R.id.custom_commands_search_label)
            .getVisibility());
        assertEquals(View.GONE, ((View) activity.findViewById(R.id.custom_commands_search_input)
            .getParent()).getVisibility());
    }

    @Test
    public void managementCopyLabelExplainsThatItCreatesANewCommand() {
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();

        assertEquals("复制为新指令", activity.getString(R.string.custom_commands_copy));
    }

    @Test
    public void groupsCommandsByScenarioWithoutChangingStoredOrder() {
        CustomCommandStore store = new CustomCommandStore(RuntimeEnvironment.getApplication());
        store.save("workspace-a", new CustomCommand("git-status", "Git 状态",
            "git status --short", "", "Git", true, CustomCommand.Confirmation.DANGEROUS_ONLY));
        store.save("workspace-a", new CustomCommand("ai-resume", "Codex 历史",
            "codex resume", "", "AI", true, CustomCommand.Confirmation.ALWAYS));
        store.save("workspace-a", new CustomCommand("git-log", "Git 提交",
            "git log --oneline -n 5", "", "Git", true, CustomCommand.Confirmation.DANGEROUS_ONLY));

        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();
        LinearLayout list = activity.findViewById(R.id.custom_commands_list);

        assertEquals(5, list.getChildCount());
        assertEquals("Git 场景", ((TextView) list.getChildAt(0)).getText().toString());
        assertEquals("Git 状态", ((TextView) list.getChildAt(1)
            .findViewById(R.id.custom_command_name)).getText().toString());
        assertEquals("Git 提交", ((TextView) list.getChildAt(2)
            .findViewById(R.id.custom_command_name)).getText().toString());
        assertEquals("AI 场景", ((TextView) list.getChildAt(3)).getText().toString());
        assertEquals("Codex 历史", ((TextView) list.getChildAt(4)
            .findViewById(R.id.custom_command_name)).getText().toString());
        assertEquals("Git 状态", store.list("workspace-a").get(0).name);
        assertEquals("Codex 历史", store.list("workspace-a").get(1).name);
        assertEquals("Git 提交", store.list("workspace-a").get(2).name);
    }

    @Test
    public void invalidWorkspaceDisablesCreation() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            WorkspaceTargetStore.PREFERENCES_NAME, 0).edit().clear().commit();
        CustomCommandsActivity activity = Robolectric.buildActivity(
            CustomCommandsActivity.class).setup().get();

        assertFalse(activity.findViewById(R.id.custom_commands_add).isEnabled());
        assertFalse(activity.findViewById(R.id.custom_commands_templates).isEnabled());
        assertEquals(activity.getString(R.string.custom_commands_invalid_workspace),
            ((TextView) activity.findViewById(R.id.custom_commands_empty)).getText().toString());
        assertEquals(View.GONE, activity.findViewById(
            R.id.custom_commands_template_hint).getVisibility());
        assertEquals(View.GONE, activity.findViewById(
            R.id.custom_commands_action_feedback).getVisibility());
        assertEquals(View.GONE, activity.findViewById(
            R.id.custom_commands_backup).getVisibility());
    }
}
