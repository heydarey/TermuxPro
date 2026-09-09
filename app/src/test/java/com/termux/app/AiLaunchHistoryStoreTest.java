package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AiLaunchHistoryStoreTest {
    private AiLaunchHistoryStore store;

    @Before
    public void setUp() {
        RuntimeEnvironment.getApplication().getSharedPreferences(
            AiLaunchHistoryStore.PREFERENCES_NAME, Context.MODE_PRIVATE).edit().clear().commit();
        store = new AiLaunchHistoryStore(RuntimeEnvironment.getApplication());
    }

    @Test
    public void recordsOnlyTermuxProLaunchesByWorkspace() {
        WorkspaceTarget workspaceA = new WorkspaceTarget("a", "A", "a@example.com", 22, "~/a");
        WorkspaceTarget workspaceB = new WorkspaceTarget("b", "B", "b@example.com", 2222, "~/b");

        store.record(workspaceA, AiCliLaunchCommand.Tool.CLAUDE,
            AiCliLaunchCommand.Mode.NEW_SESSION);
        store.record(workspaceB, AiCliLaunchCommand.Tool.CODEX,
            AiCliLaunchCommand.Mode.PICK_HISTORY);

        List<AiLaunchHistoryStore.Entry> aEntries = store.readForWorkspace("a");
        assertEquals(1, aEntries.size());
        assertEquals("a@example.com", aEntries.get(0).host);
        assertEquals(AiCliLaunchCommand.Tool.CLAUDE, aEntries.get(0).tool);
        assertEquals(AiCliLaunchCommand.Mode.NEW_SESSION, aEntries.get(0).mode);

        List<AiLaunchHistoryStore.Entry> bEntries = store.readForWorkspace("b");
        assertEquals(1, bEntries.size());
        assertEquals("b@example.com", bEntries.get(0).host);
        assertEquals(AiCliLaunchCommand.Tool.CODEX, bEntries.get(0).tool);
        assertEquals(AiCliLaunchCommand.Mode.PICK_HISTORY, bEntries.get(0).mode);
    }

    @Test
    public void keepsOnlyRecentEntriesPerWorkspace() {
        WorkspaceTarget workspace = new WorkspaceTarget("a", "A", "a@example.com", 22, "~/a");
        for (int index = 0; index < 8; index++) {
            store.record(workspace, AiCliLaunchCommand.Tool.CODEX,
                index % 2 == 0 ? AiCliLaunchCommand.Mode.NEW_SESSION
                    : AiCliLaunchCommand.Mode.PICK_HISTORY);
        }

        List<AiLaunchHistoryStore.Entry> entries = store.readForWorkspace("a");
        assertEquals(5, entries.size());
        assertTrue(entries.get(0).launchedAtMillis >= entries.get(4).launchedAtMillis);
    }

    @Test
    public void clearsOnlySelectedWorkspace() {
        WorkspaceTarget workspaceA = new WorkspaceTarget("a", "A", "a@example.com", 22, "~/a");
        WorkspaceTarget workspaceB = new WorkspaceTarget("b", "B", "b@example.com", 22, "~/b");
        store.record(workspaceA, AiCliLaunchCommand.Tool.CLAUDE,
            AiCliLaunchCommand.Mode.NEW_SESSION);
        store.record(workspaceB, AiCliLaunchCommand.Tool.CODEX,
            AiCliLaunchCommand.Mode.NEW_SESSION);

        store.clearWorkspace("a");

        assertTrue(store.readForWorkspace("a").isEmpty());
        assertEquals(1, store.readForWorkspace("b").size());
    }
}
