package dev.sn.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Component level helpers that the node types share.
 */
public final class TextUtils {
    private TextUtils() {
    }

    public static int rgbToInt(float r, float g, float b) {
        return (((int) (r * 0xff)) & 0xFF) << 16 | (((int) (g * 0xff)) & 0xFF) << 8 | (((int) (b * 0xff)) & 0xFF);
    }

    /**
     * Rebuilds {@code base} with one component per code point so that a gradient can colour each of
     * them. Parts that already carry a colour are left untouched.
     *
     * <p>A rendered text is a tree whose text may sit on the root or on any sibling, so the walk
     * goes over the whole tree instead of assuming a flat list of siblings.
     */
    public static MutableComponent toGradient(Component base, GradientNode.GradientProvider provider) {
        return recursiveGradient(base, provider, 0, length(base),
                text -> text.getStyle().getColor() == null,
                Style::withColor).text();
    }

    private static int length(Component base) {
        int length = base.getString().codePointCount(0, base.getString().length());

        for (var sibling : base.getSiblings()) {
            length += length(sibling);
        }

        return length;
    }

    private record TextLengthPair(MutableComponent text, int length) {
    }

    private static TextLengthPair recursiveGradient(Component base, GradientNode.GradientProvider provider,
                                                    int pos, int totalLength, Predicate<Component> canContinue,
                                                    BiFunction<Style, TextColor, Style> apply) {
        // A component that already carries a colour opts out of the gradient.
        if (!canContinue.test(base)) {
            var skipped = base.copy();
            return new TextLengthPair(skipped, pos + base.getString().length());
        }

        var text = base.getString();
        MutableComponent out = text.isEmpty() ? Component.empty() : Component.literal("").setStyle(base.getStyle());

        for (var character : text.codePoints().toArray()) {
            out.append(Component.literal(new String(Character.toChars(character)))
                    .setStyle(apply.apply(base.getStyle(), provider.getColorAt(pos++, totalLength))));
        }

        for (var sibling : base.getSiblings()) {
            var pair = recursiveGradient(sibling, provider, pos, totalLength, canContinue, apply);
            pos = pair.length();
            out.append(pair.text());
        }

        return new TextLengthPair(out, pos);
    }

    public static MutableComponent cloneTransformText(Component input, Function<MutableComponent, MutableComponent> transform) {
        return cloneTransformText(input, transform, text -> true);
    }

    public static MutableComponent cloneTransformText(Component input, Function<MutableComponent, MutableComponent> transform,
                                                      Predicate<Component> canContinue) {
        if (!canContinue.test(input)) {
            return input.copy();
        }

        var out = Component.empty().copy();
        out.append(Component.literal(input.getString()));

        for (var sibling : input.getSiblings()) {
            out.append(cloneTransformText(sibling, transform, canContinue));
        }

        out.setStyle(input.getStyle());

        return transform.apply(out);
    }

    /**
     * Drops colour and formatting from a subtree, used by {@code <clear_color>}.
     */
    public static TextNode removeColors(TextNode node) {
        if (node instanceof ColorNode || node instanceof FormattingNode) {
            return new ParentNode(new TextNode[0]);
        }

        if (node instanceof BooleanStyleNode booleanNode) {
            return new ParentNode(new TextNode[0]);
        }

        if (node instanceof GradientNode) {
            return new ParentNode(new TextNode[0]);
        }

        if (node instanceof StyledNode styledNode) {
            var children = new TextNode[styledNode.getChildren().length];

            for (var i = 0; i < children.length; i++) {
                children[i] = removeColors(styledNode.getChildren()[i]);
            }

            return new StyledNode(children, styledNode.rawStyle().withColor((TextColor) null));
        }

        if (node instanceof ParentTextNode parent) {
            var children = new TextNode[parent.getChildren().length];

            for (var i = 0; i < children.length; i++) {
                children[i] = removeColors(parent.getChildren()[i]);
            }

            return parent.copyWith(children);
        }

        return node;
    }
}