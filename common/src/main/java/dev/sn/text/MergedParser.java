package dev.sn.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a list of parsers one after another, merging the consecutive tag parsers into a single
 * multi syntax parser so that they do not fight over the same characters.
 */
public final class MergedParser implements NodeParser {
    private final NodeParser[] parsers;

    public MergedParser(NodeParser... parsers) {
        var list = new ArrayList<NodeParser>(parsers.length);
        var combiner = new ArrayList<MultiTagLikeParser.Pair>(4);

        for (var parser : parsers) {
            if (parser instanceof SingleTagLikeParser single) {
                combiner.add(new MultiTagLikeParser.Pair(single.format(), single.provider()));
                continue;
            }

            if (combiner.size() == 1) {
                list.add(new SingleTagLikeParser(combiner.get(0).format(), combiner.get(0).provider()));
                combiner.clear();
            } else if (combiner.size() > 1) {
                list.add(new MultiTagLikeParser(combiner));
                combiner.clear();
            }

            list.add(parser);
        }

        if (combiner.size() == 1) {
            list.add(new SingleTagLikeParser(combiner.get(0).format(), combiner.get(0).provider()));
        } else if (combiner.size() > 1) {
            list.add(new MultiTagLikeParser(combiner));
        }

        this.parsers = list.toArray(new NodeParser[0]);
    }

    public List<NodeParser> parsers() {
        return List.of(this.parsers);
    }

    @Override
    public TextNode[] parseNodes(TextNode input) {
        var out = new TextNode[]{input};

        for (var parser : this.parsers) {
            out = parser.parseNodes(TextNode.asSingle(out));
        }

        return out;
    }
}