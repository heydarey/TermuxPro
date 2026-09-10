package com.termux.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.View;

import com.termux.R;
import com.termux.app.activities.SettingsActivity;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** 产品身份和非危险视觉语义的 Manifest 回归。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "zh-rCN")
public class ManifestProductIdentityTest {
    @Test
    public void settingsUsesTermuxProThemeInsteadOfUpstreamRedPrimaryTheme() throws Exception {
        String manifest = new String(Files.readAllBytes(Paths.get("src/main/AndroidManifest.xml")),
            StandardCharsets.UTF_8);
        assertTrue(manifest.contains("android:name=\".app.activities.SettingsActivity\"\n"
            + "            android:exported=\"true\"\n"
            + "            android:label=\"@string/title_activity_termux_settings\"\n"
            + "            android:theme=\"@style/Theme.TermuxPro.DayNight.NoActionBar\""));
    }

    @Test
    public void settingsToolbarResolvesNeutralProductColorAtRuntime() {
        SettingsActivity activity = Robolectric.buildActivity(SettingsActivity.class).setup().get();

        TypedValue colorPrimaryDark = new TypedValue();
        assertTrue(activity.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark,
            colorPrimaryDark, true));
        assertEquals(activity.getColor(R.color.tp_surface), colorPrimaryDark.data);
        assertNotEquals(activity.getColor(com.termux.shared.R.color.red_400), colorPrimaryDark.data);
        assertNotEquals(activity.getColor(com.termux.shared.R.color.red_800), colorPrimaryDark.data);

        View toolbar = activity.findViewById(com.termux.shared.R.id.toolbar);
        assertTrue(toolbar.getBackground() instanceof ColorDrawable);
        assertEquals(activity.getColor(R.color.tp_surface),
            ((ColorDrawable) toolbar.getBackground()).getColor());

        activity.finish();
    }
}
