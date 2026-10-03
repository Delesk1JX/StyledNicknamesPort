package dev.sn.text;

/**
 * Simplified Text Format: identical to QuickText except that the argument may be separated with a
 * space as well as with a colon, and that only quotes count as argument wrappers.
 */
public record LegacyFormat() implements TagFormat {
    public static final LegacyFormat INSTANCE = new LegacyFormat();

    @Override
    public int matchStart(String string, int index) {
        return string.charAt(index) == '<' ? 1 : 0;
    }

    @Override
    public int matchEnd(String string, int index) {
        return string.charAt(index) == '>' ? 1 : 0;
    }

    @Override
    public int matchArgument(String string, int index) {
        var c = string.charAt(index);
        return c == ':' || c == ' ' ? 1 : 0;
    }

    @Override
    public char[] argumentWrappers() {
        return SimpleArguments.LEGACY_WRAPPERS;
    }

    @Override
    public int endLength() {
        return 1;
    }

    @Override
    public int index() {
        return -1;
    }

    @Override
    public boolean hasArgument() {
        return true;
    }
}