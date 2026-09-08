package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
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

import java.util.Arrays;
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
            R.id.custom_commands_scenario_hint).getVisibility());
        assertEquals("选用模板", ((TextView) activity.findViewById(
            R.id.custom_commands_templates)).getText().toString());
        assertEquals("新建指令", ((TextView) activity.findViewById(
            R.id.custom_commands_add)).getText().toString());

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
        assertTrue(listView.getAdapter().getCount() >= 7);
        assertTrue(listView.getAdapter().getItem(0).toString().contains("codex resume"));
        assertTrue(listView.getAdapter().getItem(1).toString().contains("claude"));
        assertFalse(listView.getAdapter().getItem(1).toString().contains("--continue"));
        assertTrue(listView.getAdapter().getItem(2).toString().contains("claude --resume"));
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
        assertEquals("Codex：打开历史会话", ((EditText) editor.findViewById(
            R.id.custom_command_name_input)).getText().toString());
        assertEquals("codex resume", ((EditText) editor.findViewById(
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
    }
}
