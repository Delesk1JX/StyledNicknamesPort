package dev.sn.core;

import dev.sn.text.LegacyProvider;
import dev.sn.text.NodeParser;
import dev.sn.text.TagLikeParser;
import dev.sn.text.TagRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the parser used to turn a nickname into text.
 *
 * <p>A player's own nickname is parsed against a registry holding only the tags their permissions
 * allow, so a nickname cannot carry formatting the server owner switched off.
 */
public final class ParserUtils {
    private ParserUtils() {
    }

    /**
     * @param player the author of the text, or null when the server itself is setting the name, in
     *               which case every tag is available
     */
    public static NodeParser forNickname(ServerPlayer player) {
        var config = ConfigManager.get().data;
        var builder = NodeParser.builder().simplifiedTextFormat().quickText();

        var registry = player == null
                ? TagRegistry.safe()
                : registryFor(player, config);

        builder.customTagRegistry(registry);

        if (player == null) {
            if (config.allowLegacyFormatting) {
                builder.legacyAll();
            }
        } else {
            var legacyColors = new ArrayList<ChatFormatting>();

            for (var formatting : ChatFormatting.values()) {
                if (config.allowLegacyFormatting && registry.getTag(formatting.getName()) != null) {
                    legacyColors.add(formatting);
                }
            }

            boolean rgbAllowed = registry.getTag("color") != null;

            if (!legacyColors.isEmpty() || rgbAllowed) {
                builder.legacy(rgbAllowed, legacyColors);
            }
        }

        return builder.build();
    }

    /**
     * @return every tag the player is allowed to use
     */
    private static TagRegistry registryFor(ServerPlayer player, ConfigData config) {
        var registry = TagRegistry.createSafe();

        for (var tag : registry.getTags()) {
            var enabled = config.defaultEnabledFormatting.getOrDefault(tag.name(), false);

            if (enabled) {
                continue;
            }

            // A tag that is off by default can still be granted per player.
            if (Permissions.checkFormatting(player, tag.name())) {
                continue;
            }

            registry.remove(tag);
        }

        return registry;
    }

    /**
     * Parses a nickname, used when it is set and when it is read back.
     */
    public static Component parseNickname(ServerPlayer player, String nickname) {
        return forNickname(player).parseComponent(nickname, dev.sn.text.ParserContext.of());
    }

    /**
     * Parses a text that is not a nickname, such as a chat message, where every tag is available.
     */
    public static NodeParser forConfig() {
        var config = ConfigManager.get().data;
        var builder = NodeParser.builder()
                .simplifiedTextFormat()
                .quickText()
                .placeholders(TagLikeParser.PLACEHOLDER_USER, Config.KEY);

        if (config.allowLegacyFormatting) {
            builder.legacyAll();
        }

        return builder.build();
    }


}