package dev.sn.text;

import java.util.ArrayList;

/**
 * Renders the parts of the tree that cannot change and freezes them into a component, so that a
 * config message such as {@code Your nickname is <nickname>} is only walked once.
 */
public final class StaticPreParser implements NodeParser {
    public static final NodeParser INSTANCE = new StaticPreParser();

    private StaticPreParser() {
    }

    @Override
    public TextNode[] parseNodes(TextNode input) {
        return new TextNode[]{parse(input)};
    }

    public static TextNode parse(TextNode node) {
        if (!node.isDynamic()) {
            return new DirectComponentNode(node.toComponent(ParserContext.of(), true));
        }

        if (node instanceof ParentNode parent) {
            var children = new ArrayList<TextNode>(parent.getChildren().length);

            for (var child : parent.getChildren()) {
                children.add(parse(child));
            }

            return parent.copyWith(children.toArray(new TextNode[0]));
        }

        return node;
    }
}