package dev.sn.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.Collection;

/**
 * Base implementation that renders children and then lets a subclass restyle the result.
 */
public class ParentNode implements ParentTextNode {
    public static final ParentNode EMPTY = new ParentNode(new TextNode[0]);

    protected final TextNode[] children;

    public ParentNode(TextNode... children) {
        this.children = children;
    }

    public ParentNode(Collection<TextNode> children) {
        this(children.toArray(new TextNode[0]));
    }

    @Override
    public final TextNode[] getChildren() {
        return this.children;
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new ParentNode(children);
    }

    @Override
    public Component toComponent(ParserContext context, boolean removeBackslashes) {
        var compact = context.get(ParserContext.COMPACT_COMPONENT) != Boolean.FALSE;

        if (this.children.length == 0) {
            return Component.empty();
        }

        if (this.children.length == 1 && this.children[0] != null && compact) {
            var out = this.children[0].toComponent(context, true);

            if (out.getString().isEmpty()) {
                return out;
            }

            return this.applyFormatting(out.copy(), context);
        }

        MutableComponent base = compact ? null : Component.empty();
        var styled = new java.util.ArrayList<Component>(this.children.length);

        for (var child : this.children) {
            if (child == null) {
                continue;
            }

            var component = child.toComponent(context, true);

            if (component.getString().isEmpty()) {
                continue;
            }

            if (base == null && component.getStyle().isEmpty()) {
                // The first child with no styling of its own can adopt the parent's style.
                base = component.copy();
            } else {
                if (base == null) {
                    base = Component.empty();
                }

                // A child that carries styling has to be kept as a separate component, because
                // setting this node's style afterwards must not overwrite it.
                if (!component.getStyle().isEmpty()) {
                    styled.add(component);
                }

                base.append(component);
            }
        }

        if (base == null || base.getString().isEmpty()) {
            return Component.empty();
        }

        var result = this.applyFormatting(base, context);

        // Children that had their own style keep it; the parent's style is applied around them.
        if (!styled.isEmpty() && result != base) {
            var wrapper = Component.empty();
            wrapper.append(result);

            for (var child : styled) {
                wrapper.append(child);
            }

            return wrapper;
        }

        return result;
    }

    protected Component applyFormatting(MutableComponent out, ParserContext context) {
        return out.setStyle(applyFormatting(out.getStyle(), context));
    }

    protected Style applyFormatting(Style style, ParserContext context) {
        return style;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{children=" + Arrays.toString(children) + "}";
    }
}