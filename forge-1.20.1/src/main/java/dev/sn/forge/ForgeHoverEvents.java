package dev.sn.forge;

import dev.sn.text.HoverEvents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;

import java.util.UUID;

/**
 * Hover events on Minecraft 1.21.1, where {@link HoverEvent} holds a typed {@code Action} and its
 * value.
 */
final class ForgeHoverEvents implements HoverEvents {
    static final ForgeHoverEvents INSTANCE = new ForgeHoverEvents();

    private ForgeHoverEvents() {
    }

    @Override
    public Style text(String text) {
        if (text.isEmpty()) {
            return Style.EMPTY;
        }

        return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                net.minecraft.network.chat.Component.literal(text)));
    }

    @Override
    public Style entity(String entityType, String uuid, net.minecraft.network.chat.Component name) {
        var type = EntityType.byString(entityType).orElse(EntityType.PIG);
        var id = parseUuid(uuid);

        var info = new HoverEvent.EntityTooltipInfo(type, id, name);
        return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, info));
    }

    private static UUID parseUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (Exception e) {
            return new UUID(0, 0);
        }
    }
}