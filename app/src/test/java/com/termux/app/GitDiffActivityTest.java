package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.termux.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public final class GitDiffActivityTest {

    @Test
    public void branchDialogsUseReadableTermuxProStyleAndOfferRemoteTracking() throws Exception {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t2\t1\t1\t\t\t0\n"
                + "TP_LOCAL\tdev\n"
                + "TP_LOCAL\tmobile-ui\n"
                + "TP_REMOTE\torigin/feature/mobile\n"
                + "TP_REMOTE\torigin/HEAD\n");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();
        activity.showOverviewForTesting("~/repo", intent.getStringExtra(
            GitDiffActivity.EXTRA_UI_TEST_OVERVIEW));

        AlertDialog branches = activity.createBranchesDialog();
        assertNotNull(branches);
        activity.showStyledDialog(branches);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(activity.getColor(R.color.tp_text_secondary),
            branches.getButton(AlertDialog.BUTTON_NEGATIVE).getCurrentTextColor());
        assertEquals(3, branches.getListView().getAdapter().getCount());
        assertTrue(((TextView) branches.getListView().getAdapter().getView(2, null,
            branches.getListView())).getText().toString().contains("origin/feature/mobile"));

        AlertDialog deleteBranches = activity.createDeleteBranchDialog();
        assertNotNull(deleteBranches);
        activity.showStyledDialog(deleteBranches);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(1, deleteBranches.getListView().getAdapter().getCount());
        assertTrue(((TextView) deleteBranches.getListView().getAdapter().getView(0, null,
            deleteBranches.getListView())).getText().toString().contains("mobile-ui"));
        assertTrue(!((TextView) deleteBranches.getListView().getAdapter().getView(0, null,
            deleteBranches.getListView())).getText().toString().contains("dev"));

        AlertDialog remote = activity.createTrackRemoteBranchDialog("origin/feature/mobile");
        assertNotNull(remote);
        activity.showStyledDialog(remote);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(activity.getString(R.string.git_workbench_track_remote_action),
            remote.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        assertEquals(activity.getColor(R.color.tp_primary),
            remote.getButton(AlertDialog.BUTTON_POSITIVE).getCurrentTextColor());
        assertEquals(activity.getColor(R.color.tp_text_secondary),
            remote.getButton(AlertDialog.BUTTON_NEGATIVE).getCurrentTextColor());
        assertTrue(((TextView) remote.findViewById(android.R.id.message)).getText().toString()
            .contains("2 个未提交文件"));
        branches.getListView().performItemClick(branches.getListView().getAdapter()
                .getView(1, null, branches.getListView()), 1,
            branches.getListView().getAdapter().getItemId(1));
        assertTrue(((TextView) activity.findViewById(R.id.git_diff_status_message)).getText()
            .toString().contains("已阻止切换分支"));

        activity.confirmDeleteLocalBranch("mobile-ui");
        AlertDialog confirmDelete = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(confirmDelete);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(activity.getString(R.string.git_workbench_delete_branch_action),
            confirmDelete.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        assertEquals(activity.getColor(R.color.tp_danger),
            confirmDelete.getButton(AlertDialog.BUTTON_POSITIVE).getCurrentTextColor());
        assertTrue(((TextView) confirmDelete.findViewById(android.R.id.message)).getText()
            .toString().contains("不会删除远端分支"));
    }

    @Test
    public void newBranchDialogKeepsReadableStyleAndValidatesInput() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t2\t0\t2\t\t\t0\n"
                + "TP_LOCAL\tdev\n");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();

        AlertDialog dialog = activity.createNewBranchDialog();
        assertNotNull(dialog);
        dialog.show();
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(activity.getString(R.string.git_workbench_create_branch_action),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        assertEquals(activity.getColor(R.color.tp_primary),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).getCurrentTextColor());
        assertTrue(((TextView) dialog.findViewById(android.R.id.message)).getText().toString()
            .contains("2 个未提交文件"));

        EditText input = dialog.findViewById(android.R.id.edit);
        assertNotNull(input);
        input.setText("bad branch");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertNotNull(input.getError());
        assertTrue(dialog.isShowing());
        dialog.dismiss();
    }

    @Test
    public void overviewShowsIndexSplitAndDisablesUnavailableIndexActions() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t3\t2\t1\t4\t1\t1\torigin/dev\n"
                + "TP_LOCAL\tdev\n");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();

        TextView index = activity.findViewById(R.id.git_overview_index_state);
        TextView sync = activity.findViewById(R.id.git_overview_sync);
        assertEquals("hdr@192.168.1.153:22 · ~/repo",
            ((TextView) activity.findViewById(R.id.git_overview_path)).getText().toString());
        assertTrue(index.getText().toString().contains("已暂存 2"));
        assertTrue(index.getText().toString().contains("未暂存 1"));
        assertTrue(sync.getText().toString().contains("跟踪 origin/dev"));
        assertTrue(sync.getText().toString().contains("领先 4"));
        assertTrue(sync.getText().toString().contains("落后 1"));
        assertTrue(((Button) activity.findViewById(R.id.git_overview_fetch_button)).isEnabled());
        assertTrue(((Button) activity.findViewById(R.id.git_overview_pull_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_push_button)).isEnabled());
        assertTrue(((Button) activity.findViewById(R.id.git_overview_stage_all_button)).isEnabled());
        assertTrue(((Button) activity.findViewById(R.id.git_overview_unstage_all_button)).isEnabled());
        assertTrue(((Button) activity.findViewById(R.id.git_overview_commit_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_files_button)).isEnabled());
        assertEquals(View.GONE, activity.findViewById(R.id.git_overview_review_actions)
            .getVisibility());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_delete_branch_button)).isEnabled());
        assertTrue(((Button) activity.findViewById(R.id.git_overview_stash_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_stashes_button)).isEnabled());

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t2\t0\t1\torigin/dev\n"
            + "TP_LOCAL\tdev\n"
            + "TP_LOCAL\tmobile-ui\n"
            + "TP_STASH\tstash@{0}\t1 hour ago\tWIP mobile\n");
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_pull_button)).isEnabled());
        assertTrue(((Button) activity.findViewById(R.id.git_overview_push_button)).isEnabled());
        assertTrue(((Button) activity.findViewById(R.id.git_overview_delete_branch_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_stash_button)).isEnabled());
        assertTrue(((Button) activity.findViewById(R.id.git_overview_stashes_button)).isEnabled());

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t\t\t0\n");
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_fetch_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_pull_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_push_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_stage_all_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_unstage_all_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_commit_button)).isEnabled());
        assertTrue(!((Button) activity.findViewById(R.id.git_overview_files_button)).isEnabled());
        assertEquals(View.GONE, activity.findViewById(R.id.git_overview_review_actions)
            .getVisibility());
    }

    @Test
    public void overviewShowsRecentCommitsSummaryWithoutOpeningSecondaryScreen() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t0\t0\t0\t\t\t0\n"
                + "TP_LOCAL\tdev\n"
                + "TP_LOG\tabc1234\t2 minutes ago\tfix: 修复滚动\n"
                + "TP_LOG\tdef5678\t1 hour ago\tfeat: 增加 Git 工作台\n"
                + "TP_LOG\t987abcd\tyesterday\tdocs: 更新说明\n"
                + "TP_LOG\t5555555\tlast week\tchore: 清理\n");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();

        String summary = ((TextView) activity.findViewById(R.id.git_overview_recent_commits))
            .getText().toString();
        assertTrue(summary.contains("abc1234 · 2 minutes ago · fix: 修复滚动"));
        assertTrue(summary.contains("def5678 · 1 hour ago · feat: 增加 Git 工作台"));
        assertTrue(summary.contains("987abcd · yesterday · docs: 更新说明"));
        assertTrue(summary.contains("还有 1 条"));

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tmain\t0\t0\t0\t0\t\t\t0\n");
        assertEquals("当前仓库还没有提交记录。",
            ((TextView) activity.findViewById(R.id.git_overview_recent_commits)).getText()
                .toString());
    }

    @Test
    public void overviewToolboxGroupsGitActionsForMobileInsteadOfHorizontalScrolling() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t3\t1\t2\t1\t0\t1\torigin/dev\n"
                + "TP_LOCAL\tdev\n"
                + "TP_LOCAL\tmobile-ui\n"
                + "TP_LOG\tabc1234\t2 minutes ago\tfix: 修复滚动\n"
                + "TP_STASH\tstash@{0}\t1 hour ago\tWIP mobile\n");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();

        assertEquals("Git 工具箱", ((TextView) activity.findViewById(
            R.id.git_overview_toolbox_title)).getText().toString());
        assertEquals("分支管理", ((TextView) activity.findViewById(
            R.id.git_workbench_branch_tools_title)).getText().toString());
        assertEquals("远端同步", ((TextView) activity.findViewById(
            R.id.git_workbench_sync_tools_title)).getText().toString());
        assertEquals("改动处理", ((TextView) activity.findViewById(
            R.id.git_workbench_change_tools_title)).getText().toString());
        assertEquals("历史与临时保存", ((TextView) activity.findViewById(
            R.id.git_workbench_history_tools_title)).getText().toString());

        Button branches = activity.findViewById(R.id.git_overview_branches_button);
        Button createBranch = activity.findViewById(R.id.git_overview_create_branch_button);
        Button deleteBranch = activity.findViewById(R.id.git_overview_delete_branch_button);
        Button commits = activity.findViewById(R.id.git_overview_commits_button);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, branches.getLayoutParams().width);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, createBranch.getLayoutParams().width);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, deleteBranch.getLayoutParams().width);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, commits.getLayoutParams().width);
        assertTrue(((TextView) activity.findViewById(R.id.git_overview_toolbox_title))
            .getContentDescription() == null);
    }

    @Test
    public void commitHistoryUsesReadableSelectionAndExplainsReadOnlyDetails() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t0\t0\t0\t\t\t0\n"
                + "TP_LOG\tabc1234\t2 minutes ago\tfix: 修复滚动\n"
                + "TP_LOG\tdef5678\t1 hour ago\tfeat: 增加 Git 工作台\n");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();

        AlertDialog commits = activity.createCommitsDialog();
        assertNotNull(commits);
        activity.showStyledDialog(commits);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(activity.getColor(R.color.tp_text_secondary),
            commits.getButton(AlertDialog.BUTTON_NEGATIVE).getCurrentTextColor());
        String message = ((TextView) commits.findViewById(R.id.git_commit_history_message)).getText()
            .toString();
        assertTrue(message.contains("当前分支：dev"));
        assertTrue(message.contains("当前目标：hdr@192.168.1.153:22 · ~/repo"));
        assertTrue(message.contains("只读"));
        ListView list = commits.findViewById(R.id.git_commit_history_list);
        assertNotNull(list);
        assertEquals(2, list.getAdapter().getCount());
        assertTrue(list.getAdapter().getItem(0).toString().contains("abc1234"));
        assertTrue(list.getAdapter().getItem(0).toString().contains("修复滚动"));
        EditText filter = commits.findViewById(R.id.git_commit_history_filter);
        assertNotNull(filter);
        filter.setText("Git");
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(1, list.getAdapter().getCount());
        assertTrue(list.getAdapter().getItem(0).toString().contains("def5678"));
        filter.setText("missing");
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(0, list.getAdapter().getCount());
        assertTrue(((TextView) commits.findViewById(R.id.git_commit_history_empty)).getText()
            .toString().contains("missing"));
    }

    @Test
    public void overviewShowsActionableNextStepForCommonGitStates() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t2\t0\t2\t\t\t0\n");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();

        TextView nextStep = activity.findViewById(R.id.git_overview_next_step);
        assertTrue(nextStep.getText().toString().contains("审查"));
        assertTrue(nextStep.getText().toString().contains("保存 Stash"));

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t3\t2\t1\t\t\t0\n");
        assertTrue(nextStep.getText().toString().contains("已有 2 个文件进入暂存区"));
        assertTrue(nextStep.getText().toString().contains("仍有 1 个未暂存文件"));

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t0\t3\t1\torigin/dev\n");
        assertTrue(nextStep.getText().toString().contains("落后 3 个提交"));
        assertTrue(nextStep.getText().toString().contains("快进拉取"));

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t2\t0\t1\torigin/dev\n");
        assertTrue(nextStep.getText().toString().contains("领先 2 个提交"));
        assertTrue(nextStep.getText().toString().contains("不会 force push"));

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t\t\t0\n");
        assertTrue(nextStep.getText().toString().contains("没有上游"));
        assertTrue(nextStep.getText().toString().contains("不会替你猜目标分支"));

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tabc1234\t1\t0\t0\t0\t\t\t0\n");
        assertTrue(nextStep.getText().toString().contains("游离 HEAD"));

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t0\t0\t1\torigin/dev\n");
        assertTrue(nextStep.getText().toString().contains("工作树干净"));
        assertTrue(nextStep.getText().toString().contains("启动 Claude/Codex"));
    }

    @Test
    public void primaryActionMapsRecommendationToSafeExistingGitFlow() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t2\t0\t2\t\t\t0\n"
                + "TP_STATUS_Z\000"
                + " M app/src/main/java/App.java\000"
                + "?? README.md\000");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();
        Button primary = activity.findViewById(R.id.git_overview_primary_action_button);

        assertEquals("推荐：按文件审查", primary.getText().toString());
        assertTrue(primary.getContentDescription().toString().contains("不提交、不推送、不丢弃"));
        primary.performClick();
        assertEquals("按文件", shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getTitle()
            .toString());
        ShadowAlertDialog.getLatestAlertDialog().dismiss();

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t2\t2\t0\t\t\t0\n");
        assertEquals("推荐：提交已暂存修改", primary.getText().toString());
        primary.performClick();
        assertEquals("提交", shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getTitle()
            .toString());
        ShadowAlertDialog.getLatestAlertDialog().dismiss();

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t0\t2\t1\torigin/dev\n");
        assertEquals("推荐：快进拉取", primary.getText().toString());
        primary.performClick();
        assertEquals("快进拉取", shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getTitle()
            .toString());
        ShadowAlertDialog.getLatestAlertDialog().dismiss();

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t2\t0\t1\torigin/dev\n");
        assertEquals("推荐：普通推送", primary.getText().toString());
        primary.performClick();
        assertEquals("推送", shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getTitle()
            .toString());
        ShadowAlertDialog.getLatestAlertDialog().dismiss();

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t\t\t0\n");
        assertEquals("推荐：新建分支", primary.getText().toString());
        primary.performClick();
        assertEquals("新建分支", shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getTitle()
            .toString());
        ShadowAlertDialog.getLatestAlertDialog().dismiss();

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tabc1234\t1\t0\t0\t0\t\t\t0\n"
            + "TP_LOCAL\tdev\n");
        assertEquals("推荐：选择分支", primary.getText().toString());
        primary.performClick();
        assertEquals("切换分支", shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getTitle()
            .toString());
        ShadowAlertDialog.getLatestAlertDialog().dismiss();

        activity.showOverviewForTesting("~/repo", "TP_OVERVIEW\tdev\t0\t0\t0\t0\t0\t0\t1\torigin/dev\n"
            + "TP_LOG\tabc1234\t2 minutes ago\tfix: 修复滚动\n");
        assertEquals("推荐：查看提交记录", primary.getText().toString());
        primary.performClick();
        assertEquals("提交记录", shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getTitle()
            .toString());
        ShadowAlertDialog.getLatestAlertDialog().dismiss();
    }

    @Test
    public void stashDialogsExplainSafeCreateApplyAndDropRules() {
        Intent dirtyIntent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t3\t1\t2\t\t\t0\n"
                + "TP_LOCAL\tdev\n"
                + "TP_STASH\tstash@{0}\t1 hour ago\tWIP mobile flow\n");
        GitDiffActivity dirty = Robolectric.buildActivity(GitDiffActivity.class, dirtyIntent)
            .setup().get();

        AlertDialog stash = dirty.createStashDialog();
        assertNotNull(stash);
        stash.show();
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(dirty.getString(R.string.git_workbench_stash_save_action),
            stash.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        assertTrue(((TextView) stash.findViewById(android.R.id.message)).getText().toString()
            .contains("不会提交，也不会推送"));
        ((EditText) stash.findViewById(android.R.id.edit)).setText("bad\nstash");
        stash.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertNotNull(((EditText) stash.findViewById(android.R.id.edit)).getError());
        stash.dismiss();

        AlertDialog stashes = dirty.createStashesDialog();
        assertNotNull(stashes);
        dirty.showStyledDialog(stashes);
        assertEquals(1, stashes.getListView().getAdapter().getCount());
        assertTrue(stashes.getListView().getAdapter().getItem(0).toString()
            .contains("WIP mobile flow"));

        dirty.confirmApplyStash(dirty.mOverviewForTesting().stashes.get(0));
        assertTrue(((TextView) dirty.findViewById(R.id.git_diff_status_message)).getText()
            .toString().contains("已阻止应用 stash"));

        Intent cleanIntent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t0\t0\t0\t\t\t0\n"
                + "TP_LOCAL\tdev\n"
                + "TP_STASH\tstash@{0}\t1 hour ago\tWIP mobile flow\n");
        GitDiffActivity clean = Robolectric.buildActivity(GitDiffActivity.class, cleanIntent)
            .setup().get();
        GitRepositoryOverview.StashEntry entry = clean.mOverviewForTesting().stashes.get(0);

        clean.confirmApplyStash(entry);
        AlertDialog apply = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(apply);
        assertTrue(((TextView) apply.findViewById(android.R.id.message)).getText()
            .toString().contains("stash 会保留"));
        apply.dismiss();

        clean.confirmDropStash(entry);
        AlertDialog drop = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(drop);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(clean.getString(R.string.git_workbench_stash_drop_action),
            drop.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        assertEquals(clean.getColor(R.color.tp_danger),
            drop.getButton(AlertDialog.BUTTON_POSITIVE).getCurrentTextColor());
        assertTrue(((TextView) drop.findViewById(android.R.id.message)).getText()
            .toString().contains("不触碰工作树和远端仓库"));
    }

    @Test
    public void changedFilesDialogShowsPerFileIndexActions() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t3\t2\t2\t\t\t0\n"
                + "TP_LOCAL\tdev\n"
                + "TP_STATUS_Z\000"
                + "M  staged.txt\000"
                + " M unstaged.txt\000"
                + "MM mixed.txt\000");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();

        assertTrue(((Button) activity.findViewById(R.id.git_overview_files_button)).isEnabled());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.git_overview_review_actions)
            .getVisibility());
        assertEquals("按文件审查", ((Button) activity.findViewById(
            R.id.git_overview_files_button)).getText().toString());
        AlertDialog files = activity.createChangedFilesDialog();
        assertNotNull(files);
        activity.showStyledDialog(files);
        assertEquals(3, files.getListView().getAdapter().getCount());
        assertTrue(files.getListView().getAdapter().getItem(0).toString().contains("已暂存"));
        assertTrue(files.getListView().getAdapter().getItem(1).toString().contains("未暂存"));
        assertTrue(files.getListView().getAdapter().getItem(2).toString().contains("已暂存 + 未暂存"));
        files.getListView().performItemClick(files.getListView().getAdapter().getView(2, null,
            files.getListView()), 2, 2);
        AlertDialog actions = ShadowAlertDialog.getLatestAlertDialog();
        assertEquals(2, actions.getListView().getAdapter().getCount());
        assertEquals(activity.getString(R.string.git_workbench_stage_file_action),
            actions.getListView().getAdapter().getItem(0).toString());
        assertEquals(activity.getString(R.string.git_workbench_unstage_file_action),
            actions.getListView().getAdapter().getItem(1).toString());
    }

    @Test
    public void commitDialogExplainsScopeAndValidatesMessage() {
        Intent intent = GitDiffActivity.newIntent(RuntimeEnvironment.getApplication(),
                "hdr@192.168.1.153", 22, "~/repo")
            .putExtra(GitDiffActivity.EXTRA_UI_TEST_OVERVIEW, "TP_OVERVIEW\tdev\t0\t3\t2\t1\t\t\t0\n"
                + "TP_LOCAL\tdev\n");
        GitDiffActivity activity = Robolectric.buildActivity(GitDiffActivity.class, intent)
            .setup().get();

        AlertDialog dialog = activity.createCommitDialog();
        assertNotNull(dialog);
        dialog.show();
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(activity.getString(R.string.git_workbench_commit_action),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        assertEquals(activity.getColor(R.color.tp_primary),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).getCurrentTextColor());
        assertTrue(((TextView) dialog.findViewById(android.R.id.message)).getText().toString()
            .contains("2 个已暂存文件"));
        assertTrue(((TextView) dialog.findViewById(android.R.id.message)).getText().toString()
            .contains("分支 dev"));
        assertTrue(((TextView) dialog.findViewById(android.R.id.message)).getText().toString()
            .contains("hdr@192.168.1.153:22 · ~/repo"));
        assertTrue(((TextView) dialog.findViewById(android.R.id.message)).getText().toString()
            .contains("1 个未暂存文件不会进入本次提交"));

        EditText input = dialog.findViewById(android.R.id.edit);
        assertNotNull(input);
        input.setText("");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertNotNull(input.getError());
        assertTrue(dialog.isShowing());
        dialog.dismiss();
    }
}
