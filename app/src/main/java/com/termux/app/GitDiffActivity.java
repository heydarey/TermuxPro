package com.termux.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.termux.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 通过已认证的 OpenSSH 复用连接提供 Git 概览、分支切换、修改审查和提交记录。 */
public final class GitDiffActivity extends AppCompatActivity {

    private static final String EXTRA_HOST = "host";
    private static final String EXTRA_PORT = "port";
    private static final String EXTRA_PATH = "path";
    static final String EXTRA_START_IN_DIFF = "start_in_diff";
    static final String EXTRA_UI_TEST_OVERVIEW = "ui_test_overview";
    private static final int MAX_OUTPUT_BYTES = 1_500_000;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final RemoteCommandRunner mRunner = new RemoteCommandRunner();
    private TextView mContent;
    private TextView mStatusMessage;
    private ProgressBar mProgress;
    private View mContentScroll;
    private View mOverviewScroll;
    private View mStatusState;
    private View mReturnWorkspace;
    private GitRepositoryOverview mOverview;
    private Mode mMode = Mode.OVERVIEW;
    @Nullable private String mDisplayedCommitHash;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String host, int port,
                                   @NonNull String path) {
        return new Intent(context, GitDiffActivity.class)
            .putExtra(EXTRA_HOST, host)
            .putExtra(EXTRA_PORT, port)
            .putExtra(EXTRA_PATH, path);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_diff);
        mContent = findViewById(R.id.git_diff_content);
        mContentScroll = findViewById(R.id.git_diff_scroll);
        mOverviewScroll = findViewById(R.id.git_overview_scroll);
        mStatusState = findViewById(R.id.git_diff_status_state);
        mStatusMessage = findViewById(R.id.git_diff_status_message);
        mReturnWorkspace = findViewById(R.id.git_diff_return_workspace_button);
        mProgress = findViewById(R.id.git_diff_progress);
        findViewById(R.id.git_diff_back_button).setOnClickListener(view -> navigateBack());
        findViewById(R.id.git_diff_refresh_button).setOnClickListener(view -> refreshCurrentMode());
        findViewById(R.id.git_overview_branches_button).setOnClickListener(view -> showBranches());
        findViewById(R.id.git_overview_create_branch_button).setOnClickListener(
            view -> showCreateBranchDialog());
        findViewById(R.id.git_overview_delete_branch_button).setOnClickListener(
            view -> showDeleteBranchDialog());
        findViewById(R.id.git_overview_fetch_button).setOnClickListener(view -> fetchUpstream());
        findViewById(R.id.git_overview_pull_button).setOnClickListener(view -> confirmPullFastForward());
        findViewById(R.id.git_overview_push_button).setOnClickListener(view -> confirmPushUpstream());
        findViewById(R.id.git_overview_changes_button).setOnClickListener(view -> loadDiff());
        findViewById(R.id.git_overview_files_button).setOnClickListener(view -> showChangedFiles());
        findViewById(R.id.git_overview_stash_button).setOnClickListener(view -> showCreateStashDialog());
        findViewById(R.id.git_overview_stashes_button).setOnClickListener(view -> showStashes());
        findViewById(R.id.git_overview_stage_all_button).setOnClickListener(
            view -> confirmStageAll());
        findViewById(R.id.git_overview_unstage_all_button).setOnClickListener(
            view -> confirmUnstageAll());
        findViewById(R.id.git_overview_commit_button).setOnClickListener(
            view -> showCommitDialog());
        findViewById(R.id.git_overview_commits_button).setOnClickListener(view -> showCommits());
        mReturnWorkspace.setOnClickListener(view -> WorkspaceNavigation.returnToWorkspace(this));
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });
        String uiTestOverview = getIntent().getStringExtra(EXTRA_UI_TEST_OVERVIEW);
        if (uiTestOverview == null) {
            if (getIntent().getBooleanExtra(EXTRA_START_IN_DIFF, false)) loadDiff();
            else loadOverview();
        } else {
            String path = getIntent().getStringExtra(EXTRA_PATH);
            showOverviewForTesting(path == null ? "" : path, uiTestOverview);
        }
    }

    private void refreshCurrentMode() {
        if (mMode == Mode.DIFF) loadDiff();
        else if (mMode == Mode.COMMIT_DETAIL && mDisplayedCommitHash != null) {
            loadCommitDetails(mDisplayedCommitHash);
        }
        else loadOverview();
    }

    private void loadOverview() {
        ConnectionTarget target = readTarget();
        if (target == null) return;
        mMode = Mode.OVERVIEW;
        beginLoading(getString(R.string.git_workbench_loading));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitOverviewRemoteCommand(target.path), MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode != 0) {
                    showRemoteFailure(result, R.string.git_workbench_not_repository);
                    return;
                }
                if (result.truncated) {
                    showStatus(getString(R.string.git_workbench_overview_truncated), false);
                    return;
                }
                try {
                    mOverview = GitRepositoryOverview.parse(result.output);
                    showOverview(target, mOverview);
                } catch (IllegalArgumentException exception) {
                    showStatus(getString(R.string.git_workbench_invalid_response), true);
                }
            });
        });
    }

    private void loadDiff() {
        ConnectionTarget target = readTarget();
        if (target == null) return;

        mMode = Mode.DIFF;
        beginLoading(getString(R.string.git_diff_loading));
        mExecutor.execute(() -> showResultOnMain(runGitDiff(target.host, target.port, target.path)));
    }

    private void beginLoading(@NonNull String message) {
        mRunner.cancel();
        mProgress.setVisibility(View.VISIBLE);
        showStatus(message, false);
    }

    private ConnectionTarget readTarget() {
        String host = getIntent().getStringExtra(EXTRA_HOST);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        int port = getIntent().getIntExtra(EXTRA_PORT, 22);
        if (host == null || path == null || port < 1 || port > 65535) {
            showStatus(getString(R.string.git_diff_invalid_workspace), true);
            return null;
        }
        return new ConnectionTarget(host, port, path);
    }

    @NonNull
    private CommandResult runGitDiff(@NonNull String host, int port, @NonNull String path) {
        RemoteCommandRunner.Result result = mRunner.run(host, port,
            WorkspaceCommandBuilder.buildGitDiffRemoteCommand(path), MAX_OUTPUT_BYTES);
        if (result.exitCode == RemoteCommandRunner.ERROR_SSH_MISSING) {
            return new CommandResult(-1, getString(R.string.git_diff_ssh_missing), false, true);
        }
        if (result.exitCode == RemoteCommandRunner.ERROR_INTERRUPTED) {
            return new CommandResult(-1, getString(R.string.git_diff_cancelled), false, false);
        }
        if (result.exitCode == RemoteCommandRunner.ERROR_PROCESS) {
            return new CommandResult(-1, getString(R.string.git_diff_connection_error,
                result.errorType == null ? "Process" : result.errorType), false, true);
        }
        return new CommandResult(result.exitCode, result.output, result.truncated, result.exitCode != 0);
    }

    private void showResultOnMain(@NonNull CommandResult result) {
        mMainHandler.post(() -> {
            if (!isFinishing() && !isDestroyed()) showResult(result);
        });
    }

    private void showResult(@NonNull CommandResult result) {
        mProgress.setVisibility(View.GONE);
        String output = result.output;
        if (result.exitCode != 0) {
            showStatus(result.exitCode == -1 && !output.trim().isEmpty()
                ? output : getString(R.string.git_diff_failed), result.recoverable);
            return;
        } else if (output.trim().isEmpty()) {
            showStatus(getString(R.string.git_diff_clean), false);
            return;
        }
        if (result.truncated) output += "\n\n" + getString(R.string.git_diff_truncated);
        mStatusState.setVisibility(View.GONE);
        mOverviewScroll.setVisibility(View.GONE);
        mContentScroll.setVisibility(View.VISIBLE);
        mContent.setText(colorize(output));
    }

    private void showOverview(@NonNull ConnectionTarget target,
                              @NonNull GitRepositoryOverview overview) {
        mOverview = overview;
        mProgress.setVisibility(View.GONE);
        mStatusState.setVisibility(View.GONE);
        mContentScroll.setVisibility(View.GONE);
        mOverviewScroll.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.git_overview_head)).setText(getString(
            overview.detached ? R.string.git_workbench_detached : R.string.git_workbench_branch,
            overview.head));
        ((TextView) findViewById(R.id.git_overview_path)).setText(getString(
            R.string.git_workbench_target, target.host, target.port, target.path));
        ((TextView) findViewById(R.id.git_overview_changes)).setText(getResources().getQuantityString(
            R.plurals.git_workbench_changed_files, overview.changedFiles, overview.changedFiles));
        ((TextView) findViewById(R.id.git_overview_index_state)).setText(getString(
            R.string.git_workbench_index_state, overview.stagedFiles, overview.unstagedFiles));
        Button stageAll = findViewById(R.id.git_overview_stage_all_button);
        stageAll.setEnabled(overview.unstagedFiles > 0);
        stageAll.setAlpha(overview.unstagedFiles > 0 ? 1f : 0.48f);
        Button unstageAll = findViewById(R.id.git_overview_unstage_all_button);
        unstageAll.setEnabled(overview.stagedFiles > 0);
        unstageAll.setAlpha(overview.stagedFiles > 0 ? 1f : 0.48f);
        Button commit = findViewById(R.id.git_overview_commit_button);
        commit.setEnabled(overview.stagedFiles > 0);
        commit.setAlpha(overview.stagedFiles > 0 ? 1f : 0.48f);
        Button files = findViewById(R.id.git_overview_files_button);
        boolean hasFileChanges = !overview.fileChanges.isEmpty();
        files.setEnabled(hasFileChanges);
        files.setAlpha(hasFileChanges ? 1f : 0.48f);
        findViewById(R.id.git_overview_review_actions).setVisibility(
            hasFileChanges ? View.VISIBLE : View.GONE);
        Button stash = findViewById(R.id.git_overview_stash_button);
        stash.setEnabled(overview.changedFiles > 0);
        stash.setAlpha(overview.changedFiles > 0 ? 1f : 0.48f);
        Button stashes = findViewById(R.id.git_overview_stashes_button);
        stashes.setEnabled(!overview.stashes.isEmpty());
        stashes.setAlpha(!overview.stashes.isEmpty() ? 1f : 0.48f);
        Button deleteBranch = findViewById(R.id.git_overview_delete_branch_button);
        boolean canDeleteBranch = !deletableLocalBranches(overview).isEmpty();
        deleteBranch.setEnabled(canDeleteBranch);
        deleteBranch.setAlpha(canDeleteBranch ? 1f : 0.48f);
        TextView sync = findViewById(R.id.git_overview_sync);
        if (overview.ahead == null || overview.behind == null) {
            sync.setText(R.string.git_workbench_no_upstream);
        } else {
            sync.setText(getString(R.string.git_workbench_sync_with_upstream,
                overview.upstream == null ? getString(R.string.git_workbench_unknown_upstream)
                    : overview.upstream,
                overview.ahead, overview.behind));
        }
        ((TextView) findViewById(R.id.git_overview_next_step)).setText(
            nextStepGuidance(overview));
        bindPrimaryAction(overview);
        ((TextView) findViewById(R.id.git_overview_recent_commits)).setText(
            recentCommitsSummary(overview));
        Button fetch = findViewById(R.id.git_overview_fetch_button);
        fetch.setEnabled(overview.upstream != null);
        fetch.setAlpha(overview.upstream != null ? 1f : 0.48f);
        boolean canPull = overview.upstream != null && overview.behind != null && overview.behind > 0;
        Button pull = findViewById(R.id.git_overview_pull_button);
        pull.setEnabled(canPull);
        pull.setAlpha(canPull ? 1f : 0.48f);
        boolean canPush = overview.upstream != null && overview.ahead != null
            && overview.ahead > 0 && (overview.behind == null || overview.behind == 0)
            && !overview.detached;
        Button push = findViewById(R.id.git_overview_push_button);
        push.setEnabled(canPush);
        push.setAlpha(canPush ? 1f : 0.48f);
    }

    /** 将“下一步建议”落实为首屏主操作，避免手机上在横向按钮长条中寻找动作。 */
    private void bindPrimaryAction(@NonNull GitRepositoryOverview overview) {
        Button action = findViewById(R.id.git_overview_primary_action_button);
        action.setVisibility(View.VISIBLE);
        action.setEnabled(true);
        action.setAlpha(1f);
        if (overview.detached) {
            action.setText(R.string.git_workbench_primary_branch);
            action.setContentDescription(getString(R.string.git_workbench_primary_branch_description));
            action.setOnClickListener(view -> showBranches());
            return;
        }
        if (overview.changedFiles > 0) {
            if (overview.stagedFiles > 0 && overview.unstagedFiles == 0) {
                action.setText(R.string.git_workbench_primary_commit);
                action.setContentDescription(getString(R.string.git_workbench_primary_commit_description));
                action.setOnClickListener(view -> showCommitDialog());
                return;
            }
            if (!overview.fileChanges.isEmpty()) {
                action.setText(R.string.git_workbench_primary_review_files);
                action.setContentDescription(getString(
                    R.string.git_workbench_primary_review_files_description));
                action.setOnClickListener(view -> showChangedFiles());
                return;
            }
            action.setText(R.string.git_workbench_primary_review_diff);
            action.setContentDescription(getString(
                R.string.git_workbench_primary_review_diff_description));
            action.setOnClickListener(view -> loadDiff());
            return;
        }
        if (overview.upstream == null || overview.ahead == null || overview.behind == null) {
            action.setText(R.string.git_workbench_primary_create_branch);
            action.setContentDescription(getString(
                R.string.git_workbench_primary_create_branch_description));
            action.setOnClickListener(view -> showCreateBranchDialog());
            return;
        }
        if (overview.behind > 0) {
            action.setText(R.string.git_workbench_primary_pull);
            action.setContentDescription(getString(R.string.git_workbench_primary_pull_description));
            action.setOnClickListener(view -> confirmPullFastForward());
            return;
        }
        if (overview.ahead > 0) {
            action.setText(R.string.git_workbench_primary_push);
            action.setContentDescription(getString(R.string.git_workbench_primary_push_description));
            action.setOnClickListener(view -> confirmPushUpstream());
            return;
        }
        if (!overview.commits.isEmpty()) {
            action.setText(R.string.git_workbench_primary_commits);
            action.setContentDescription(getString(R.string.git_workbench_primary_commits_description));
            action.setOnClickListener(view -> showCommits());
            return;
        }
        action.setText(R.string.git_workbench_primary_create_branch);
        action.setContentDescription(getString(
            R.string.git_workbench_primary_create_branch_description));
        action.setOnClickListener(view -> showCreateBranchDialog());
    }

    @NonNull
    private String nextStepGuidance(@NonNull GitRepositoryOverview overview) {
        if (overview.detached) return getString(R.string.git_workbench_next_detached);
        if (overview.changedFiles > 0) {
            if (overview.stagedFiles > 0) {
                return getString(R.string.git_workbench_next_commit_or_review,
                    overview.stagedFiles, overview.unstagedFiles);
            }
            return getString(R.string.git_workbench_next_review_or_stash,
                overview.unstagedFiles);
        }
        if (overview.upstream == null || overview.ahead == null || overview.behind == null) {
            return getString(R.string.git_workbench_next_set_upstream_or_branch);
        }
        if (overview.behind > 0) {
            return getString(R.string.git_workbench_next_pull_ff, overview.upstream,
                overview.behind);
        }
        if (overview.ahead > 0) {
            return getString(R.string.git_workbench_next_push, overview.upstream,
                overview.ahead);
        }
        return getString(R.string.git_workbench_next_clean);
    }

    @NonNull
    private String recentCommitsSummary(@NonNull GitRepositoryOverview overview) {
        if (overview.commits.isEmpty()) return getString(R.string.git_workbench_no_commits);
        StringBuilder text = new StringBuilder();
        int limit = Math.min(3, overview.commits.size());
        for (int index = 0; index < limit; index++) {
            GitRepositoryOverview.Commit commit = overview.commits.get(index);
            if (text.length() > 0) text.append('\n');
            text.append(commit.shortHash).append(" · ").append(commit.relativeTime)
                .append(" · ").append(commit.subject);
        }
        if (overview.commits.size() > limit) {
            text.append('\n').append(getString(R.string.git_workbench_recent_commits_more,
                overview.commits.size() - limit));
        }
        return text.toString();
    }

    /** 模拟器截图只注入脱敏协议数据，仍走与真实 SSH 结果相同的解析和渲染路径。 */
    void showOverviewForTesting(@NonNull String path, @NonNull String protocolOutput) {
        showOverview(overviewTarget(path), GitRepositoryOverview.parse(protocolOutput));
    }

    @NonNull
    GitRepositoryOverview mOverviewForTesting() {
        return mOverview;
    }

    private void showCommits() {
        if (mOverview == null) return;
        if (mOverview.commits.isEmpty()) {
            showStatus(getString(R.string.git_workbench_no_commits), false);
            return;
        }
        AlertDialog dialog = createCommitsDialog();
        if (dialog != null) showStyledDialog(dialog);
    }

    @Nullable
    AlertDialog createCommitsDialog() {
        if (mOverview == null || mOverview.commits.isEmpty()) return null;
        View content = getLayoutInflater().inflate(R.layout.dialog_git_commits_filter, null);
        TextView message = content.findViewById(R.id.git_commit_history_message);
        message.setText(commitHistoryMessage());
        EditText filter = content.findViewById(R.id.git_commit_history_filter);
        ListView list = content.findViewById(R.id.git_commit_history_list);
        TextView empty = content.findViewById(R.id.git_commit_history_empty);
        list.setEmptyView(empty);

        ArrayList<GitRepositoryOverview.Commit> visibleCommits = new ArrayList<>(mOverview.commits);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_termuxpro_list,
            commitLabels(visibleCommits));
        list.setAdapter(adapter);
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                visibleCommits.clear();
                visibleCommits.addAll(filterCommits(mOverview.commits,
                    text == null ? "" : text.toString()));
                adapter.clear();
                adapter.addAll(commitLabels(visibleCommits));
                adapter.notifyDataSetChanged();
                empty.setText(getString(R.string.git_workbench_commit_filter_empty,
                    text == null ? "" : text.toString().trim()));
            }

            @Override
            public void afterTextChanged(Editable text) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_commits)
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        list.setOnItemClickListener((parent, view, position, id) -> {
            GitRepositoryOverview.Commit commit = visibleCommits.get(position);
            dialog.dismiss();
            loadCommitDetails(commit.shortHash);
        });
        return dialog;
    }

    @NonNull
    private List<String> commitLabels(@NonNull List<GitRepositoryOverview.Commit> commits) {
        List<String> labels = new ArrayList<>(commits.size());
        for (GitRepositoryOverview.Commit commit : commits) {
            labels.add(getString(R.string.git_workbench_commit_item, commit.shortHash,
                commit.relativeTime, commit.subject));
        }
        return labels;
    }

    @NonNull
    static List<GitRepositoryOverview.Commit> filterCommits(
        @NonNull List<GitRepositoryOverview.Commit> commits, @NonNull String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return new ArrayList<>(commits);
        List<GitRepositoryOverview.Commit> result = new ArrayList<>();
        for (GitRepositoryOverview.Commit commit : commits) {
            String haystack = (commit.shortHash + " " + commit.relativeTime + " " + commit.subject)
                .toLowerCase(Locale.ROOT);
            if (haystack.contains(normalized)) result.add(commit);
        }
        return result;
    }

    @NonNull
    private String commitHistoryMessage() {
        if (mOverview == null) return getString(R.string.git_workbench_commit_detail_hint);
        ConnectionTarget target = currentTargetWithoutSideEffects();
        String targetLabel = target == null ? getString(R.string.git_workbench_unknown_target)
            : getString(R.string.git_workbench_target, target.host, target.port, target.path);
        return getString(R.string.git_workbench_commit_detail_hint_with_context,
            mOverview.head, targetLabel);
    }

    @Nullable
    private ConnectionTarget currentTargetWithoutSideEffects() {
        String host = getIntent().getStringExtra(EXTRA_HOST);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        int port = getIntent().getIntExtra(EXTRA_PORT, 22);
        if (host == null || path == null || port < 1 || port > 65535) return null;
        return new ConnectionTarget(host, port, path);
    }

    private void loadCommitDetails(@NonNull String shortHash) {
        ConnectionTarget target = readTarget();
        if (target == null || !WorkspaceCommandBuilder.isSafeGitCommitHash(shortHash)) return;
        mMode = Mode.COMMIT_DETAIL;
        mDisplayedCommitHash = shortHash;
        beginLoading(getString(R.string.git_workbench_commit_detail_loading, shortHash));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitShowCommitRemoteCommand(target.path, shortHash),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> showCommitDetailsOnMain(result));
        });
    }

    private void showCommitDetailsOnMain(@NonNull RemoteCommandRunner.Result result) {
        if (isFinishing() || isDestroyed()) return;
        mProgress.setVisibility(View.GONE);
        if (result.exitCode != 0) {
            showRemoteFailure(result, R.string.git_workbench_commit_detail_failed);
            return;
        }
        if (result.output.trim().isEmpty()) {
            showStatus(getString(R.string.git_workbench_commit_detail_missing), false);
            return;
        }
        String output = result.output;
        if (result.truncated) output += "\n\n" + getString(R.string.git_diff_truncated);
        mStatusState.setVisibility(View.GONE);
        mOverviewScroll.setVisibility(View.GONE);
        mContentScroll.setVisibility(View.VISIBLE);
        mContent.setText(colorize(output));
    }

    private void showBranches() {
        AlertDialog dialog = createBranchesDialog();
        if (dialog != null) showStyledDialog(dialog);
    }

    private void showChangedFiles() {
        AlertDialog dialog = createChangedFilesDialog();
        if (dialog != null) showStyledDialog(dialog);
    }

    private void showStashes() {
        AlertDialog dialog = createStashesDialog();
        if (dialog != null) showStyledDialog(dialog);
    }

    @Nullable
    AlertDialog createStashesDialog() {
        if (mOverview == null) return null;
        if (mOverview.stashes.isEmpty()) {
            showStatus(getString(R.string.git_workbench_no_stashes), false);
            return null;
        }
        String[] labels = new String[mOverview.stashes.size()];
        for (int index = 0; index < mOverview.stashes.size(); index++) {
            labels[index] = stashLabel(mOverview.stashes.get(index));
        }
        return new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_stashes)
            .setAdapter(new ArrayAdapter<>(this, R.layout.item_termuxpro_list, labels),
                (selectionDialog, which) -> showStashActions(mOverview.stashes.get(which)))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    }

    @NonNull
    private String stashLabel(@NonNull GitRepositoryOverview.StashEntry stash) {
        return getString(R.string.git_workbench_stash_item, stash.ref, stash.relativeTime,
            stash.subject);
    }

    private void showStashActions(@NonNull GitRepositoryOverview.StashEntry stash) {
        String[] actions = {
            getString(R.string.git_workbench_stash_apply),
            getString(R.string.git_workbench_stash_drop)
        };
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(stash.ref)
            .setMessage(stash.subject)
            .setAdapter(new ArrayAdapter<>(this, R.layout.item_termuxpro_list, actions),
                (selectionDialog, which) -> {
                    if (which == 0) confirmApplyStash(stash);
                    else confirmDropStash(stash);
                })
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showStyledDialog(dialog);
    }

    @Nullable
    AlertDialog createChangedFilesDialog() {
        if (mOverview == null) return null;
        if (mOverview.fileChanges.isEmpty()) {
            showStatus(getString(R.string.git_workbench_no_file_changes), false);
            return null;
        }
        String[] labels = new String[mOverview.fileChanges.size()];
        for (int index = 0; index < mOverview.fileChanges.size(); index++) {
            labels[index] = fileChangeLabel(mOverview.fileChanges.get(index));
        }
        return new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_files)
            .setAdapter(new ArrayAdapter<>(this, R.layout.item_termuxpro_list, labels),
                (selectionDialog, which) -> showFileIndexActions(mOverview.fileChanges.get(which)))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    }

    @NonNull
    private String fileChangeLabel(@NonNull GitRepositoryOverview.FileChange change) {
        return getString(R.string.git_workbench_file_change,
            fileChangeScope(change), change.path);
    }

    @NonNull
    private String fileChangeScope(@NonNull GitRepositoryOverview.FileChange change) {
        if (change.hasStagedChange() && change.hasUnstagedChange()) {
            return getString(R.string.git_workbench_file_scope_mixed);
        }
        if (change.hasStagedChange()) return getString(R.string.git_workbench_file_scope_staged);
        return getString(R.string.git_workbench_file_scope_unstaged);
    }

    private void showFileIndexActions(@NonNull GitRepositoryOverview.FileChange change) {
        List<String> actions = new ArrayList<>();
        List<Boolean> stageActions = new ArrayList<>();
        if (change.hasUnstagedChange()) {
            actions.add(getString(R.string.git_workbench_stage_file_action));
            stageActions.add(true);
        }
        if (change.hasStagedChange()) {
            actions.add(getString(R.string.git_workbench_unstage_file_action));
            stageActions.add(false);
        }
        if (actions.isEmpty()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(change.path)
            .setMessage(R.string.git_workbench_file_action_message)
            .setAdapter(new ArrayAdapter<>(this, R.layout.item_termuxpro_list,
                actions.toArray(new String[0])), (selectionDialog, which) ->
                runFileIndexOperation(stageActions.get(which), change.path))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showStyledDialog(dialog);
    }

    @Nullable
    AlertDialog createBranchesDialog() {
        if (mOverview == null || (mOverview.localBranches.isEmpty()
            && mOverview.remoteBranches.isEmpty())) {
            showStatus(getString(R.string.git_workbench_no_local_branches), false);
            return null;
        }
        int localCount = mOverview.localBranches.size();
        String[] labels = new String[localCount + mOverview.remoteBranches.size()];
        for (int index = 0; index < localCount; index++) {
            String branch = mOverview.localBranches.get(index);
            labels[index] = branch.equals(mOverview.head)
                ? getString(R.string.git_workbench_current_branch, branch)
                : getString(R.string.git_workbench_local_branch, branch);
        }
        for (int index = 0; index < mOverview.remoteBranches.size(); index++) {
            labels[localCount + index] = getString(R.string.git_workbench_remote_branch,
                mOverview.remoteBranches.get(index));
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_switch_branch)
            .setAdapter(new ArrayAdapter<>(this, R.layout.item_termuxpro_list, labels),
                (selectionDialog, which) -> {
                if (which < localCount) confirmSwitch(mOverview.localBranches.get(which));
                else confirmTrackRemoteBranch(mOverview.remoteBranches.get(which - localCount));
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        return dialog;
    }

    private void showDeleteBranchDialog() {
        AlertDialog dialog = createDeleteBranchDialog();
        if (dialog != null) showStyledDialog(dialog);
    }

    @Nullable
    AlertDialog createDeleteBranchDialog() {
        if (mOverview == null) return null;
        List<String> branches = deletableLocalBranches(mOverview);
        if (branches.isEmpty()) {
            showStatus(getString(R.string.git_workbench_no_deletable_local_branches), false);
            return null;
        }
        String[] labels = new String[branches.size()];
        for (int index = 0; index < branches.size(); index++) {
            labels[index] = getString(R.string.git_workbench_local_branch, branches.get(index));
        }
        return new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_delete_branch)
            .setAdapter(new ArrayAdapter<>(this, R.layout.item_termuxpro_list, labels),
                (selectionDialog, which) -> confirmDeleteLocalBranch(branches.get(which)))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    }

    @NonNull
    private static List<String> deletableLocalBranches(@NonNull GitRepositoryOverview overview) {
        List<String> branches = new ArrayList<>();
        for (String branch : overview.localBranches) {
            if (!overview.detached && branch.equals(overview.head)) continue;
            if (!WorkspaceCommandBuilder.isSafeGitBranchName(branch)) continue;
            branches.add(branch);
        }
        return branches;
    }

    void confirmDeleteLocalBranch(@NonNull String branch) {
        if (mOverview == null || !mOverview.localBranches.contains(branch)
            || (!mOverview.detached && branch.equals(mOverview.head))
            || !WorkspaceCommandBuilder.isSafeGitBranchName(branch)) {
            showStatus(getString(R.string.git_workbench_delete_branch_current_blocked), false);
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_delete_branch)
            .setMessage(getString(R.string.git_workbench_delete_branch_message, branch))
            .setPositiveButton(R.string.git_workbench_delete_branch_action,
                (selectionDialog, which) -> deleteLocalBranch(branch))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showDangerDialog(dialog);
    }

    private void showDangerDialog(@NonNull AlertDialog dialog) {
        TermuxProDialogStyle.show(this, dialog, shownDialog -> {
            if (shownDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                shownDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(this, R.color.tp_danger));
            }
        });
    }

    private void confirmTrackRemoteBranch(@NonNull String branch) {
        if (mOverview != null && mOverview.changedFiles > 0) {
            showStatus(getString(R.string.git_workbench_switch_dirty_blocked,
                mOverview.changedFiles), false);
            return;
        }
        AlertDialog dialog = createTrackRemoteBranchDialog(branch);
        if (dialog != null) showStyledDialog(dialog);
    }

    @Nullable
    AlertDialog createTrackRemoteBranchDialog(@NonNull String branch) {
        if (mOverview == null || GitRepositoryOverview.isRemoteHead(branch)) return null;
        int message = mOverview.changedFiles > 0
            ? R.string.git_workbench_track_remote_dirty_message
            : R.string.git_workbench_track_remote_message;
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(branch)
            .setMessage(getString(message, mOverview.head, branch, mOverview.changedFiles))
            .setPositiveButton(R.string.git_workbench_track_remote_action,
                (selectionDialog, which) -> trackRemoteBranch(branch))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        return dialog;
    }

    private void confirmSwitch(@NonNull String branch) {
        if (mOverview == null || branch.equals(mOverview.head)) return;
        if (mOverview.changedFiles > 0) {
            showStatus(getString(R.string.git_workbench_switch_dirty_blocked,
                mOverview.changedFiles), false);
            return;
        }
        int message = mOverview.changedFiles > 0
            ? R.string.git_workbench_switch_dirty_message : R.string.git_workbench_switch_message;
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_switch_branch)
            .setMessage(getString(message, mOverview.head, branch, mOverview.changedFiles))
            .setPositiveButton(R.string.git_workbench_switch_action,
                (selectionDialog, which) -> switchBranch(branch))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showStyledDialog(dialog);
    }

    private void confirmStageAll() {
        if (mOverview == null || mOverview.unstagedFiles <= 0) {
            showStatus(getString(R.string.git_workbench_no_unstaged_changes), false);
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_stage_all)
            .setMessage(getString(R.string.git_workbench_stage_all_message,
                mOverview.unstagedFiles))
            .setPositiveButton(R.string.git_workbench_stage_all_action,
                (selectionDialog, which) -> runIndexOperation(true))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showStyledDialog(dialog);
    }

    private void confirmUnstageAll() {
        if (mOverview == null || mOverview.stagedFiles <= 0) {
            showStatus(getString(R.string.git_workbench_no_staged_changes), false);
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_unstage_all)
            .setMessage(getString(R.string.git_workbench_unstage_all_message,
                mOverview.stagedFiles))
            .setPositiveButton(R.string.git_workbench_unstage_all_action,
                (selectionDialog, which) -> runIndexOperation(false))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showStyledDialog(dialog);
    }

    private void showCommitDialog() {
        showPreparedDialog(createCommitDialog());
    }

    private void showCreateStashDialog() {
        showPreparedDialog(createStashDialog());
    }

    @Nullable
    AlertDialog createStashDialog() {
        if (mOverview == null) return null;
        if (mOverview.changedFiles <= 0) {
            showStatus(getString(R.string.git_workbench_no_stash_changes), false);
            return null;
        }
        EditText input = new EditText(this);
        input.setId(android.R.id.edit);
        input.setSingleLine(true);
        input.setHint(R.string.git_workbench_stash_hint);
        input.setTextColor(ContextCompat.getColor(this, R.color.tp_text_primary));
        input.setHintTextColor(ContextCompat.getColor(this, R.color.tp_text_secondary));
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding / 2, padding, padding / 2);
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_stash_save)
            .setMessage(getString(R.string.git_workbench_stash_message,
                mOverview.changedFiles, mOverview.stagedFiles, mOverview.unstagedFiles))
            .setView(input)
            .setPositiveButton(R.string.git_workbench_stash_save_action, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        TermuxProDialogStyle.prepare(this, dialog, shownDialog -> {
            shownDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String message = input.getText().toString();
                if (!WorkspaceCommandBuilder.isSafeGitCommitMessage(message)) {
                    input.setError(getString(R.string.git_workbench_stash_invalid));
                    return;
                }
                shownDialog.dismiss();
                stashChanges(message.trim());
            });
        });
        return dialog;
    }

    @Nullable
    AlertDialog createCommitDialog() {
        if (mOverview == null) return null;
        ConnectionTarget target = readTarget();
        if (target == null) return null;
        if (mOverview.stagedFiles <= 0) {
            showStatus(getString(R.string.git_workbench_no_staged_changes), false);
            return null;
        }
        EditText input = new EditText(this);
        input.setId(android.R.id.edit);
        input.setSingleLine(true);
        input.setHint(R.string.git_workbench_commit_hint);
        input.setTextColor(ContextCompat.getColor(this, R.color.tp_text_primary));
        input.setHintTextColor(ContextCompat.getColor(this, R.color.tp_text_secondary));
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding / 2, padding, padding / 2);
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_commit)
            .setMessage(getString(R.string.git_workbench_commit_message, mOverview.stagedFiles,
                mOverview.unstagedFiles, mOverview.head, target.host, target.port, target.path))
            .setView(input)
            .setPositiveButton(R.string.git_workbench_commit_action, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        TermuxProDialogStyle.prepare(this, dialog, shownDialog -> {
            shownDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String message = input.getText().toString();
                if (!WorkspaceCommandBuilder.isSafeGitCommitMessage(message)) {
                    input.setError(getString(R.string.git_workbench_commit_invalid));
                    return;
                }
                shownDialog.dismiss();
                commitStaged(message.trim());
            });
        });
        return dialog;
    }

    void showStyledDialog(@NonNull AlertDialog dialog) {
        TermuxProDialogStyle.show(this, dialog);
    }

    private void showCreateBranchDialog() {
        showPreparedDialog(createNewBranchDialog());
    }

    private void showPreparedDialog(@Nullable AlertDialog dialog) {
        // createXxxDialog() 已通过 TermuxProDialogStyle.prepare 绑定产品样式与输入校验。
        if (dialog != null) dialog.show();
    }

    @Nullable
    AlertDialog createNewBranchDialog() {
        if (mOverview == null) return null;
        EditText input = new EditText(this);
        input.setId(android.R.id.edit);
        input.setSingleLine(true);
        input.setHint(R.string.git_workbench_create_branch_hint);
        input.setTextColor(ContextCompat.getColor(this, R.color.tp_text_primary));
        input.setHintTextColor(ContextCompat.getColor(this, R.color.tp_text_secondary));
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding / 2, padding, padding / 2);
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_create_branch)
            .setMessage(getString(mOverview.changedFiles > 0
                ? R.string.git_workbench_create_branch_dirty_message
                : R.string.git_workbench_create_branch_message, mOverview.head,
                mOverview.changedFiles))
            .setView(input)
            .setPositiveButton(R.string.git_workbench_create_branch_action, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        TermuxProDialogStyle.prepare(this, dialog, shownDialog -> {
            shownDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String branch = input.getText().toString().trim();
                if (!WorkspaceCommandBuilder.isSafeGitBranchName(branch)) {
                    input.setError(getString(R.string.git_workbench_create_branch_invalid));
                    return;
                }
                shownDialog.dismiss();
                createBranch(branch);
            });
        });
        return dialog;
    }

    private void switchBranch(@NonNull String branch) {
        if (mOverview == null || !mOverview.localBranches.contains(branch)) return;
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_switching, branch));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitSwitchBranchRemoteCommand(target.path, branch),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 77) showStatus(getString(
                    R.string.git_workbench_switch_dirty_blocked_remote), false);
                else showStatus(getString(R.string.git_workbench_switch_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void createBranch(@NonNull String branch) {
        if (mOverview == null || !WorkspaceCommandBuilder.isSafeGitBranchName(branch)) return;
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_creating_branch, branch));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitCreateBranchRemoteCommand(target.path, branch),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 74) showStatus(getString(
                    R.string.git_workbench_create_branch_conflict, branch), false);
                else showStatus(getString(R.string.git_workbench_create_branch_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void deleteLocalBranch(@NonNull String branch) {
        if (mOverview == null || !mOverview.localBranches.contains(branch)
            || (!mOverview.detached && branch.equals(mOverview.head))
            || !WorkspaceCommandBuilder.isSafeGitBranchName(branch)) {
            return;
        }
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_deleting_branch, branch));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitDeleteLocalBranchRemoteCommand(target.path, branch),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 79) showStatus(
                    getString(R.string.git_workbench_delete_branch_current_blocked), false);
                else if (result.exitCode == 80) showStatus(getString(
                    R.string.git_workbench_delete_branch_missing, branch), false);
                else showStatus(getString(R.string.git_workbench_delete_branch_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void trackRemoteBranch(@NonNull String branch) {
        if (mOverview == null || !mOverview.remoteBranches.contains(branch)
            || GitRepositoryOverview.isRemoteHead(branch)) {
            return;
        }
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_switching, branch));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitTrackRemoteBranchCommand(target.path, branch),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 74) showStatus(getString(
                    R.string.git_workbench_track_remote_conflict, branch), false);
                else if (result.exitCode == 77) showStatus(getString(
                    R.string.git_workbench_switch_dirty_blocked_remote), false);
                else showStatus(getString(R.string.git_workbench_switch_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void runIndexOperation(boolean stage) {
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(stage
            ? R.string.git_workbench_staging_all
            : R.string.git_workbench_unstaging_all));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                stage
                    ? WorkspaceCommandBuilder.buildGitStageAllRemoteCommand(target.path)
                    : WorkspaceCommandBuilder.buildGitUnstageAllRemoteCommand(target.path),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 75) showStatus(getString(stage
                    ? R.string.git_workbench_no_unstaged_changes
                    : R.string.git_workbench_no_staged_changes), false);
                else showStatus(getString(stage
                    ? R.string.git_workbench_stage_all_failed
                    : R.string.git_workbench_unstage_all_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void runFileIndexOperation(boolean stage, @NonNull String filePath) {
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(stage
            ? R.string.git_workbench_staging_file
            : R.string.git_workbench_unstaging_file, filePath));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                stage
                    ? WorkspaceCommandBuilder.buildGitStageFileRemoteCommand(target.path, filePath)
                    : WorkspaceCommandBuilder.buildGitUnstageFileRemoteCommand(target.path, filePath),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 75) showStatus(getString(stage
                    ? R.string.git_workbench_no_unstaged_file_change
                    : R.string.git_workbench_no_staged_file_change), false);
                else showStatus(getString(stage
                    ? R.string.git_workbench_stage_file_failed
                    : R.string.git_workbench_unstage_file_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void commitStaged(@NonNull String message) {
        if (mOverview == null || mOverview.stagedFiles <= 0
            || !WorkspaceCommandBuilder.isSafeGitCommitMessage(message)) {
            return;
        }
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_committing));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitCommitStagedRemoteCommand(target.path, message),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 75) showStatus(
                    getString(R.string.git_workbench_no_staged_changes), false);
                else showStatus(getString(R.string.git_workbench_commit_failed,
                    result.output.trim()), false);
            });
        });
    }

    void confirmApplyStash(@NonNull GitRepositoryOverview.StashEntry stash) {
        if (mOverview == null || !mOverview.stashes.contains(stash)
            || !WorkspaceCommandBuilder.isSafeGitStashRef(stash.ref)) {
            return;
        }
        if (mOverview.changedFiles > 0) {
            showStatus(getString(R.string.git_workbench_stash_apply_dirty_blocked,
                mOverview.changedFiles), false);
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_stash_apply)
            .setMessage(getString(R.string.git_workbench_stash_apply_message,
                stash.ref, stash.subject))
            .setPositiveButton(R.string.git_workbench_stash_apply_action,
                (selectionDialog, which) -> applyStash(stash.ref))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showStyledDialog(dialog);
    }

    void confirmDropStash(@NonNull GitRepositoryOverview.StashEntry stash) {
        if (mOverview == null || !mOverview.stashes.contains(stash)
            || !WorkspaceCommandBuilder.isSafeGitStashRef(stash.ref)) {
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_stash_drop)
            .setMessage(getString(R.string.git_workbench_stash_drop_message,
                stash.ref, stash.subject))
            .setPositiveButton(R.string.git_workbench_stash_drop_action,
                (selectionDialog, which) -> dropStash(stash.ref))
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showDangerDialog(dialog);
    }

    private void stashChanges(@NonNull String message) {
        if (mOverview == null || mOverview.changedFiles <= 0
            || !WorkspaceCommandBuilder.isSafeGitCommitMessage(message)) {
            return;
        }
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_stashing));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitStashPushRemoteCommand(target.path, message),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 75) showStatus(
                    getString(R.string.git_workbench_no_stash_changes), false);
                else showStatus(getString(R.string.git_workbench_stash_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void applyStash(@NonNull String stashRef) {
        if (mOverview == null || mOverview.changedFiles > 0
            || !WorkspaceCommandBuilder.isSafeGitStashRef(stashRef)) {
            return;
        }
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_stash_applying, stashRef));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitStashApplyRemoteCommand(target.path, stashRef),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 77) showStatus(getString(
                    R.string.git_workbench_stash_apply_dirty_remote_blocked), false);
                else if (result.exitCode == 81) showStatus(getString(
                    R.string.git_workbench_stash_missing, stashRef), false);
                else showStatus(getString(R.string.git_workbench_stash_apply_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void dropStash(@NonNull String stashRef) {
        if (!WorkspaceCommandBuilder.isSafeGitStashRef(stashRef)) return;
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_stash_dropping, stashRef));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitStashDropRemoteCommand(target.path, stashRef),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 81) showStatus(getString(
                    R.string.git_workbench_stash_missing, stashRef), false);
                else showStatus(getString(R.string.git_workbench_stash_drop_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void fetchUpstream() {
        if (mOverview == null || mOverview.upstream == null) {
            showStatus(getString(R.string.git_workbench_no_upstream), false);
            return;
        }
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_fetching, mOverview.upstream));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitFetchUpstreamRemoteCommand(target.path),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 76) showStatus(
                    getString(R.string.git_workbench_no_upstream), false);
                else showStatus(getString(R.string.git_workbench_fetch_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void confirmPullFastForward() {
        if (mOverview == null || mOverview.upstream == null) {
            showStatus(getString(R.string.git_workbench_no_upstream), false);
            return;
        }
        if (mOverview.behind == null || mOverview.behind <= 0) {
            showStatus(getString(R.string.git_workbench_pull_not_needed), false);
            return;
        }
        if (mOverview.changedFiles > 0) {
            showStatus(getString(R.string.git_workbench_pull_dirty_blocked,
                mOverview.changedFiles), false);
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_pull)
            .setMessage(getString(R.string.git_workbench_pull_message,
                mOverview.behind, mOverview.upstream))
            .setPositiveButton(R.string.git_workbench_pull_action,
                (selectionDialog, which) -> pullFastForward())
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showStyledDialog(dialog);
    }

    private void pullFastForward() {
        if (mOverview == null || mOverview.upstream == null) return;
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_pulling, mOverview.upstream));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitPullFastForwardRemoteCommand(target.path),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 76) showStatus(
                    getString(R.string.git_workbench_no_upstream), false);
                else if (result.exitCode == 77) showStatus(
                    getString(R.string.git_workbench_pull_dirty_remote_blocked), false);
                else showStatus(getString(R.string.git_workbench_pull_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void confirmPushUpstream() {
        if (mOverview == null || mOverview.upstream == null) {
            showStatus(getString(R.string.git_workbench_no_upstream), false);
            return;
        }
        if (mOverview.detached) {
            showStatus(getString(R.string.git_workbench_push_detached_blocked), false);
            return;
        }
        if (mOverview.ahead == null || mOverview.ahead <= 0) {
            showStatus(getString(R.string.git_workbench_push_not_needed), false);
            return;
        }
        if (mOverview.behind != null && mOverview.behind > 0) {
            showStatus(getString(R.string.git_workbench_push_behind_blocked,
                mOverview.behind), false);
            return;
        }
        int message = mOverview.changedFiles > 0
            ? R.string.git_workbench_push_dirty_message
            : R.string.git_workbench_push_message;
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.git_workbench_push)
            .setMessage(getString(message, mOverview.ahead, mOverview.upstream,
                mOverview.changedFiles))
            .setPositiveButton(R.string.git_workbench_push_action,
                (selectionDialog, which) -> pushUpstream())
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        showStyledDialog(dialog);
    }

    private void pushUpstream() {
        if (mOverview == null || mOverview.upstream == null) return;
        ConnectionTarget target = readTarget();
        if (target == null) return;
        beginLoading(getString(R.string.git_workbench_pushing, mOverview.upstream));
        mExecutor.execute(() -> {
            RemoteCommandRunner.Result result = mRunner.run(target.host, target.port,
                WorkspaceCommandBuilder.buildGitPushUpstreamRemoteCommand(target.path),
                MAX_OUTPUT_BYTES);
            mMainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (result.exitCode == 0) loadOverview();
                else if (result.exitCode == 76) showStatus(
                    getString(R.string.git_workbench_no_upstream), false);
                else if (result.exitCode == 78) showStatus(
                    getString(R.string.git_workbench_push_detached_blocked), false);
                else showStatus(getString(R.string.git_workbench_push_failed,
                    result.output.trim()), false);
            });
        });
    }

    private void showRemoteFailure(@NonNull RemoteCommandRunner.Result result, int commandFailure) {
        mProgress.setVisibility(View.GONE);
        if (result.exitCode == RemoteCommandRunner.ERROR_SSH_MISSING) {
            showStatus(getString(R.string.git_diff_ssh_missing), true);
        } else if (result.exitCode == RemoteCommandRunner.ERROR_PROCESS) {
            showStatus(getString(R.string.git_diff_connection_error,
                result.errorType == null ? "Process" : result.errorType), true);
        } else if (result.exitCode == RemoteCommandRunner.ERROR_INTERRUPTED) {
            showStatus(getString(R.string.git_diff_cancelled), false);
        } else {
            showStatus(getString(commandFailure), false);
        }
    }

    private void navigateBack() {
        if (mMode != Mode.OVERVIEW && mOverview != null) {
            mMode = Mode.OVERVIEW;
            String path = getIntent().getStringExtra(EXTRA_PATH);
            showOverview(overviewTarget(path == null ? "" : path), mOverview);
        } else {
            finish();
        }
    }

    @NonNull
    private ConnectionTarget overviewTarget(@NonNull String fallbackPath) {
        String host = getIntent().getStringExtra(EXTRA_HOST);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        int port = getIntent().getIntExtra(EXTRA_PORT, 22);
        return new ConnectionTarget(host == null ? "" : host, port,
            path == null ? fallbackPath : path);
    }

    private void showStatus(@NonNull String message, boolean recoverable) {
        mContentScroll.setVisibility(View.GONE);
        mOverviewScroll.setVisibility(View.GONE);
        mStatusState.setVisibility(View.VISIBLE);
        mStatusMessage.setText(message);
        mReturnWorkspace.setVisibility(recoverable ? View.VISIBLE : View.GONE);
    }

    @NonNull
    private SpannableStringBuilder colorize(@NonNull String output) {
        SpannableStringBuilder styled = new SpannableStringBuilder();
        String[] lines = output.split("\n", -1);
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            int start = styled.length();
            styled.append(line);
            if (index < lines.length - 1) styled.append('\n');
            int color;
            switch (DiffLineClassifier.classify(line)) {
                case HEADER: color = Color.rgb(125, 183, 255); break;
                case HUNK: color = Color.rgb(208, 167, 255); break;
                case ADDITION: color = Color.rgb(112, 225, 161); break;
                case DELETION: color = Color.rgb(255, 138, 128); break;
                default: color = Color.rgb(216, 228, 236); break;
            }
            styled.setSpan(new ForegroundColorSpan(color), start, styled.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return styled;
    }

    @Override
    protected void onDestroy() {
        mRunner.cancel();
        mExecutor.shutdownNow();
        super.onDestroy();
    }

    private static final class CommandResult {
        final int exitCode;
        final String output;
        final boolean truncated;
        final boolean recoverable;

        CommandResult(int exitCode, @NonNull String output, boolean truncated, boolean recoverable) {
            this.exitCode = exitCode;
            this.output = output;
            this.truncated = truncated;
            this.recoverable = recoverable;
        }
    }

    private enum Mode { OVERVIEW, DIFF, COMMIT_DETAIL }

    private static final class ConnectionTarget {
        @NonNull final String host;
        final int port;
        @NonNull final String path;

        ConnectionTarget(@NonNull String host, int port, @NonNull String path) {
            this.host = host;
            this.port = port;
            this.path = path;
        }
    }
}
