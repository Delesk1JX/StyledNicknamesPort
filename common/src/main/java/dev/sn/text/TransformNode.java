package dev.sn.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Rewrites the rendered component of its children, which is how {@code <clear>} and
 * {@code <clear_color>} are expressed.
 */
public final class TransformNode extends ParentNode {
    private final Function<MutableComponent, Component> transform;

    public TransformNode(TextNode[] children, Function<MutableComponent, Component> transform) {
        super(children);
        this.transform = transform;
    }

    public static TransformNode deepStyle(Function<Style, Style> styleFunction, TextNode... nodes) {
        return new TransformNode(nodes, new StyleTransformer(styleFunction));
    }

    @Override
    protected Component applyFormatting(MutableComponent out, ParserContext context) {
        return this.transform.apply(out);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new TransformNode(children, this.transform);
    }

    @Override
    public String toString() {
        return "TransformNode{children=" + Arrays.toString(getChildren()) + "}";
    }

    /**
     * Applies a style function to every component of the tree, dropping hover and click events so
     * that a cleared segment cannot keep them.
     */
    public record StyleTransformer(Function<Style, Style> styleFunction) implements Function<MutableComponent, Component> {
        @Override
        public Component apply(MutableComponent text) {
            return TextUtils.cloneTransformText(text, this::transformStyle);
        }

        private MutableComponent transformStyle(MutableComponent text) {
            var style = this.styleFunction.apply(text.getStyle());
            return text.setStyle(style.withHoverEvent(null).withClickEvent(null));
        }
    }
}