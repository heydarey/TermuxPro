package com.termux.app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 只记录 TermuxPro 自己发起的 AI CLI 启动记录。
 *
 * 不读取 Claude/Codex 私有历史、不保存终端输出、不跨工作区展示，避免共享账号场景泄露或误续接。
 */
final class AiLaunchHistoryStore {
    static final String PREFERENCES_NAME = "termuxpro_ai_launch_history";
    private static final String KEY_ENTRIES = "entries_v1";
    private static final int MAX_TOTAL_ENTRIES = 30;
    private static final int MAX_WORKSPACE_ENTRIES = 5;

    private final SharedPreferences mPreferences;

    AiLaunchHistoryStore(@NonNull Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    void record(@NonNull WorkspaceTarget workspace, @NonNull AiCliLaunchCommand.Tool tool,
                @NonNull AiCliLaunchCommand.Mode mode) {
        List<Entry> entries = readAll();
        entries.add(0, new Entry(workspace.id, workspace.name, workspace.host, workspace.port,
            workspace.path, tool, mode, System.currentTimeMillis()));
        write(trim(entries));
    }

    @NonNull
    List<Entry> readForWorkspace(@NonNull String workspaceId) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : readAll()) {
            if (!workspaceId.equals(entry.workspaceId)) continue;
            result.add(entry);
            if (result.size() >= MAX_WORKSPACE_ENTRIES) break;
        }
        return result;
    }

    void clearWorkspace(@NonNull String workspaceId) {
        List<Entry> kept = new ArrayList<>();
        for (Entry entry : readAll()) {
            if (!workspaceId.equals(entry.workspaceId)) kept.add(entry);
        }
        write(kept);
    }

    boolean deleteEntry(@NonNull String workspaceId, long launchedAtMillis,
                        @NonNull AiCliLaunchCommand.Tool tool,
                        @NonNull AiCliLaunchCommand.Mode mode) {
        List<Entry> kept = new ArrayList<>();
        boolean deleted = false;
        for (Entry entry : readAll()) {
            if (!deleted && workspaceId.equals(entry.workspaceId)
                && launchedAtMillis == entry.launchedAtMillis
                && tool == entry.tool && mode == entry.mode) {
                deleted = true;
                continue;
            }
            kept.add(entry);
        }
        if (deleted) write(kept);
        return deleted;
    }

    @NonNull
    private List<Entry> readAll() {
        String serialized = mPreferences.getString(KEY_ENTRIES, "[]");
        List<Entry> entries = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(serialized);
            for (int index = 0; index < array.length(); index++) {
                Entry entry = Entry.fromJson(array.optJSONObject(index));
                if (entry != null) entries.add(entry);
            }
        } catch (JSONException ignored) {
            return entries;
        }
        return entries;
    }

    @NonNull
    private List<Entry> trim(@NonNull List<Entry> entries) {
        List<Entry> result = new ArrayList<>();
        int total = 0;
        for (Entry entry : entries) {
            if (total >= MAX_TOTAL_ENTRIES) break;
            int workspaceCount = 0;
            for (Entry existing : result) {
                if (entry.workspaceId.equals(existing.workspaceId)) workspaceCount++;
            }
            if (workspaceCount >= MAX_WORKSPACE_ENTRIES) continue;
            result.add(entry);
            total++;
        }
        return result;
    }

    private void write(@NonNull List<Entry> entries) {
        JSONArray array = new JSONArray();
        for (Entry entry : entries) array.put(entry.toJson());
        mPreferences.edit().putString(KEY_ENTRIES, array.toString()).apply();
    }

    static final class Entry {
        @NonNull final String workspaceId;
        @NonNull final String workspaceName;
        @NonNull final String host;
        final int port;
        @NonNull final String path;
        @NonNull final AiCliLaunchCommand.Tool tool;
        @NonNull final AiCliLaunchCommand.Mode mode;
        final long launchedAtMillis;

        Entry(@NonNull String workspaceId, @NonNull String workspaceName, @NonNull String host,
              int port, @NonNull String path, @NonNull AiCliLaunchCommand.Tool tool,
              @NonNull AiCliLaunchCommand.Mode mode, long launchedAtMillis) {
            this.workspaceId = workspaceId;
            this.workspaceName = workspaceName;
            this.host = host;
            this.port = port;
            this.path = path;
            this.tool = tool;
            this.mode = mode;
            this.launchedAtMillis = launchedAtMillis;
        }

        @NonNull
        JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                object.put("workspaceId", workspaceId);
                object.put("workspaceName", workspaceName);
                object.put("host", host);
                object.put("port", port);
                object.put("path", path);
                object.put("tool", tool.name());
                object.put("mode", mode.name());
                object.put("launchedAtMillis", launchedAtMillis);
            } catch (JSONException ignored) {
                // JSONObject with primitive/string values should not fail; keep fail-closed if it does.
            }
            return object;
        }

        static Entry fromJson(JSONObject object) {
            if (object == null) return null;
            String workspaceId = object.optString("workspaceId", "").trim();
            String host = object.optString("host", "").trim();
            String path = object.optString("path", "").trim();
            if (workspaceId.isEmpty() || host.isEmpty() || path.isEmpty()) return null;
            try {
                return new Entry(workspaceId,
                    object.optString("workspaceName", "远程工作区").trim(),
                    host,
                    object.optInt("port", 22),
                    path,
                    AiCliLaunchCommand.Tool.valueOf(object.optString("tool")),
                    AiCliLaunchCommand.Mode.valueOf(object.optString("mode")),
                    object.optLong("launchedAtMillis", 0L));
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
    }
}
