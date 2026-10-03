package dev.sn.text;

import net.minecraft.network.chat.Style;

import java.util.Arrays;

/**
 * {@code <insert>}: the text inserted into the chat box when the segment is shift clicked.
 */
public final class InsertNode extends SimpleStylingNode {
    private final TextNode value;

    public InsertNode(TextNode[] children, TextNode value) {
        super(children);
        this.value = value;
    }

    public TextNode value() {
        return this.value;
    }

    @Override
    protected Style style(ParserContext context) {
        return Style.EMPTY.withInsertion(this.value.toComponent(context, true).getString());
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new InsertNode(children, this.value);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        return new InsertNode(children, TextNode.asSingle(parser.parseNodes(this.value)));
    }

    @Override
    public boolean isDynamicNoChildren() {
        return this.value.isDynamic();
    }

    @Override
    public String toString() {
        return "InsertNode{children=" + Arrays.toString(getChildren()) + "}";
    }
}