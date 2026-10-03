package dev.sn.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Per character colour, used by {@code <rainbow>} and {@code <gradient>}.
 */
public final class GradientNode extends ParentNode {
    private final GradientProvider provider;

    public GradientNode(TextNode[] children, GradientProvider provider) {
        super(children);
        this.provider = provider;
    }

    public static GradientNode rainbow(TextNode... nodes) {
        return rainbow(1, 1, 1, 0, nodes);
    }

    public static GradientNode rainbow(float saturation, float value, float frequency, float offset, TextNode... nodes) {
        return new GradientNode(nodes, GradientProvider.rainbow(saturation, value, frequency, offset));
    }

    public static GradientNode colors(List<TextColor> colors, TextNode... nodes) {
        return new GradientNode(nodes, GradientProvider.colors(colors));
    }

    public static GradientNode colorsHard(List<TextColor> colors, TextNode... nodes) {
        return new GradientNode(nodes, GradientProvider.colorsHard(colors));
    }

    @Override
    protected Component applyFormatting(MutableComponent out, ParserContext context) {
        return TextUtils.toGradient(out, this.provider);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new GradientNode(children, this.provider);
    }

    @Override
    public String toString() {
        return "GradientNode{provider=" + this.provider + ", children=" + Arrays.toString(getChildren()) + "}";
    }

    @FunctionalInterface
    public interface GradientProvider {
        TextColor getColorAt(int index, int length);

        static GradientProvider colors(List<TextColor> colors) {
            return colorsOkLab(colors);
        }

        static GradientProvider colorsOkLab(List<TextColor> colors) {
            var lab = new ArrayList<OkLab>(colors.size());

            for (var color : colors) {
                lab.add(OkLab.fromRgb(color.getValue()));
            }

            if (lab.isEmpty()) {
                lab.add(new OkLab(1, 1, 1));
            } else if (lab.size() == 1) {
                lab.add(lab.get(0));
            }

            final int colorSize = lab.size();

            return (pos, length) -> {
                final float sectionSize = ((float) length) / (colorSize - 1);
                final float progress = (pos % sectionSize) / sectionSize;

                var colorA = lab.get(Math.min((int) (pos / sectionSize), colorSize - 1));
                var colorB = lab.get(Math.min((int) (pos / sectionSize) + 1, colorSize - 1));

                float l = Mth.lerp(progress, colorA.l(), colorB.l());
                float a = Mth.lerp(progress, colorA.a(), colorB.a());
                float b = Mth.lerp(progress, colorA.b(), colorB.b());

                return TextColor.fromRgb(OkLab.toRgb(l, a, b));
            };
        }

        static GradientProvider colorsHvs(List<TextColor> colors) {
            var hsv = new ArrayList<Hsv>(colors.size());

            for (var color : colors) {
                hsv.add(Hsv.fromRgb(color.getValue()));
            }

            if (hsv.isEmpty()) {
                hsv.add(new Hsv(1, 1, 1));
            } else if (hsv.size() == 1) {
                hsv.add(hsv.get(0));
            }

            final int colorSize = hsv.size();

            return (pos, length) -> {
                final float step = (((float) colorSize) - 1) / length;
                final float sectionSize = ((float) length) / (colorSize - 1);
                final float progress = (pos % sectionSize) / sectionSize;

                var colorA = hsv.get(Math.min((int) (pos / sectionSize), colorSize - 1));
                var colorB = hsv.get(Math.min((int) (pos / sectionSize) + 1, colorSize - 1));

                float h = colorB.h() - colorA.h();
                float delta = h + ((Math.abs(h) > 0.50001f) ? ((h < 0) ? 1 : -1) : 0);

                var futureHue = colorA.h() + delta * step * (pos % sectionSize);

                if (futureHue < 0) {
                    futureHue += 1;
                } else if (futureHue > 1) {
                    futureHue -= 1;
                }

                float sat = Mth.clamp(colorB.s() * progress + colorA.s() * (1 - progress), 0, 1);
                float value = Mth.clamp(colorB.v() * progress + colorA.v() * (1 - progress), 0, 1);

                return TextColor.fromRgb(Hsv.toRgb(Mth.clamp(futureHue, 0, 1), sat, value));
            };
        }

        static GradientProvider colorsHard(List<TextColor> colors) {
            final int colorSize = colors.size();

            return (pos, length) -> {
                if (length == 0) {
                    return colors.get(0);
                }

                final float sectionSize = ((float) length) / colorSize;
                return colors.get(Math.min((int) (pos / sectionSize), colorSize - 1));
            };
        }

        static GradientProvider rainbow(float saturation, float value, float frequency, float offset) {
            final float negativeFrequency = frequency < 0 ? -frequency : 0;

            return (pos, length) -> TextColor.fromRgb(Hsv.toRgb(
                    (((pos * frequency) + (negativeFrequency * length)) / (length + 1) + offset),
                    saturation,
                    value));
        }

        static GradientProvider rainbowOkLch(float saturation, float value, float frequency, float offset, int gradientLength) {
            final float negativeFrequency = frequency < 0 ? -frequency : 0;

            return (pos, length) -> TextColor.fromRgb(OkLab.toRgb(
                    value,
                    saturation / 2,
                    (((pos * frequency * Mth.TWO_PI) + (negativeFrequency * length)) / (gradientLength + 1) + offset) % 1));
        }
    }
}