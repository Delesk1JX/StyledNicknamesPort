package dev.sn.text;

import net.minecraft.network.chat.Component;

/**
 * Hides a node from parsers that only understand literal text, keeping it intact for the final
 * render pass.
 */
public record NonTransformableNode(TextNode node) implements TextNode {
    @Override
    public Component toComponent(ParserContext context, boolean removeBackslashes) {
        return node.toComponent(context, removeBackslashes);
    }

    @Override
    public boolean isDynamic() {
        return node.isDynamic();
    }
}