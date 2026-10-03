package dev.sn.text;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Colour resolved at render time, used by {@code <color>...</color>} where the argument itself may
 * be a placeholder.
 */
public final class DynamicColorNode extends SimpleStylingNode {
    private static final Function<String, TextColor> DEFAULT_RESOLVER =
            Colors::parse;

    private final TextNode color;
    private final Function<String, TextColor> resolver;

    public DynamicColorNode(TextNode[] children, TextNode color) {
        this(children, color, DEFAULT_RESOLVER);
    }

    public DynamicColorNode(TextNode[] children, TextNode color, Function<String, TextColor> resolver) {
        super(children);
        this.color = color;
        this.resolver = resolver;
    }

    public static Function<String, TextColor> extendedTextColorParse(Function<String, TextColor> resolver) {
        return string -> {
            var color = resolver.apply(string);
            return color != null ? color : Colors.parse(string);
        };
    }

    @Override
    public boolean isDynamicNoChildren() {
        return this.color.isDynamic();
    }

    @Override
    protected Style style(ParserContext context) {
        var resolved = this.resolver.apply(this.color.toComponent(context).getString());
        return resolved != null ? Style.EMPTY.withColor(resolved) : Style.EMPTY;
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new DynamicColorNode(children, this.color, this.resolver);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        return new DynamicColorNode(children, parser.parseNode(this.color), this.resolver);
    }

    @Override
    public String toString() {
        return "DynamicColorNode{color=" + this.color
                + ", children=" + Arrays.toString(getChildren()) + "}";
    }
}