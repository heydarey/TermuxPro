package com.termux.app;

import android.content.Context;

import androidx.annotation.NonNull;

/** 统一记录 TermuxPro 增值入口发起的 AI CLI 启动，避免各入口历史不一致。 */
final class AiLaunchRecorder {

    private AiLaunchRecorder() {}

    static boolean recordActiveIfConfigured(@NonNull Context context,
                                            @NonNull AiCliLaunchCommand.Tool tool,
                                            @NonNull AiCliLaunchCommand.Mode mode) {
        WorkspaceTarget workspace = WorkspaceTargetStore.readActive(context);
        if (workspace == null || !workspace.isConfigured()) return false;
        new AiLaunchHistoryStore(context).record(workspace, tool, mode);
        return true;
    }
}
