package dev.sn.text;

import net.minecraft.network.chat.Style;

import java.util.Collection;

/**
 * Base for nodes that only change the {@link Style} of their children.
 */
public abstract class SimpleStylingNode extends ParentNode {
    public SimpleStylingNode(TextNode... children) {
        super(children);
    }

    public SimpleStylingNode(Collection<TextNode> children) {
        super(children);
    }

    @Override
    protected Style applyFormatting(Style style, ParserContext context) {
        return style.applyTo(this.style(context));
    }

    protected abstract Style style(ParserContext context);
}