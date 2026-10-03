package dev.sn.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for several tag syntaxes at once, which is needed because a parser chain can contribute
 * both formatting tags and {@code ${...}} placeholders.
 */
public class MultiTagLikeParser extends TagLikeParser {
    /** A format and the provider that handles it. */
    public record Pair(TagFormat format, Provider provider) {
    }

    private final Pair[] pairs;

    public MultiTagLikeParser(List<Pair> formatsAndProviders) {
        var copy = new ArrayList<>(formatsAndProviders);
        copy.sort((a, b) -> Integer.compare(a.format().index(), b.format().index()));
        this.pairs = copy.toArray(new Pair[0]);
    }

    public List<Pair> pairs() {
        return List.of(this.pairs);
    }

    @Override
    protected void handleLiteral(String value, Context context) {
        var pos = 0;

        while (pos != -1) {
            var tagPos = pos;
            Provider provider = null;
            TagFormat.Tag tag = null;

            // Advance over ordinary characters until some syntax claims a tag. The longest match
            // wins, and on a tie the format declared first does, so that <color:lime> is taken as a
            // QuickText tag with an argument rather than as an STF tag that stops at the colon.
            while (tagPos < value.length()) {
                var foundHere = false;

                for (var pair : this.pairs) {
                    var found = pair.format().findAt(value, tagPos, pair.provider(), context);

                    if (found == null) {
                        continue;
                    }

                    if (tag == null || found.start() < tag.start()
                            || (found.start() == tag.start() && found.end() > tag.end())) {
                        provider = pair.provider();
                        tag = found;
                        foundHere = true;
                    }
                }

                if (tag != null && foundHere) {
                    break;
                }

                if (tag != null) {
                    // This syntax already matched further ahead than the position we are scanning,
                    // so skip ahead instead of letting another syntax win a tie further left.
                    tagPos++;
                    continue;
                }

                if (value.charAt(tagPos) == '\\' && value.length() > tagPos + 1) {
                    tagPos++;
                }

                tagPos++;
            }

            if (provider != null && tag != null) {
                pos = this.handleTag(value, pos, tag, provider, context);
            } else {
                context.addNode(new LiteralNode(value.substring(pos)));
                pos = -1;
            }
        }
    }
}