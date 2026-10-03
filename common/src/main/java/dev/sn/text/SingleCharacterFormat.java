package dev.sn.text;

/**
 * QuickText style: {@code <tag>} or {@code <tag:argument>}.
 */
public record SingleCharacterFormat(char start, char end, char argument, char[] argumentWrappers) implements TagFormat {
    public SingleCharacterFormat(char start, char end, char argument) {
        this(start, end, argument, SimpleArguments.DEFAULT_WRAPPERS);
    }

    public SingleCharacterFormat(char start, char end) {
        this(start, end, (char) 0, SimpleArguments.DEFAULT_WRAPPERS);
    }

    @Override
    public int matchStart(String string, int index) {
        return string.charAt(index) == this.start ? 1 : 0;
    }

    @Override
    public int matchEnd(String string, int index) {
        return string.charAt(index) == this.end ? 1 : 0;
    }

    @Override
    public int matchArgument(String string, int index) {
        return string.charAt(index) == this.argument ? 1 : 0;
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
        return this.argument != 0;
    }
}