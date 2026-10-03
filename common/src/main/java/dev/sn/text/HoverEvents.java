package dev.sn.text;

import net.minecraft.network.chat.Style;

/**
 * Builds the hover events a nickname can carry.
 *
 * <p>Like {@link ClickEvents}, the class behind hover events was reshaped between the targeted game
 * versions: up to 1.21.1 it held a typed {@code Action}, while 26.1 uses a sealed hierarchy of
 * records. Each version implements this, and the parser only asks for the two kinds that exist
 * everywhere.
 */
public interface HoverEvents {
    /**
     * @return a style showing {@code text} on hover, or an empty style when there is no text
     */
    Style text(String text);

    /**
     * @return a style showing an entity on hover, or an empty style when the value is unusable
     */
    Style entity(String entityType, String uuid, net.minecraft.network.chat.Component name);
}