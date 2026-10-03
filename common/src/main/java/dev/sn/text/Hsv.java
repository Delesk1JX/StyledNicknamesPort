package dev.sn.text;

/**
 * HSV colour space, used by rainbow and HSV gradients.
 */
public record Hsv(float h, float s, float v) {
    public static Hsv fromRgb(int rgb) {
        var b = (float) (rgb & 0xFF) / 255;
        rgb >>= 8;
        var g = (float) (rgb & 0xFF) / 255;
        rgb >>= 8;
        var r = (float) (rgb & 0xFF) / 255;

        float cmax = Math.max(r, Math.max(g, b));
        float cmin = Math.min(r, Math.min(g, b));
        float diff = cmax - cmin;
        float h;
        float s;

        if (cmax == cmin) {
            h = 0;
        } else if (cmax == r) {
            h = (0.1666f * ((g - b) / diff) + 1) % 1;
        } else if (cmax == g) {
            h = (0.1666f * ((b - r) / diff) + 0.333f) % 1;
        } else {
            h = (0.1666f * ((r - g) / diff) + 0.666f) % 1;
        }

        s = cmax == 0 ? 0 : diff / cmax;

        return new Hsv(h, s, cmax);
    }

    public static int toRgb(float hue, float saturation, float value) {
        int h = (int) (hue * 6) % 6;
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h) {
            case 0 -> {
                return TextUtils.rgbToInt(value, t, p);
            }
            case 1 -> {
                return TextUtils.rgbToInt(q, value, p);
            }
            case 2 -> {
                return TextUtils.rgbToInt(p, value, t);
            }
            case 3 -> {
                return TextUtils.rgbToInt(p, q, value);
            }
            case 4 -> {
                return TextUtils.rgbToInt(t, p, value);
            }
            case 5 -> {
                return TextUtils.rgbToInt(value, p, q);
            }
            default -> {
                return 0;
            }
        }
    }
}