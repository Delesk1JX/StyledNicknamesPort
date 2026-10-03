package dev.sn.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

/**
 * Colour parsing that behaves the same on every targeted game version.
 *
 * <p>{@code TextColor.parseColor} changed its return type between 1.21 and 26.1, so the accepted
 * syntax is reimplemented here instead of being called: {@code #rgb}, {@code #rrggbb} and
 * {@code #rrggbbaa}, plus vanilla colour names and their aliases.
 */
public final class Colors {
    private Colors() {
    }

    public static TextColor parse(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        var value = input.charAt(0) == '#' ? input.substring(1) : null;

        if (value != null) {
            return fromHex(value);
        }

        return fromName(input);
    }

    public static TextColor fromHex(String value) {
        switch (value.length()) {
            case 3 -> {
                int r = digit(value.charAt(0));
                int g = digit(value.charAt(1));
                int b = digit(value.charAt(2));

                if (r < 0 || g < 0 || b < 0) {
                    return null;
                }

                return TextColor.fromRgb((r << 20) | (r << 16) | (g << 12) | (g << 8) | (b << 4) | b);
            }
            case 6, 8 -> {
                long parsed;

                try {
                    parsed = Long.parseLong(value, 16);
                } catch (NumberFormatException e) {
                    return null;
                }

                if (parsed < 0 || parsed > 0xFFFFFFFFL) {
                    return null;
                }

                if (value.length() == 6) {
                    return TextColor.fromRgb((int) (parsed | 0xFF000000L));
                }

                // An eight digit literal carries its own alpha. TextColor has no alpha channel on
                // any targeted version, so the colour part is kept and the alpha is dropped rather
                // than letting an opaque colour be produced from a transparent one.
                return TextColor.fromRgb((int) ((parsed >> 8) | 0xFF000000L));
            }
            default -> {
                return null;
            }
        }
    }

    public static TextColor fromName(String name) {
        var formatting = ChatFormatting.getByName(name);

        if (formatting != null && formatting.getColor() != null) {
            return TextColor.fromRgb(formatting.getColor());
        }

        return ALIASES.get(name.toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * Minecraft spells some colours differently from the mod's own aliases.
     */
    private static final java.util.Map<String, TextColor> ALIASES = buildAliases();

    private static java.util.Map<String, TextColor> buildAliases() {
        var map = new java.util.HashMap<String, TextColor>();

        put(map, "orange", ChatFormatting.GOLD);
        put(map, "grey", ChatFormatting.GRAY);
        put(map, "light_gray", ChatFormatting.GRAY);
        put(map, "light_grey", ChatFormatting.GRAY);
        put(map, "pink", ChatFormatting.LIGHT_PURPLE);
        put(map, "purple", ChatFormatting.DARK_PURPLE);
        put(map, "dark_grey", ChatFormatting.DARK_GRAY);

        return java.util.Collections.unmodifiableMap(map);
    }

    private static void put(java.util.Map<String, TextColor> map, String name, ChatFormatting formatting) {
        if (formatting.getColor() != null) {
            map.put(name, TextColor.fromRgb(formatting.getColor()));
        }
    }

    private static int digit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }

        return -1;
    }
}