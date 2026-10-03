package dev.sn.text;

import net.minecraft.util.Mth;

/**
 * OkLab colour space. Unlike plain RGB interpolation this keeps perceived brightness roughly
 * constant, which is what the default {@code <gradient>} uses.
 *
 * @see <a href="https://bottosson.github.io/posts/oklab/">OkLab</a>
 */
public record OkLab(float l, float a, float b) {
    public static OkLab fromRgb(int rgb) {
        return fromLinearSrgb(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
    }

    private static OkLab fromLinearSrgb(float r, float g, float b) {
        float l = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b;
        float m = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b;
        float s = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b;

        float lCbrt = (float) Math.cbrt(l);
        float mCbrt = (float) Math.cbrt(m);
        float sCbrt = (float) Math.cbrt(s);

        return new OkLab(
                0.2104542553f * lCbrt + 0.7936177850f * mCbrt - 0.0040720468f * sCbrt,
                1.9779984951f * lCbrt - 2.4285922050f * mCbrt + 0.4505937099f * sCbrt,
                0.0259040371f * lCbrt + 0.7827717662f * mCbrt - 0.8086757660f * sCbrt
        );
    }

    public static int toRgb(float cL, float ca, float cb) {
        float l_ = cL + 0.3963377774f * ca + 0.2158037573f * cb;
        float m_ = cL - 0.1055613458f * ca - 0.0638541728f * cb;
        float s_ = cL - 0.0894841775f * ca - 1.2914855480f * cb;

        float l = l_ * l_ * l_;
        float m = m_ * m_ * m_;
        float s = s_ * s_ * s_;

        var r = 4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
        var g = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
        var b = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;

        return TextUtils.rgbToInt(
                Mth.clamp(r, 0, 1),
                Mth.clamp(g, 0, 1),
                Mth.clamp(b, 0, 1)
        );
    }
}