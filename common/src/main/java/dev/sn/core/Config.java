package dev.sn.core;

import dev.sn.text.DynamicTextNode;
import dev.sn.text.NodeParser;
import dev.sn.text.ParserContext;
import dev.sn.text.TagLikeParser;
import dev.sn.text.TextNode;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

/**
 * The parsed form of the config. The messages are parsed once at load time and only the nickname
 * placeholder is resolved per render.
 */
public final class Config {
    /**
     * Placeholder the config messages use, for example {@code ${nickname}}.
     */
    public static final ParserContext.Key<Function<String, Component>> KEY = DynamicTextNode.key("styled_nicknames");

    private static final NodeParser MESSAGE_PARSER = NodeParser.builder()
            .simplifiedTextFormat()
            .quickText()
            .placeholders(TagLikeParser.PLACEHOLDER_USER, KEY)
            .staticPreParsing()
            .build();

    public final ConfigData data;

    public final TextNode nicknameFormat;
    public final TextNode nicknameFormatColor;
    public final TextNode changedMessage;
    public final TextNode resetMessage;
    public final Component tooLongMessage;
    public final Component spacesNotAllowedMessage;

    public Config(ConfigData data) {
        this.data = data;

        this.nicknameFormat = MESSAGE_PARSER.parseNode(data.nicknameFormat);
        this.nicknameFormatColor = MESSAGE_PARSER.parseNode(data.nicknameFormatColor);
        this.changedMessage = MESSAGE_PARSER.parseNode(data.nicknameChangedMessage);
        this.resetMessage = MESSAGE_PARSER.parseNode(data.nicknameResetMessage);
        this.tooLongMessage = MESSAGE_PARSER.parseComponent(data.tooLongMessage, ParserContext.of());
        this.spacesNotAllowedMessage = MESSAGE_PARSER.parseComponent(data.nicknameCantContainSpacesMessage, ParserContext.of());
    }

    public Component render(TextNode node, Function<String, Component> nickname) {
        return node.toComponent(ParserContext.of(KEY, nickname));
    }
}