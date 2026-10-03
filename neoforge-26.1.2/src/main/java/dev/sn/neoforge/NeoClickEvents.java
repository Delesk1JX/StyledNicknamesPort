package dev.sn.neoforge;

import dev.sn.text.ClickEvents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;

/**
 * Click events on Minecraft 26.1.2, where {@link ClickEvent} is a sealed hierarchy of records.
 *
 * <p>Only the actions that also exist on the older targets are produced, and
 * {@code copy_to_clipboard} is skipped when the game does not offer it.
 */
final class NeoClickEvents implements ClickEvents {
    static final NeoClickEvents INSTANCE = new NeoClickEvents();

    private NeoClickEvents() {
    }

    @Override
    public Style style(String action, String value) {
        return switch (action) {
            case OPEN_URL -> {
                var uri = ClickEvents.uri(value);
                yield uri != null ? Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(uri)) : Style.EMPTY;
            }
            case CHANGE_PAGE -> {
                try {
                    yield Style.EMPTY.withClickEvent(new ClickEvent.ChangePage(Integer.parseInt(value)));
                } catch (NumberFormatException e) {
                    yield Style.EMPTY;
                }
            }
            case RUN_COMMAND -> Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(value));
            case SUGGEST_COMMAND -> Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand(value));
            case COPY_TO_CLIPBOARD -> Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(value));
            default -> Style.EMPTY;
        };
    }

    @Override
    public String actionOf(ClickEvent event) {
        if (event instanceof ClickEvent.OpenUrl) {
            return OPEN_URL;
        } else if (event instanceof ClickEvent.ChangePage) {
            return CHANGE_PAGE;
        } else if (event instanceof ClickEvent.RunCommand) {
            return RUN_COMMAND;
        } else if (event instanceof ClickEvent.SuggestCommand) {
            return SUGGEST_COMMAND;
        } else if (event instanceof ClickEvent.CopyToClipboard) {
            return COPY_TO_CLIPBOARD;
        }

        return null;
    }
}