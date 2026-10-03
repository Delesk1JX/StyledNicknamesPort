package dev.sn.text;

import net.minecraft.network.chat.Component;

/**
 * Plain text. Backslash escapes are resolved at render time so that a literal backslash survives
 * a second parse pass.
 */
public record LiteralNode(String value) implements TextNode {
    public LiteralNode(StringBuilder builder) {
        this(builder.toString());
    }

    @Override
    public Component toComponent(ParserContext context, boolean removeBackslashes) {
        if (this.value.isEmpty()) {
            return Component.empty();
        }

        if (!removeBackslashes) {
            return Component.literal(this.value);
        }

        var builder = new StringBuilder();
        var length = this.value.length();

        for (var i = 0; i < length; i++) {
            var c = this.value.charAt(i);

            if (c == '\\' && i + 1 < length) {
                var next = this.value.charAt(i + 1);

                if (Character.isWhitespace(next) || Character.isLetterOrDigit(next)) {
                    builder.append(c);
                } else {
                    builder.append(next);
                    i++;
                }
            } else {
                builder.append(c);
            }
        }

        return Component.literal(builder.toString());
    }
}