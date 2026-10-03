package dev.sn.text;

import net.minecraft.network.chat.Component;

public record EmptyNode() implements TextNode {
    public static final EmptyNode INSTANCE = new EmptyNode();

    @Override
    public Component toComponent(ParserContext context, boolean removeBackslashes) {
        return Component.empty();
    }
}