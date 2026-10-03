package dev.sn.text;

import net.minecraft.ChatFormatting;

/**
 * Colour helpers for the config, where tag names such as {@code red} decide whether a formatting
 * tag is enabled by default.
 */
public final class ChatColors {
    private ChatColors() {
    }

    /**
     * Vanilla's black is unreadable on the default dark chat background, which is why it is the one
     * colour a nickname does not get by default.
     */
    public static boolean isBlack(String tagName) {
        return ChatFormatting.BLACK.getName().equalsIgnoreCase(tagName);
    }
}