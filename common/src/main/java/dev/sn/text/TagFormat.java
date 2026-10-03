package dev.sn.text;

/**
 * Describes the delimiters of one tag syntax, for example {@code <...>} with a {@code :} separated
 * argument, or the QuickText variant which also accepts a space.
 */
public interface TagFormat {
    /** Result of a successful match. */
    record Tag(int start, int end, String id, String argument) {
    }

    /**
     * @return how many characters of a tag were matched at {@code index}, or 0 when there is no tag
     */
    int matchStart(String string, int index);

    int matchEnd(String string, int index);

    int matchArgument(String string, int index);

    char[] argumentWrappers();

    int endLength();

    boolean hasArgument();

    /**
     * Searches for the next valid tag, skipping escaped characters.
     */
    default Tag findFirst(String string, int start, TagLikeParser.Provider provider, TagLikeParser.Context context) {
        int maxLength = string.length();

        for (var i = start; i < maxLength; i++) {
            var tag = this.findAt(string, i, provider, context);

            if (tag != null) {
                return tag;
            }

            if (string.charAt(i) == '\\' && maxLength > i + 1) {
                i++;
            }
        }

        return null;
    }

    default Tag findAt(String string, int start, TagLikeParser.Provider provider, TagLikeParser.Context context) {
        if (string.charAt(start) == '\\') {
            return null;
        }

        var startLength = this.matchStart(string, start);

        if (startLength == 0) {
            return null;
        }

        String id = null;
        String argument = "";

        char wrapper = 0;
        var builder = new StringBuilder();
        int maxLengthEnd = string.length();

        validationLoop:
        for (var b = start + startLength; b < maxLengthEnd; b++) {
            var curr = string.charAt(b);
            int arg = 0;

            if (wrapper != 0) {
                if (curr == wrapper) {
                    wrapper = 0;
                }

                builder.append(curr);
                continue;
            }

            if (curr == '\\') {
                if (b + 1 < string.length()) {
                    b++;
                    builder.append(string.charAt(b));
                }

                continue;
            }

            if (id != null) {
                for (var argumentWrapper : this.argumentWrappers()) {
                    if (curr == argumentWrapper) {
                        builder.append(curr);
                        wrapper = curr;
                        continue validationLoop;
                    }
                }
            }

            if (id == null && this.hasArgument()) {
                arg = this.matchArgument(string, b);
            }

            int end = 0;

            // Either the argument separator or the closing delimiter ends the current part. A
            // character that matches neither is just more of the id, so it must not veto the
            // match that the following checks perform.
            if (arg == 0) {
                end = this.matchEnd(string, b);
            }

            if (arg != 0 || end != 0) {
                var value = builder.toString();

                if (id == null) {
                    if (!provider.isValidTag(value, context)) {
                        return null;
                    }

                    id = value;
                    builder = new StringBuilder();

                    if (end == 0) {
                        continue;
                    }
                } else {
                    argument = value;
                }

                return new Tag(start, b + end, id, argument);
            }

            builder.append(curr);
        }

        return null;
    }

    /**
     * Order used to pick the earliest tag when several syntaxes are active at once.
     */
    default int index() {
        return 0;
    }

    static TagFormat of(char start, char end, char argument) {
        return new SingleCharacterFormat(start, end, argument);
    }

    static TagFormat of(String start, String end, String argument) {
        return new MultiCharacterFormat(start, end, argument);
    }
}