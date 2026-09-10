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

    @Test
    public void confirmationNamesRemoteTargetAndCommand() throws Exception {
        ProjectTasksActivity activity = Robolectric.buildActivity(ProjectTasksActivity.class,
            ProjectTasksActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/project", "11111111-2222-3333-4444-555555555555"))
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
                "hdr@192.168.1.153", 22, "~/project", "11111111-2222-3333-4444-555555555555"))
            .setup().get();

        View sessions = activity.findViewById(R.id.project_tasks_sessions_button);
        assertEquals(View.VISIBLE, sessions.getVisibility());
        sessions.performClick();

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertNotNull(intent);
        assertEquals(TaskSessionsActivity.class.getName(), intent.getComponent().getClassName());
    }
}
