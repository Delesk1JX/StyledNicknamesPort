package dev.sn.text;

import net.minecraft.network.chat.Component;

import java.util.Collection;

/**
 * A node that owns child nodes. Styling nodes extend this and apply their effect while rendering.
 */
public interface ParentTextNode extends TextNode {
    TextNode[] getChildren();

    ParentTextNode copyWith(TextNode[] children);

    default ParentTextNode copyWith(Collection<TextNode> children) {
        return this.copyWith(children.toArray(new TextNode[0]));
    }

    default ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        return this.copyWith(children);
    }

    default ParentTextNode copyWith(Collection<TextNode> children, NodeParser parser) {
        return this.copyWith(children.toArray(new TextNode[0]), parser);
    }

    default boolean isDynamicNoChildren() {
        return false;
    }

    @Override
    default boolean isDynamic() {
        for (var child : getChildren()) {
            if (child.isDynamic()) {
                return true;
            }
        }

        return this.isDynamicNoChildren();
    }
}