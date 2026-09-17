package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public class AiLaunchRecorderTest {

    private Context mContext;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.getApplication();
        mContext.getSharedPreferences(WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit();
        mContext.getSharedPreferences(AiLaunchHistoryStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit();
    }

    @Test
    public void ignoresMissingWorkspaceWithoutCreatingHistory() {
        boolean recorded = AiLaunchRecorder.recordActiveIfConfigured(mContext,
            AiCliLaunchCommand.Tool.CLAUDE, AiCliLaunchCommand.Mode.NEW_SESSION);

        assertFalse(recorded);
        assertEquals(0, new AiLaunchHistoryStore(mContext).readForWorkspace("workspace-a").size());
    }

    @Test
    public void recordsConfiguredWorkspaceLaunchAcrossEntrypoints() {
        mContext.getSharedPreferences(WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES,
                "[{\"id\":\"workspace-a\",\"name\":\"远程开发\",\"host\":\"hdr@192.168.1.153\",\"port\":\"22\",\"path\":\"~/project\",\"remotePort\":\"5173\",\"localPort\":\"5173\"}]")
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();

        boolean recorded = AiLaunchRecorder.recordActiveIfConfigured(mContext,
            AiCliLaunchCommand.Tool.CODEX, AiCliLaunchCommand.Mode.PICK_HISTORY);

        assertTrue(recorded);
        AiLaunchHistoryStore.Entry entry = new AiLaunchHistoryStore(mContext)
            .readForWorkspace("workspace-a").get(0);
        assertEquals("远程开发", entry.workspaceName);
        assertEquals("hdr@192.168.1.153", entry.host);
        assertEquals(22, entry.port);
        assertEquals("~/project", entry.path);
        assertEquals(AiCliLaunchCommand.Tool.CODEX, entry.tool);
        assertEquals(AiCliLaunchCommand.Mode.PICK_HISTORY, entry.mode);
    }
}
