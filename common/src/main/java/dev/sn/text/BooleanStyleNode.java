package dev.sn.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.Arrays;

/**
 * A tri-state style flag, which is what {@code <bold>}, {@code <italic>} and friends need: present
 * turns it on, {@code value=false} turns it back off so an inner tag can override an outer one.
 */
public final class BooleanStyleNode extends SimpleStylingNode {
    private final ChatFormatting formatting;
    private final boolean value;

    public BooleanStyleNode(TextNode[] children, ChatFormatting formatting, boolean value) {
        super(children);
        this.formatting = formatting;
        this.value = value;
    }

    public boolean value() {
        return this.value;
    }

    @Override
    protected Style style(ParserContext context) {
        var style = Style.EMPTY;

        if (this.formatting == ChatFormatting.BOLD) {
            return style.withBold(this.value);
        } else if (this.formatting == ChatFormatting.ITALIC) {
            return style.withItalic(this.value);
        } else if (this.formatting == ChatFormatting.UNDERLINE) {
            return style.withUnderlined(this.value);
        } else if (this.formatting == ChatFormatting.STRIKETHROUGH) {
            return style.withStrikethrough(this.value);
        } else if (this.formatting == ChatFormatting.OBFUSCATED) {
            return style.withObfuscated(this.value);
        }

        return style;
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new BooleanStyleNode(children, this.formatting, this.value);
    }

    @Override
    public String toString() {
        return "BooleanStyleNode{" + this.formatting + "=" + this.value
                + ", children=" + Arrays.toString(getChildren()) + "}";
    }
}