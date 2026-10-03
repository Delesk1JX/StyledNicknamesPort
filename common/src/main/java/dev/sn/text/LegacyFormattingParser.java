package dev.sn.text;

import com.mojang.brigadier.StringReader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the legacy {@code &c} style codes, optionally with the {@code &#rrggbb} extension.
 */
public class LegacyFormattingParser implements NodeParser {
    public static final NodeParser COLORS = new LegacyFormattingParser(true, formats(false));
    public static final NodeParser BASE_COLORS = new LegacyFormattingParser(false, formats(false));
    public static final NodeParser ALL = new LegacyFormattingParser(true, ChatFormatting.values());

    private static ChatFormatting[] formats(boolean colors) {
        return java.util.Arrays.stream(ChatFormatting.values())
                .filter(x -> colors || !x.isColor())
                .toArray(ChatFormatting[]::new);
    }

    private final Map<Character, ChatFormatting> map = new HashMap<>();
    private final boolean allowRgb;

    public LegacyFormattingParser(boolean allowRgb, ChatFormatting... allowedFormatting) {
        this.allowRgb = allowRgb;

        for (var formatting : allowedFormatting) {
            this.map.put(formatting.getChar(), formatting);
        }
    }

    public boolean allowRgb() {
        return this.allowRgb;
    }

    public Collection<ChatFormatting> formatting() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    @Override
    public TextNode[] parseNodes(TextNode input) {
        return this.parseNodes(input, new ArrayList<>());
    }

    private TextNode[] parseNodes(TextNode input, List<TextNode> nextNodes) {
        if (input instanceof LiteralNode literal) {
            return this.parseLiteral(literal, nextNodes);
        }

        if (input instanceof ParentTextNode parent) {
            var list = new ArrayList<TextNode>();

            if (parent.getChildren().length > 0) {
                var nodes = new ArrayList<>(List.of(parent.getChildren()));

                while (!nodes.isEmpty()) {
                    list.add(TextNode.asSingle(this.parseNodes(nodes.remove(0), nodes)));
                }
            }

            return new TextNode[]{parent.copyWith(list.toArray(new TextNode[0]), this)};
        }

        return new TextNode[]{input};
    }

    private TextNode[] parseLiteral(LiteralNode literalNode, List<TextNode> nexts) {
        var builder = new StringBuilder();
        var reader = new StringReader(literalNode.value());

        while (reader.canRead(2)) {
            var i = reader.read();

            if (i == '\\') {
                i = reader.read();
                builder.append('\\');
                builder.append(i);
            } else if (i == '&') {
                i = reader.read();

                if (this.allowRgb && i == '#' && reader.canRead(6)) {
                    var start = reader.getCursor();

                    try {
                        var hex = new StringBuilder();

                        for (var z = 0; z < 6; z++) {
                            hex.append(reader.read());
                        }

                        var rgb = (int) Long.parseLong(hex.toString(), 16);
                        var list = new ArrayList<>(nexts);
                        nexts.clear();

                        var base = TextNode.asSingle(this.parseLiteral(new LiteralNode(reader.getRemaining()), list));
                        list.add(0, base);

                        return new TextNode[]{
                                new LiteralNode(builder.toString()),
                                new ColorNode(list.toArray(new TextNode[0]), TextColor.fromRgb(rgb))
                        };
                    } catch (Exception e) {
                        reader.setCursor(start);
                    }
                }

                var formatting = this.map.get(i);

                if (formatting != null) {
                    var list = new ArrayList<>(nexts);
                    nexts.clear();

                    var base = TextNode.asSingle(this.parseLiteral(new LiteralNode(reader.getRemaining()), list));
                    list.add(0, base);

                    return new TextNode[]{
                            new LiteralNode(builder.toString()),
                            new FormattingNode(list.toArray(new TextNode[0]), formatting)
                    };
                }

                builder.append('&');
            }

            builder.append(i);
        }

        return new TextNode[]{literalNode};
    }
}