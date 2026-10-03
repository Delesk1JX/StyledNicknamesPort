package dev.sn.text;

/**
 * A formatting parser for one tag syntax, paired with the provider that turns tags into nodes.
 */
public class TagParser implements NodeParser {
    private final TagLikeParser parser;

    public TagParser(TagFormat format, TagLikeParser.Provider provider) {
        this.parser = new SingleTagLikeParser(format, provider);
    }

    public static TagParser quickText(TagRegistry registry) {
        return new TagParser(TagLikeParser.TAGS, new ModernProvider(registry));
    }

    public static TagParser simplifiedTextFormat(TagRegistry registry) {
        return new TagParser(TagLikeParser.TAGS_LEGACY, new LegacyProvider(registry));
    }

    public TagLikeParser asTagLikeParser() {
        return this.parser;
    }

    @Override
    public TextNode[] parseNodes(TextNode input) {
        return this.parser.parseNodes(input);
    }
}