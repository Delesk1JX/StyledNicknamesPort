package dev.sn.forge;

import dev.sn.text.ClickEvents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;

/**
 * Click events on Minecraft 1.21.1, where {@link ClickEvent} is a class holding an {@code Action}
 * enum and a string value.
 */
final class ForgeClickEvents implements ClickEvents {
    static final ForgeClickEvents INSTANCE = new ForgeClickEvents();

    private ForgeClickEvents() {
    }

    @Override
    public Style style(String action, String value) {
        var resolved = switch (action) {
            case OPEN_URL -> {
                var uri = ClickEvents.uri(value);
                yield uri != null ? ClickEvent.Action.OPEN_URL : null;
            }
            case CHANGE_PAGE -> ClickEvent.Action.CHANGE_PAGE;
            case RUN_COMMAND -> ClickEvent.Action.RUN_COMMAND;
            case SUGGEST_COMMAND -> ClickEvent.Action.SUGGEST_COMMAND;
            case COPY_TO_CLIPBOARD -> ClickEvent.Action.COPY_TO_CLIPBOARD;
            default -> null;
        };

        if (resolved == null) {
            return Style.EMPTY;
        }

        // CHANGE_PAGE needs a number, so a value that is not one is rejected instead of throwing.
        if (resolved == ClickEvent.Action.CHANGE_PAGE) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return Style.EMPTY;
            }
        }

        return Style.EMPTY.withClickEvent(new ClickEvent(resolved, value));
    }

    @Override
    public String actionOf(ClickEvent event) {
        var action = event.getAction();

        if (action == ClickEvent.Action.OPEN_URL) {
            return OPEN_URL;
        } else if (action == ClickEvent.Action.CHANGE_PAGE) {
            return CHANGE_PAGE;
        } else if (action == ClickEvent.Action.RUN_COMMAND) {
            return RUN_COMMAND;
        } else if (action == ClickEvent.Action.SUGGEST_COMMAND) {
            return SUGGEST_COMMAND;
        } else if (action == ClickEvent.Action.COPY_TO_CLIPBOARD) {
            return COPY_TO_CLIPBOARD;
        }

        return null;
    }
}