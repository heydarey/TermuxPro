package com.termux.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.termux.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

import java.lang.reflect.Method;

/** 验证项目任务增值层在执行前保留远端目标上下文。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public final class ProjectTasksActivityTest {
    private static final String OWNER = "11111111-2222-3333-4444-555555555555";


    @Test
    public void confirmationNamesRemoteTargetAndCommand() throws Exception {
        ProjectTasksActivity activity = Robolectric.buildActivity(ProjectTasksActivity.class,
            ProjectTasksActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/project", OWNER))
            .setup().get();

        Method confirmTask = ProjectTasksActivity.class.getDeclaredMethod("confirmTask",
            ProjectTaskDetector.Task.class);
        confirmTask.setAccessible(true);
        confirmTask.invoke(activity, new ProjectTaskDetector.Task("test", "pnpm test"));

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        String message = ((TextView) dialog.findViewById(android.R.id.message)).getText().toString();
        assertTrue(message.contains("目标：hdr@192.168.1.153:22 · ~/project"));
        assertTrue(message.contains("命令："));
        assertTrue(message.contains("pnpm test"));
        assertTrue(message.contains("新的持久终端会话"));
    }

    @Test
    public void validWorkspaceCanOpenTaskSessionManager() {
        ProjectTasksActivity activity = Robolectric.buildActivity(ProjectTasksActivity.class,
            ProjectTasksActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/project", OWNER))
            .setup().get();

        View sessions = activity.findViewById(R.id.project_tasks_sessions_button);
        assertEquals(View.VISIBLE, sessions.getVisibility());
        sessions.performClick();

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertNotNull(intent);
        assertEquals(TaskSessionsActivity.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void taskSessionSummaryHighlightsOnlyCurrentWorkspaceTasks() {
        String current = WorkspaceCommandBuilder.workspaceFingerprint(
            "hdr@192.168.1.153", 22, "~/project");
        String output = session("mobile-task-current", 1, false, 1788153600L, 1788157200L,
            OWNER, current)
            + session("mobile-task-other", 1, false, 1788150000L, 1788153600L,
            OWNER, "other-workspace");

        String summary = ProjectTasksActivity.taskSessionSummaryText(RuntimeEnvironment.getApplication(),
            "hdr@192.168.1.153", 22, "~/project", OWNER, output);

        assertTrue(summary.contains("当前工作区有 1 个项目任务会话"));
        assertTrue(summary.contains("mobile-task-current"));
        assertTrue(summary.contains("后台运行"));
        assertTrue(summary.contains("另有 1 个其他工作区任务不会自动进入"));
        assertTrue(!summary.contains("mobile-task-other ·"));
    }

    @Test
    public void taskSessionSummaryDoesNotAutoRecoverOtherWorkspaceTasks() {
        String output = session("mobile-task-other", 1, true, 1788150000L, 1788153600L,
            OWNER, "other-workspace");

        String summary = ProjectTasksActivity.taskSessionSummaryText(RuntimeEnvironment.getApplication(),
            "hdr@192.168.1.153", 22, "~/project", OWNER, output);

        assertTrue(summary.contains("当前工作区暂无任务会话"));
        assertTrue(summary.contains("1 个其他工作区任务"));
        assertTrue(summary.contains("不会自动进入或管理"));
        assertTrue(!summary.contains("mobile-task-other"));
    }

    private static String session(String name, int windows, boolean attached, long created,
                                  long activity, String owner, String fingerprint) {
        return name + "\0" + windows + "\0" + (attached ? "1" : "0") + "\0"
            + created + "\0" + activity + "\0" + owner + "\0" + fingerprint + "\0";
    }
}
