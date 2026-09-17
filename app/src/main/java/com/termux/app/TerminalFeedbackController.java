package com.termux.app;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

/** 管理终端页应用内反馈，确保文案、显式配色载体和生命周期始终一起更新。 */
final class TerminalFeedbackController {

    static final long SHORT_DURATION_MS = 3500L;
    static final long LONG_DURATION_MS = 6000L;

    private final TextView banner;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable hideAction;
    private CharSequence persistentText;
    private boolean temporaryVisible;

    TerminalFeedbackController(TextView banner) {
        this.banner = banner;
    }

    void show(String text, boolean longDuration) {
        if (TextUtils.isEmpty(text) || text.trim().isEmpty()) return;
        if (hideAction != null) mainHandler.removeCallbacks(hideAction);
        temporaryVisible = true;
        banner.setText(text);
        banner.setContentDescription(text);
        banner.setVisibility(View.VISIBLE);
        banner.bringToFront();
        banner.announceForAccessibility(text);
        hideAction = this::hideTemporary;
        mainHandler.postDelayed(hideAction,
            longDuration ? LONG_DURATION_MS : SHORT_DURATION_MS);
    }

    void showPersistent(String text) {
        if (TextUtils.isEmpty(text) || text.trim().isEmpty()) return;
        persistentText = text;
        if (temporaryVisible) return;
        showPersistentNow();
    }

    void hidePersistent() {
        persistentText = null;
        if (!temporaryVisible) hide();
    }

    private void hideTemporary() {
        if (hideAction != null) mainHandler.removeCallbacks(hideAction);
        hideAction = null;
        temporaryVisible = false;
        if (!TextUtils.isEmpty(persistentText)) {
            showPersistentNow();
        } else {
            banner.setVisibility(View.GONE);
        }
    }

    private void showPersistentNow() {
        if (TextUtils.equals(banner.getText(), persistentText) &&
            banner.getVisibility() == View.VISIBLE) return;
        banner.setText(persistentText);
        banner.setContentDescription(persistentText);
        banner.setVisibility(View.VISIBLE);
        banner.bringToFront();
        banner.announceForAccessibility(persistentText);
    }

    void hide() {
        if (hideAction != null) mainHandler.removeCallbacks(hideAction);
        hideAction = null;
        temporaryVisible = false;
        persistentText = null;
        banner.setVisibility(View.GONE);
    }
}
