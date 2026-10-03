package dev.sn.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Assembles the parsers a mod needs, in the order they should run.
 */
public class ParserBuilder {
    private final Map<TagFormat, TagLikeParser.Provider> tagLike = new LinkedHashMap<>();
    private final List<NodeParser> parserList = new ArrayList<>();
    private final List<ChatFormatting> legacyFormatting = new ArrayList<>();

    private boolean hasLegacy = false;
    private boolean legacyRgb = false;
    private boolean simplifiedTextFormat = false;
    private boolean quickText = false;
    private boolean staticPreParsing = false;
    private TagRegistry customTagRegistry = null;

    public static ParserBuilder of() {
        return new ParserBuilder();
    }

    /**
     * Enables the Simplified Text Format syntax.
     */
    public ParserBuilder simplifiedTextFormat() {
        this.simplifiedTextFormat = true;
        return this;
    }

    /**
     * Enables the QuickText syntax. Combined with {@link #simplifiedTextFormat()} both are accepted.
     */
    public ParserBuilder quickText() {
        this.quickText = true;
        return this;
    }

    /**
     * Restricts the parser to the given tags, which is how per player permissions are enforced.
     */
    public ParserBuilder customTagRegistry(TagRegistry registry) {
        this.customTagRegistry = registry;
        return this;
    }

    public ParserBuilder legacyColor() {
        return this.add(LegacyFormattingParser.COLORS);
    }

    public ParserBuilder legacyVanillaColor() {
        return this.add(LegacyFormattingParser.BASE_COLORS);
    }

    public ParserBuilder legacyAll() {
        return this.add(LegacyFormattingParser.ALL);
    }

    public ParserBuilder legacy(boolean allowRgb, ChatFormatting... formatting) {
        this.hasLegacy = true;
        this.legacyRgb = allowRgb;
        this.legacyFormatting.addAll(List.of(formatting));
        return this;
    }

    public ParserBuilder legacy(boolean allowRgb, List<ChatFormatting> formatting) {
        this.hasLegacy = true;
        this.legacyRgb = allowRgb;
        this.legacyFormatting.addAll(formatting);
        return this;
    }

    /**
     * Resolves {@code ${...}} placeholders from a context value.
     */
    public ParserBuilder placeholders(TagFormat format, ParserContext.Key<Function<String, Component>> key) {
        this.tagLike.put(format, TagLikeParser.Provider.placeholder(key));
        return this;
    }

    public ParserBuilder customTags(TagFormat format, TagLikeParser.Provider provider) {
        this.tagLike.put(format, provider);
        return this;
    }

    /**
     * Renders the static parts of the result once, so that repeated rendering is cheaper.
     */
    public ParserBuilder staticPreParsing() {
        this.staticPreParsing = true;
        return this;
    }

    public ParserBuilder add(NodeParser parser) {
        if (parser instanceof LegacyFormattingParser legacy) {
            this.hasLegacy = true;
            this.legacyFormatting.addAll(legacy.formatting());
            this.legacyRgb |= legacy.allowRgb();
        }

        this.parserList.add(parser);
        return this;
    }

    public NodeParser build() {
        var list = new ArrayList<NodeParser>(this.parserList.size() + 3);

        if (!this.tagLike.isEmpty()) {
            list.add(new MultiTagLikeParser(this.tagLike.entrySet().stream()
                    .map(entry -> new MultiTagLikeParser.Pair(entry.getKey(), entry.getValue()))
                    .toList()));
        }

        var registry = this.customTagRegistry != null ? this.customTagRegistry : TagRegistry.defaults();

        if (this.quickText && this.simplifiedTextFormat) {
            list.add(TagParser.quickText(registry));
            list.add(TagParser.simplifiedTextFormat(registry));
        } else if (this.quickText) {
            list.add(TagParser.quickText(registry));
        } else if (this.simplifiedTextFormat) {
            list.add(TagParser.simplifiedTextFormat(registry));
        }

        list.addAll(this.parserList);

        if (this.hasLegacy) {
            list.add(new LegacyFormattingParser(this.legacyRgb, this.legacyFormatting.toArray(new ChatFormatting[0])));
        }

        if (this.staticPreParsing) {
            list.add(StaticPreParser.INSTANCE);
        }

        return NodeParser.merge(list);
    }
}