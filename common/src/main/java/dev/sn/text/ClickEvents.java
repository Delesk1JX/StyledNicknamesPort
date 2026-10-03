package dev.sn.text;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;

import java.net.URI;

/**
 * Builds the click events a nickname can carry.
 *
 * <p>The shape of {@link ClickEvent} changed between the targeted versions: up to 1.21.1 it was a
 * class with an {@code Action} enum and a string value, while 26.1 replaced it with a sealed
 * hierarchy of records. Only the actions that exist on both are exposed, and each version implements
 * this small factory.
 */
public interface ClickEvents {
    String OPEN_URL = "open_url";
    String RUN_COMMAND = "run_command";
    String SUGGEST_COMMAND = "suggest_command";
    String COPY_TO_CLIPBOARD = "copy_to_clipboard";
    String CHANGE_PAGE = "change_page";

    /**
     * @return a style carrying the requested click event, or an empty style when the value cannot be
     *         turned into one
     */
    Style style(String action, String value);

    /**
     * @return the action of an existing click event under the names used here, or null when it is of
     *         a kind this mod does not rebuild
     */
    String actionOf(ClickEvent event);

    static boolean isKnown(String action) {
        return OPEN_URL.equals(action) || RUN_COMMAND.equals(action) || SUGGEST_COMMAND.equals(action)
                || COPY_TO_CLIPBOARD.equals(action) || CHANGE_PAGE.equals(action);
    }

    static URI uri(String value) {
        try {
            return URI.create(value);
        } catch (Exception e) {
            return null;
        }
    }
}