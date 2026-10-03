package dev.sn.text;

/**
 * Tag syntax with multi character delimiters, used by the {@code ${...}} placeholder format.
 */
public record MultiCharacterFormat(char[] start, char[] end, char[] argument, char[] argumentWrappers) implements TagFormat {
    public MultiCharacterFormat(String start, String end, String argument) {
        this(start.toCharArray(), end.toCharArray(), argument.toCharArray(), SimpleArguments.DEFAULT_WRAPPERS);
    }

    @Override
    public int matchStart(String string, int index) {
        for (var a = 0; a < this.start.length; a++) {
            if (string.charAt(index + a) != this.start[a]) {
                return 0;
            }
        }

        return this.start.length;
    }

    @Override
    public int matchEnd(String string, int index) {
        for (var a = 0; a < this.end.length; a++) {
            if (string.charAt(index + a) != this.end[a]) {
                return 0;
            }
        }

        return this.end.length;
    }

    @Override
    public int matchArgument(String string, int index) {
        if (this.argument.length == 0) {
            return 0;
        }

        for (var a = 0; a < this.argument.length; a++) {
            if (string.charAt(index + a) != this.argument[a]) {
                return 0;
            }
        }

        return this.argument.length;
    }

    @Override
    public int endLength() {
        return this.end.length;
    }

    @Override
    public int index() {
        return -this.start.length;
    }

    @Override
    public boolean hasArgument() {
        return this.argument.length != 0;
    }
}