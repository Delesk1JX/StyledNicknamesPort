package dev.sn.neoforge;

import dev.sn.text.HoverEvents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;

import java.util.UUID;

/**
 * Hover events on Minecraft 26.1.2, where {@link HoverEvent} is a sealed hierarchy of records.
 */
final class NeoHoverEvents implements HoverEvents {
    static final NeoHoverEvents INSTANCE = new NeoHoverEvents();

    private NeoHoverEvents() {
    }

    @Override
    public Style text(String text) {
        if (text.isEmpty()) {
            return Style.EMPTY;
        }

        return Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(net.minecraft.network.chat.Component.literal(text)));
    }

    @Override
    public Style entity(String entityType, String uuid, net.minecraft.network.chat.Component name) {
        var type = EntityType.byString(entityType).orElse(EntityType.PIG);
        var id = parseUuid(uuid);

        return Style.EMPTY.withHoverEvent(new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(type, id, name)));
    }

    private static UUID parseUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (Exception e) {
            return new UUID(0, 0);
        }
    }
}