package dev.sn.text;

/**
 * Parser for a single tag syntax.
 */
public class SingleTagLikeParser extends TagLikeParser {
    private final TagFormat format;
    private final Provider provider;

    public SingleTagLikeParser(TagFormat format, Provider provider) {
        this.format = format;
        this.provider = provider;
    }

    public TagFormat format() {
        return this.format;
    }

    public Provider provider() {
        return this.provider;
    }

    @Override
    protected void handleLiteral(String value, Context context) {
        var pos = 0;

        while (pos != -1) {
            pos = this.handleTag(value, pos, this.format.findFirst(value, pos, this.provider, context), this.provider, context);
        }
    }
}