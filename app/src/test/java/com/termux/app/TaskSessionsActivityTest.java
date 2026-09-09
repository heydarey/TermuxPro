package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;


import com.termux.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public class TaskSessionsActivityTest {
    @Test
    public void previewShowsOwnedAndUnknownSessionsWithDiscoverableCreateAction() {
        Intent intent = TaskSessionsActivity.newIntent(RuntimeEnvironment.getApplication(),
            "dev@example.com", 22, "~/project", "11111111-2222-3333-4444-555555555555")
            .putExtra(TaskSessionsActivity.EXTRA_UI_TEST_SESSIONS, true);
        TaskSessionsActivity activity = Robolectric.buildActivity(TaskSessionsActivity.class, intent)
            .setup().get();

        ListView sessions = activity.findViewById(R.id.task_sessions_list);
        View create = activity.findViewById(R.id.task_sessions_create_button);
        assertEquals(3, sessions.getAdapter().getCount());
        assertEquals(View.VISIBLE, create.getVisibility());
        TextView ownedRow = (TextView) sessions.getAdapter().getView(0, null, sessions);
        String rowText = ownedRow.getText().toString();
        assertTrue(rowText.startsWith("feature-login"));
        assertTrue(rowText.contains("当前工作区 · TermuxPro 创建"));
        assertTrue(rowText.contains("创建 "));
        assertTrue(rowText.contains("活跃 "));
        TextView otherWorkspaceRow = (TextView) sessions.getAdapter().getView(1, null, sessions);
        assertTrue(otherWorkspaceRow.getText().toString().contains("其他工作区"));
        TextView safetyHint = activity.findViewById(R.id.task_sessions_safety_hint);
        assertTrue(safetyHint.getText().toString().contains("当前工作区会话优先显示"));
        assertTrue(safetyHint.getText().toString().contains("必须确认后才能进入"));
        assertEquals(activity.getString(R.string.task_sessions_safety_hint),
            safetyHint.getContentDescription().toString());

        create.performClick();
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        assertNotNull(dialog.findViewById(R.id.task_session_name_input));
    }

    @Test
    public void largeFontUsesCompactSafetyHintWithoutWeakeningAccessibilityDescription() {
        assertEquals(R.string.task_sessions_safety_hint,
            TaskSessionsActivity.safetyHintResForFontScale(1.49f));
        assertEquals(R.string.task_sessions_safety_hint_compact,
            TaskSessionsActivity.safetyHintResForFontScale(1.5f));
        assertEquals(R.string.task_sessions_ready,
            TaskSessionsActivity.readyMessageResForFontScale(1.49f));
        assertEquals(R.string.task_sessions_ready_compact,
            TaskSessionsActivity.readyMessageResForFontScale(1.5f));
        assertEquals(R.string.task_sessions_row,
            TaskSessionsActivity.sessionRowResForFontScale(1.49f));
        assertEquals(R.string.task_sessions_row_compact,
            TaskSessionsActivity.sessionRowResForFontScale(1.5f));
    }

    @Test
    public void compactRowKeepsNameStateAndOwnershipBeforeLowPriorityTimestamps() {
        TaskSessionsActivity activity = previewActivity();
        ListView sessions = activity.findViewById(R.id.task_sessions_list);
        TextView status = activity.findViewById(R.id.task_sessions_status);
        TextView safetyHint = activity.findViewById(R.id.task_sessions_safety_hint);
        assertEquals(View.VISIBLE, status.getVisibility());
        assertTrue(status.getContentDescription().toString().contains("重命名或停止"));
        assertTrue(safetyHint.getContentDescription().toString().contains("必须确认后才能进入"));
        assertTrue(TaskSessionsActivity.hidesReadyMessageForFontScale(1.5f));
        assertTrue(!TaskSessionsActivity.hidesReadyMessageForFontScale(1.49f));
        // 行格式选择独立于远端结果，确保大字体仅收敛视觉信息而非放宽会话归属边界。
        assertEquals(R.string.task_sessions_row_compact,
            TaskSessionsActivity.sessionRowResForFontScale(1.5f));
        assertTrue(((TextView) sessions.getAdapter().getView(0, null, sessions)).getText().toString()
            .contains("当前工作区"));
    }

    @Test
    public void invalidNameKeepsCreateDialogOpenWithInlineError() {
        TaskSessionsActivity activity = previewActivity();
        activity.findViewById(R.id.task_sessions_create_button).performClick();
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        EditText input = dialog.findViewById(R.id.task_session_name_input);
        input.setText("bad name");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        assertTrue(dialog.isShowing());
    }

    @Test
    public void ownedSessionSeparatesDangerActionAndUnknownSessionHasAttachOnly() {
        TaskSessionsActivity activity = previewActivity();
        ListView sessions = activity.findViewById(R.id.task_sessions_list);

        sessions.performItemClick(sessions.getAdapter().getView(0, null, sessions), 0, 0);
        AlertDialog owned = (AlertDialog) ShadowDialog.getLatestDialog();
        assertEquals(2, owned.getListView().getAdapter().getCount());
        assertEquals(activity.getString(R.string.task_sessions_stop),
            owned.getButton(AlertDialog.BUTTON_NEUTRAL).getText().toString());
        owned.dismiss();

        sessions.performItemClick(sessions.getAdapter().getView(1, null, sessions), 1, 1);
        AlertDialog otherWorkspace = (AlertDialog) ShadowDialog.getLatestDialog();
        assertExternalAttachRequiresConfirmation(activity, otherWorkspace, "不属于当前工作区");
        otherWorkspace.dismiss();

        sessions.performItemClick(sessions.getAdapter().getView(2, null, sessions), 2, 2);
        AlertDialog unknown = (AlertDialog) ShadowDialog.getLatestDialog();
        assertExternalAttachRequiresConfirmation(activity, unknown, "无法可靠判断");
    }

    private void assertExternalAttachRequiresConfirmation(TaskSessionsActivity activity,
                                                          AlertDialog dialog,
                                                          String expectedWarning) {
        assertTrue(dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getText().length() > 0);
        assertEquals(activity.getString(R.string.task_sessions_attach_external_confirm),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        assertTrue(dialog.getButton(AlertDialog.BUTTON_NEUTRAL) == null
            || dialog.getButton(AlertDialog.BUTTON_NEUTRAL).getVisibility() != View.VISIBLE
            || dialog.getButton(AlertDialog.BUTTON_NEUTRAL).getText().length() == 0);
        assertTrue(((android.widget.TextView) dialog.findViewById(android.R.id.message))
            .getText().toString().contains(expectedWarning));
        assertTrue(((android.widget.TextView) dialog.findViewById(android.R.id.message))
            .getText().toString().contains("不会重命名、停止或写入"));
    }

    private TaskSessionsActivity previewActivity() {
        Intent intent = TaskSessionsActivity.newIntent(RuntimeEnvironment.getApplication(),
            "dev@example.com", 22, "~/project", "11111111-2222-3333-4444-555555555555")
            .putExtra(TaskSessionsActivity.EXTRA_UI_TEST_SESSIONS, true);
        return Robolectric.buildActivity(TaskSessionsActivity.class, intent).setup().get();
    }
}
