package dev.sn.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Small helpers for reading user supplied tag arguments.
 */
public final class SimpleArguments {
    public static final char[] DEFAULT_WRAPPERS = new char[]{'"', '\'', '`'};
    public static final char[] LEGACY_WRAPPERS = new char[]{'\''};

    private SimpleArguments() {
    }

    public static boolean isWrapCharacter(char c) {
        return c == '"' || c == '\'' || c == '`';
    }

    public static boolean isWrapCharacter(char c, char[] wrappers) {
        for (var wrapper : wrappers) {
            if (wrapper == c) {
                return true;
            }
        }

        return false;
    }

    public static String unwrap(String string, char[] wrappers) {
        if (string.length() < 2) {
            return string;
        }

        var c1 = string.charAt(0);
        var c2 = string.charAt(string.length() - 1);

        if (c1 == c2 && isWrapCharacter(c1, wrappers)) {
            var builder = new StringBuilder(string.length() - 2);

            for (var i = 1; i < string.length() - 1; i++) {
                var chr = string.charAt(i);

                if (chr == c1 && string.charAt(i + 1) == c1) {
                    i++;
                }

                builder.append(chr);
            }

            return builder.toString();
        }

        return string;
    }

    public static List<String> split(String string, char separator) {
        return split(string, separator, true, true);
    }

    public static List<String> split(String string, char separator, boolean removeWrapping, boolean removeBackslash) {
        var list = new ArrayList<String>();
        var builder = new StringBuilder();

        char wrap = 0;

        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);

            if (character == '\\') {
                if (!removeBackslash) {
                    builder.append(character);
                }

                if (i + 1 < string.length()) {
                    builder.append(string.charAt(i + 1));
                    i++;
                }

                continue;
            }

            if (character == separator && wrap == 0) {
                list.add(builder.toString());
                builder = new StringBuilder();
                continue;
            }

            if (wrap == character && wrap != 0) {
                if (i + 1 >= string.length() || string.charAt(i + 1) != wrap) {
                    wrap = 0;

                    if (removeWrapping) {
                        continue;
                    }
                } else if (removeWrapping) {
                    i++;
                }
            } else if (wrap == 0 && isWrapCharacter(character)) {
                wrap = character;

                if (removeWrapping) {
                    continue;
                }
            }

            builder.append(character);
        }

        if (!builder.isEmpty()) {
            list.add(builder.toString());
        }

        return list;
    }

    public static boolean bool(String arg) {
        return bool(arg, false);
    }

    public static boolean bool(String arg, boolean defaultBool) {
        if (arg == null || arg.isBlank()) {
            return defaultBool;
        }

        switch (arg.toLowerCase(Locale.ROOT)) {
            case "true", "tru", "yes", "y", "1", "enabled", "enable", "on" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public static float floatNumber(String arg) {
        return floatNumber(arg, 0);
    }

    public static float floatNumber(String arg, float defaultFloat) {
        if (arg == null || arg.isBlank()) {
            return defaultFloat;
        }

        try {
            return Float.parseFloat(arg);
        } catch (Exception e) {
            return defaultFloat;
        }
    }

    public static int intNumber(String arg) {
        return intNumber(arg, 0);
    }

    public static int intNumber(String arg, int defaultInt) {
        if (arg == null || arg.isBlank()) {
            return defaultInt;
        }

        try {
            return Integer.parseInt(arg);
        } catch (Exception e) {
            return defaultInt;
        }
    }
}