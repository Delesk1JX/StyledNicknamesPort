package dev.sn.text;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Arrays;

/**
 * Applies a fixed colour, coming from a named colour tag or from a hex literal.
 */
public final class ColorNode extends SimpleStylingNode {
    private final TextColor color;

    public ColorNode(TextNode[] children, TextColor color) {
        super(children);
        this.color = color;
    }

    public TextColor color() {
        return this.color;
    }

    @Override
    protected Style style(ParserContext context) {
        return Style.EMPTY.withColor(this.color);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new ColorNode(children, this.color);
    }

    @Override
    public String toString() {
        return "ColorNode{color=" + this.color + ", children=" + Arrays.toString(getChildren()) + "}";
    }
}