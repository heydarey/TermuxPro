package com.termux.app.terminal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TermuxTerminalSessionActivityClientTest {

    @Test
    public void toastTitleAlwaysContainsSessionIndexEvenWithoutNameOrTitle() {
        assertEquals("Session 1", TermuxTerminalSessionActivityClient.formatToastTitle(0, null, null));
        assertEquals("会话 2",
            TermuxTerminalSessionActivityClient.formatToastTitle(1, "", "", "会话"));
    }

    @Test
    public void toastTitleKeepsNameAndTerminalTitleReadable() {
        assertEquals("Session 3 · ssh-153",
            TermuxTerminalSessionActivityClient.formatToastTitle(2, "ssh-153", null));
        assertEquals("Session 4\n~/project",
            TermuxTerminalSessionActivityClient.formatToastTitle(3, null, "~/project"));
        assertEquals("Session 5 · ssh-153\ncodex",
            TermuxTerminalSessionActivityClient.formatToastTitle(4, "ssh-153", "codex"));
    }

    @Test
    public void toastTitleCleansBlankLinesAndVeryLongLabels() {
        assertEquals("会话 6 · ssh-153\ncodex resume",
            TermuxTerminalSessionActivityClient.formatToastTitle(5, "  ssh-153\n", "\n codex\tresume ", "会话"));

        String longTitle =
            "01234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789";
        String formatted = TermuxTerminalSessionActivityClient.formatToastTitle(6, null, longTitle, "会话");
        assertEquals("会话 7\n0123456789012345678901234567890123456789012345678901234567890123456789012345678…",
            formatted);
    }

    @Test
    public void invalidSessionIndexDoesNotCreateFeedbackText() {
        assertNull(TermuxTerminalSessionActivityClient.formatToastTitle(-1, "ssh-153", "codex"));
    }
}
