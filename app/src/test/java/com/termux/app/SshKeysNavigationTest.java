package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.termux.R;

import java.lang.reflect.Method;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

/** 验证终端工具箱可打开 SSH 公钥管理增值页，且工作区无效时失败关闭。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public final class SshKeysNavigationTest {
    @Test
    public void opensSshKeysForActiveWorkspace() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        JSONArray profiles = new JSONArray()
            .put(new JSONObject()
                .put("id", "workspace-a")
                .put("host", "hdr@192.168.1.153")
                .put("port", "22")
                .put("path", "~/project"));
        context.getSharedPreferences(WorkspaceTargetStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(WorkspaceTargetStore.KEY_PROFILES, profiles.toString())
            .putString(WorkspaceTargetStore.KEY_ACTIVE_PROFILE, "workspace-a")
            .commit();

        Intent intent = SshKeysNavigation.newIntentForActiveWorkspace(context);

        assertEquals(SshKeysActivity.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void rejectsMissingWorkspace() {
        assertNull(SshKeysNavigation.newIntentForActiveWorkspace(
            RuntimeEnvironment.getApplication()));
    }

    @Test
    public void keyConfirmDialogsShowLocalAndRemoteBoundaries() throws Exception {
        Intent intent = SshKeysActivity.newIntent(RuntimeEnvironment.getApplication(),
            "hdr@192.168.1.153", 2222);
        SshKeysActivity activity = Robolectric.buildActivity(SshKeysActivity.class, intent)
            .setup().get();

        invokePrivate(activity, "confirmGenerate");
        AlertDialog generate = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(generate);
        String generateMessage = ((TextView) generate.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(generateMessage.contains("应用私有的 OpenSSH 目录"));
        assertTrue(generateMessage.contains("不会读取或保存口令"));
        generate.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Intent generateIntent = shadowOf(activity).getNextStartedActivity();
        assertTrue(generateIntent.getStringExtra(TermuxActivity.EXTRA_STARTUP_COMMAND)
            .contains("ssh-keygen -t ed25519 -a 64"));

        invokePrivate(activity, "confirmInstall");
        AlertDialog install = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(install);
        String installMessage = ((TextView) install.findViewById(android.R.id.message))
            .getText().toString();
        assertTrue(installMessage.contains("hdr@192.168.1.153:2222"));
        assertTrue(installMessage.contains("ssh-copy-id -p 2222 -- 'hdr@192.168.1.153'"));
        assertTrue(installMessage.contains("不会读取或保存服务器密码"));
        install.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Intent installIntent = shadowOf(activity).getNextStartedActivity();
        assertEquals("ssh-copy-id -p 2222 -- 'hdr@192.168.1.153'",
            installIntent.getStringExtra(TermuxActivity.EXTRA_STARTUP_COMMAND));
    }

    private static void invokePrivate(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }
}
