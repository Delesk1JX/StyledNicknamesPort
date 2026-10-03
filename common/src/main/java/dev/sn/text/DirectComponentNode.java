package dev.sn.text;

import net.minecraft.network.chat.Component;

/**
 * Wraps an already built component, so that a static sub-tree only has to be rendered once.
 */
public record DirectComponentNode(Component component) implements TextNode {
    @Override
    public Component toComponent(ParserContext context, boolean removeBackslashes) {
        return this.component;
    }
}