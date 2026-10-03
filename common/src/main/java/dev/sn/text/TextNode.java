package dev.sn.text;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * One node of the parsed text tree. Nodes are immutable and are turned into a real
 * {@link Component} only when {@link #toComponent(ParserContext, boolean)} is called.
 */
public interface TextNode {
    Component toComponent(ParserContext context, boolean removeBackslashes);

    default Component toComponent(ParserContext context) {
        return toComponent(context, true);
    }

    default Component toComponent() {
        return toComponent(ParserContext.of(), true);
    }

    /**
     * Whether this node can produce different output depending on the context it is rendered with.
     */
    default boolean isDynamic() {
        return false;
    }

    static TextNode of(String input) {
        return new LiteralNode(input);
    }

    static TextNode wrap(TextNode... nodes) {
        return new ParentNode(nodes);
    }

    static TextNode wrap(List<TextNode> nodes) {
        return new ParentNode(nodes.toArray(new TextNode[0]));
    }

    static TextNode asSingle(TextNode... nodes) {
        if (nodes.length == 0) {
            return EmptyNode.INSTANCE;
        } else if (nodes.length == 1) {
            return nodes[0];
        }

        return wrap(nodes);
    }

    static TextNode asSingle(List<TextNode> nodes) {
        return asSingle(nodes.toArray(new TextNode[0]));
    }

    static TextNode[] array(TextNode... nodes) {
        if (nodes.length == 1 && nodes[0] instanceof ParentNode parent) {
            return parent.getChildren();
        }

        return nodes;
    }

    static TextNode empty() {
        return EmptyNode.INSTANCE;
    }
}