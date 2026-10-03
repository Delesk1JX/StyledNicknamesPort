package dev.sn.text;

import net.minecraft.network.chat.Style;

import java.util.Arrays;

/**
 * Applies a whole {@link Style} at once, which is what {@code <rawstyle>} and the internal
 * "wrap into a style" path use.
 *
 * <p>A click event that is rebuilt from a parsed value goes through {@link ClickEvents}, because the
 * class that holds click events was reshaped between the targeted game versions.
 */
public final class StyledNode extends SimpleStylingNode {
    private final Style style;
    private final TextNode clickValue;
    private final TextNode insertion;

    public StyledNode(TextNode[] children, Style style) {
        this(children, style, null, null);
    }

    public StyledNode(TextNode[] children, Style style, TextNode clickValue, TextNode insertion) {
        super(children);
        this.style = style;
        this.clickValue = clickValue;
        this.insertion = insertion;
    }

    public Style rawStyle() {
        return this.style;
    }

    public TextNode clickValue() {
        return this.clickValue;
    }

    public TextNode insertion() {
        return this.insertion;
    }

    @Override
    protected Style style(ParserContext context) {
        var style = this.style;

        if (this.clickValue != null && style.getClickEvent() != null) {
            var value = this.clickValue.toComponent(context, true).getString();
            var action = EventProvider.clicks().actionOf(style.getClickEvent());

            if (action != null && ClickEvents.isKnown(action)) {
                var rebuilt = EventProvider.clicks().style(action, value);

                if (rebuilt.getClickEvent() == null) {
                    style = rebuilt;
                }
            }
        }

        if (this.insertion != null) {
            style = style.withInsertion(this.insertion.toComponent(context, true).getString());
        }

        return style;
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new StyledNode(children, this.style, this.clickValue, this.insertion);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        return new StyledNode(children, this.style,
                this.clickValue != null ? TextNode.asSingle(parser.parseNodes(this.clickValue)) : null,
                this.insertion != null ? TextNode.asSingle(parser.parseNodes(this.insertion)) : null);
    }

    @Override
    public boolean isDynamicNoChildren() {
        return (this.clickValue != null && this.clickValue.isDynamic())
                || (this.insertion != null && this.insertion.isDynamic());
    }

    @Override
    public String toString() {
        return "StyledNode{style=" + this.style + ", children=" + Arrays.toString(getChildren()) + "}";
    }
}