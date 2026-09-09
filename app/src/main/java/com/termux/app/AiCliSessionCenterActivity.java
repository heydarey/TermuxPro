package com.termux.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.termux.R;

/**
 * AI CLI 会话中心。
 *
 * 该页面只整理 TermuxPro 增值层上下文，不读取 Claude/Codex 私有历史，也不自动进入 tmux。
 * 真正启动命令仍由工作台或终端中的显式安全弹窗完成。
 */
public final class AiCliSessionCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_cli_session_center);

        findViewById(R.id.ai_cli_center_back).setOnClickListener(view -> finish());
        findViewById(R.id.ai_cli_center_open_workspace).setOnClickListener(view ->
            startActivity(new Intent(this, WorkspaceActivity.class)));
        findViewById(R.id.ai_cli_center_open_templates).setOnClickListener(view ->
            startActivity(new Intent(this, CustomCommandsActivity.class)));
        findViewById(R.id.ai_cli_center_open_diagnostic).setOnClickListener(view ->
            openEnvironmentPreflight());
        findViewById(R.id.ai_cli_center_open_tmux).setOnClickListener(view -> openTmuxSessions());
        findViewById(R.id.ai_cli_center_open_git).setOnClickListener(view -> openGitWorkbench());
        findViewById(R.id.ai_cli_center_open_project_tasks).setOnClickListener(view ->
            openProjectTasks());
        findViewById(R.id.ai_cli_center_claude_new).setOnClickListener(view ->
            launchAiCli(AiCliLaunchCommand.Tool.CLAUDE, AiCliLaunchCommand.Mode.NEW_SESSION));
        findViewById(R.id.ai_cli_center_claude_history).setOnClickListener(view ->
            launchAiCli(AiCliLaunchCommand.Tool.CLAUDE, AiCliLaunchCommand.Mode.PICK_HISTORY));
        findViewById(R.id.ai_cli_center_codex_new).setOnClickListener(view ->
            launchAiCli(AiCliLaunchCommand.Tool.CODEX, AiCliLaunchCommand.Mode.NEW_SESSION));
        findViewById(R.id.ai_cli_center_codex_history).setOnClickListener(view ->
            launchAiCli(AiCliLaunchCommand.Tool.CODEX, AiCliLaunchCommand.Mode.PICK_HISTORY));

        bindTarget();
        configureLargeFontHierarchy();
        bindCommands();
    }

    /** 大字体优先保证四个 AI 核心操作可见，完整上下文和策略仍由可读控件保留。 */
    private void configureLargeFontHierarchy() {
        if (getResources().getConfiguration().fontScale < 1.5f) return;
        findViewById(R.id.ai_cli_center_context_title).setVisibility(View.GONE);
        findViewById(R.id.ai_cli_center_target_label).setVisibility(View.GONE);
        TextView hint = findViewById(R.id.ai_cli_center_start_hint);
        hint.setText(R.string.ai_cli_center_start_hint_compact);
        hint.setContentDescription(getString(R.string.ai_cli_center_start_hint));
    }

    private void bindTarget() {
        TextView target = findViewById(R.id.ai_cli_center_target);
        TextView detail = findViewById(R.id.ai_cli_center_target_detail);
        TextView policy = findViewById(R.id.ai_cli_center_policy_summary);
        TextView aiRisk = findViewById(R.id.ai_cli_center_ai_risk);
        WorkspaceTarget workspace = WorkspaceTargetStore.readActive(this);
        if (workspace == null || workspace.host == null || workspace.host.trim().isEmpty()
            || workspace.port < 1 || workspace.path == null || workspace.path.trim().isEmpty()) {
            target.setText(R.string.ai_cli_center_target_missing);
            detail.setText(R.string.ai_cli_center_target_missing_detail);
            policy.setVisibility(View.GONE);
            aiRisk.setText(R.string.ai_cli_center_ai_risk_missing);
            return;
        }
        target.setText(workspace.name);
        // 首屏只保留用户启动 AI 前必须确认的服务器与目录，策略说明留在后续安全工具区，
        // 避免大字体下把“新建 AI 会话”压出可见范围。
        detail.setText(getString(R.string.ai_cli_center_target_detail,
            workspace.host, workspace.port, workspace.path));
        policy.setVisibility(View.VISIBLE);
        policy.setText(policySummary(workspace.connectionPolicy, workspace.sessionName)
            + "\n" + getString(R.string.ai_cli_center_ai_policy));
        aiRisk.setText(aiLaunchRiskSummary(workspace.connectionPolicy, workspace.sessionName));
    }

    private String policySummary(String policy, String sessionName) {
        if (WorkspaceCommandBuilder.POLICY_LIST_SESSIONS.equals(policy)) {
            return getString(R.string.ai_cli_center_policy_list_sessions);
        }
        if (WorkspaceCommandBuilder.POLICY_ATTACH_SESSION.equals(policy)) {
            return getString(R.string.ai_cli_center_policy_attach_session,
                sessionDisplayName(sessionName));
        }
        if (WorkspaceCommandBuilder.POLICY_CREATE_OR_ATTACH.equals(policy)) {
            return getString(R.string.ai_cli_center_policy_create_or_attach,
                sessionDisplayName(sessionName));
        }
        if (!WorkspaceCommandBuilder.POLICY_SSH_ONLY.equals(policy)) {
            return getString(R.string.ai_cli_center_policy_unknown);
        }
        return getString(R.string.ai_cli_center_policy_ssh_only);
    }

    private String aiLaunchRiskSummary(String policy, String sessionName) {
        if (WorkspaceCommandBuilder.POLICY_ATTACH_SESSION.equals(policy)
            || WorkspaceCommandBuilder.POLICY_CREATE_OR_ATTACH.equals(policy)) {
            return getString(R.string.ai_cli_center_ai_risk_tmux,
                sessionDisplayName(sessionName));
        }
        if (WorkspaceCommandBuilder.POLICY_LIST_SESSIONS.equals(policy)) {
            return getString(R.string.ai_cli_center_ai_risk_list_tmux);
        }
        if (!WorkspaceCommandBuilder.POLICY_SSH_ONLY.equals(policy)) {
            return getString(R.string.ai_cli_center_ai_risk_unknown_policy);
        }
        return getString(R.string.ai_cli_center_ai_risk_ssh_only);
    }

    private String sessionDisplayName(String sessionName) {
        return sessionName == null || sessionName.trim().isEmpty()
            ? getString(R.string.ai_cli_center_policy_session_missing)
            : sessionName;
    }

    private void bindCommands() {
        ((TextView) findViewById(R.id.ai_cli_center_claude_commands)).setText(
            getString(R.string.ai_cli_center_command_pair,
                AiCliLaunchCommand.command(AiCliLaunchCommand.Tool.CLAUDE,
                    AiCliLaunchCommand.Mode.NEW_SESSION),
                AiCliLaunchCommand.command(AiCliLaunchCommand.Tool.CLAUDE,
                    AiCliLaunchCommand.Mode.PICK_HISTORY)));
        ((TextView) findViewById(R.id.ai_cli_center_codex_commands)).setText(
            getString(R.string.ai_cli_center_command_pair,
                AiCliLaunchCommand.command(AiCliLaunchCommand.Tool.CODEX,
                    AiCliLaunchCommand.Mode.NEW_SESSION),
                AiCliLaunchCommand.command(AiCliLaunchCommand.Tool.CODEX,
                    AiCliLaunchCommand.Mode.PICK_HISTORY)));
    }

    private void openTmuxSessions() {
        Intent intent = TaskSessionsNavigation.newIntentForActiveWorkspace(this);
        if (intent == null) {
            startActivity(new Intent(this, WorkspaceActivity.class));
            return;
        }
        startActivity(intent);
    }

    private void openGitWorkbench() {
        Intent intent = GitWorkbenchNavigation.newIntentForActiveWorkspace(this, false);
        if (intent == null) {
            startActivity(new Intent(this, WorkspaceActivity.class));
            return;
        }
        startActivity(intent);
    }

    private void openProjectTasks() {
        Intent intent = ProjectTasksNavigation.newIntentForActiveWorkspace(this);
        if (intent == null) {
            startActivity(new Intent(this, WorkspaceActivity.class));
            return;
        }
        startActivity(intent);
    }

    private void openEnvironmentPreflight() {
        Intent intent = ConnectionDiagnosticNavigation.newIntentForActiveWorkspace(this);
        if (intent == null) {
            startActivity(new Intent(this, WorkspaceActivity.class));
            return;
        }
        startActivity(intent);
    }

    private void launchAiCli(AiCliLaunchCommand.Tool tool, AiCliLaunchCommand.Mode mode) {
        WorkspaceTarget workspace = WorkspaceTargetStore.readActive(this);
        if (workspace == null || !SshTargetValidator.isValid(workspace.host)
            || workspace.port < 1 || workspace.port > 65535
            || workspace.path == null || workspace.path.trim().isEmpty()) {
            startActivity(new Intent(this, WorkspaceActivity.class));
            return;
        }
        String command = WorkspaceCommandBuilder.buildSshCommand(
            workspace.host, workspace.port, workspace.path,
            AiCliLaunchCommand.command(tool, mode),
            WorkspaceCommandBuilder.POLICY_SSH_ONLY, "");
        startActivity(new Intent(this, TermuxActivity.class)
            .putExtra(TermuxActivity.EXTRA_STARTUP_COMMAND, command)
            .putExtra(TermuxActivity.EXTRA_NEW_SESSION, true));
    }
}
