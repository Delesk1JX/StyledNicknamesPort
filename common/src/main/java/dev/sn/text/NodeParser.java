package dev.sn.text;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Turns a node tree into another node tree. Parsers are chained: the builder merges them and each
 * one gets a chance to rewrite what the previous ones produced.
 */
public interface NodeParser {
    NodeParser NOOP = input -> new TextNode[]{input};

    TextNode[] parseNodes(TextNode input);

    default TextNode parseNode(TextNode input) {
        return TextNode.asSingle(this.parseNodes(input));
    }

    default TextNode parseNode(String input) {
        return this.parseNode(TextNode.of(input));
    }

    default Component parseComponent(String input, ParserContext context) {
        return TextNode.asSingle(this.parseNodes(TextNode.of(input))).toComponent(context, true);
    }

    static NodeParser merge(NodeParser... parsers) {
        if (parsers.length == 0) {
            return NOOP;
        } else if (parsers.length == 1) {
            return parsers[0];
        }

        return new MergedParser(parsers);
    }

    static NodeParser merge(List<NodeParser> parsers) {
        return merge(parsers.toArray(new NodeParser[0]));
    }

    static ParserBuilder builder() {
        return new ParserBuilder();
    }
}