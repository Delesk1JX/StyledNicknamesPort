package dev.sn.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.function.Function;

/**
 * Placeholder resolved from a {@link ParserContext} at render time, which is what makes the
 * configured {@code nicknameFormat} reusable for every player.
 */
public record DynamicTextNode(String id, ParserContext.Key<Function<String, Component>> key) implements TextNode {
    public static ParserContext.Key<Function<String, Component>> key(String id) {
        return ParserContext.Key.of("dynamic:" + id);
    }

    @Override
    public Component toComponent(ParserContext context, boolean removeBackslashes) {
        var function = context.get(this.key);

        if (function != null) {
            var component = function.apply(this.id);

            if (component != null) {
                return component;
            }

            return invalid("[INVALID KEY " + this.key.key() + " | " + this.id + "]");
        }

        return invalid("[MISSING CONTEXT FOR " + this.key.key() + " | " + this.id + "]");
    }

private static Component invalid(String message) {
        // Neither withColor overload exists on the oldest target, so the style is set directly.
        var style = Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000)).withItalic(true);
        return Component.literal(message).setStyle(style);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}