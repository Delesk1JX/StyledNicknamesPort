package dev.sn.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the parts of the parser the mod's own formatting depends on. Everything is checked through
 * the rendered component, because that is what a player actually sees.
 */
class TextParserTest {
    private static NodeParser parser() {
        return NodeParser.builder()
                .simplifiedTextFormat()
                .quickText()
                .customTagRegistry(TagRegistry.createSafe())
                .build();
    }

    /**
     * A styled segment is rendered as the component itself when it is the only one, and as a
     * sibling when the surrounding text follows it. Both forms are found here so that the tests do
     * not depend on which one a given input produces.
     */
    private static Style styleOf(String input) {
        return effectiveStyle(parser().parseComponent(input, ParserContext.of()));
    }

    private static Style effectiveStyle(Component component) {
        if (!component.getStyle().isEmpty()) {
            return component.getStyle();
        }

        for (var sibling : component.getSiblings()) {
            if (!sibling.getStyle().isEmpty()) {
                return sibling.getStyle();
            }
        }

        return component.getStyle();
    }

    @Test
    void plainTextIsUntouched() {
        assertEquals("Hello", parser().parseComponent("Hello", ParserContext.of()).getString());
    }

    @Test
    void namedColourApplies() {
        assertEquals(0xFF5555, styleOf("<red>Hi").getColor().getValue());
    }

    @Test
    void colourAliasResolves() {
        // "orange" is an alias the vanilla game does not know but the mod format allows.
        assertEquals(TextColor.fromRgb(ChatFormatting.GOLD.getColor()), styleOf("<orange>Hi").getColor());
    }

    @Test
    void hexColourApplies() {
        assertEquals(0xAABBCC, styleOf("<#aabbcc>Hi").getColor().getValue());
    }

    @Test
    void shortHexColourExpands() {
        assertEquals(0xAABBCC, styleOf("<#abc>Hi").getColor().getValue());
    }

    @Test
    void closingTagEndsTheStyling() {
        var component = parser().parseComponent("<red>red</>plain", ParserContext.of());

        assertEquals("redplain", component.getString());

        // The styled part is red and the trailing part is not.
        assertEquals(0xFF5555, effectiveStyle(component).getColor().getValue());
    }

    @Test
    void formattingFlagsApply() {
        assertTrue(styleOf("<bold>Hi").isBold());
        assertTrue(styleOf("<italic>Hi").isItalic());
        assertTrue(styleOf("<u>Hi").isUnderlined());
        assertTrue(styleOf("<st>Hi").isStrikethrough());
        assertTrue(styleOf("<obf>Hi").isObfuscated());
    }

    @Test
    void formattingFlagCanBeTurnedOff() {
        assertFalse(styleOf("<bold:value=false>Hi").isBold());
    }

    @Test
    void nestedColoursKeepTheirText() {
        var component = parser().parseComponent("<red>a<blue>b</>c</>d", ParserContext.of());

        assertEquals("abcd", component.getString());
    }

    @Test
    void resetTagClearsEverything() {
        var component = parser().parseComponent("<red><bold>x</>y", ParserContext.of());

        assertEquals("xy", component.getString());
    }

    @Test
    void legacyCodesParse() {
        var parser = NodeParser.builder().simplifiedTextFormat().quickText().legacyAll().build();
        var component = parser.parseComponent("&cRed&r Plain", ParserContext.of());

        assertEquals("Red Plain", component.getString());
        assertEquals(0xFF5555, effectiveStyle(component).getColor().getValue());
    }

    @Test
    void legacyRgbParses() {
        var parser = NodeParser.builder().simplifiedTextFormat().quickText().legacy(true,
                List.of(ChatFormatting.RED)).build();
        var component = parser.parseComponent("&#ff0000x", ParserContext.of());

        assertEquals("x", component.getString());
        assertEquals(0xFF0000, effectiveStyle(component).getColor().getValue());
    }

    @Test
    void gradientColoursEveryCharacter() {
        var component = parser().parseComponent("<gradient:#ff0000:#0000ff>abc", ParserContext.of());

        assertEquals("abc", component.getString());

        // Every character gets its own colour.
        var colors = new java.util.ArrayList<Integer>();
        collectColors(component, colors);
        assertEquals(3, colors.size());

        // The gradient runs from red to blue. OkLab interpolation is perceptual rather than
        // linear, so the endpoints are compared on their hue rather than on an exact value.
        int first = colors.get(0);
        int last = colors.get(2);

        assertTrue((first >> 16 & 0xFF) > (first & 0xFF), "first colour should be reddish: " + Integer.toHexString(first));
        assertTrue((last & 0xFF) > (last >> 16 & 0xFF), "last colour should be bluish: " + Integer.toHexString(last));
    }

    private static void collectColors(Component component, List<Integer> out) {
        if (component.getStyle().getColor() != null) {
            out.add(component.getStyle().getColor().getValue());
        }

        for (var sibling : component.getSiblings()) {
            collectColors(sibling, out);
        }
    }

    @Test
    void rainbowProducesColours() {
        var component = parser().parseComponent("<rainbow>abc", ParserContext.of());

        assertEquals("abc", component.getString());

        var colors = new java.util.ArrayList<Integer>();
        collectColors(component, colors);

        assertEquals(3, colors.size());
    }

    @Test
    void unknownTagStaysLiteral() {
        assertEquals("<not_a_tag>x", parser().parseComponent("<not_a_tag>x", ParserContext.of()).getString());
    }

    @Test
    void restrictedRegistryRejectsForbiddenTag() {
        var registry = TagRegistry.createSafe();
        registry.remove(registry.getTag("red"));

        var parser = NodeParser.builder().simplifiedTextFormat().quickText().customTagRegistry(registry).build();
        var component = parser.parseComponent("<red>Hi", ParserContext.of());

        // With the tag gone the text must survive as literal text instead of being styled.
        assertEquals("<red>Hi", component.getString());
        assertNull(effectiveStyle(component).getColor());
    }

    @Test
    void insertAndClickAreRendered() {
        assertEquals("me", styleOf("<insert:me>x").getInsertion());
        assertNotNull(styleOf("<open_url:https://example.com>x").getClickEvent());
    }

    @Test
    void hoverTextIsRendered() {
        assertNotNull(styleOf("<hover:tip>x").getHoverEvent());
    }

    @Test
    void placeholderResolvesFromContext() {
        var key = DynamicTextNode.key("nickname");

        var parser = NodeParser.builder()
                .simplifiedTextFormat()
                .quickText()
                .placeholders(TagLikeParser.PLACEHOLDER_USER, key)
                .build();

        Function<String, Component> resolver = id -> Component.literal("Nick:" + id);
        var context = ParserContext.of(key, resolver);

        assertEquals("Nick:nickname", parser.parseComponent("${nickname}", context).getString());
    }

    @Test
    void escapedTagStaysLiteral() {
        assertEquals("<red>text", parser().parseComponent("\\<red\\>text", ParserContext.of()).getString());
    }

    @Test
    void colorTagParsesItsArgument() {
        // Vanilla spells light green "green"; there is no "lime".
        assertEquals(0x55FF55, styleOf("<color:green>Hi").getColor().getValue());
        assertEquals(0x123456, styleOf("<color:#123456>Hi").getColor().getValue());
    }

    @Test
    void clearColorRemovesColour() {
        assertNull(styleOf("<clear_color>text").getColor());
    }

    @Test
    void staticPreParserFreezesStaticParts() {
        var parser = NodeParser.builder().simplifiedTextFormat().quickText().staticPreParsing().build();
        var node = parser.parseNode("<red>Hi");

        assertTrue(node instanceof DirectComponentNode);
        assertFalse(node.isDynamic());
    }
}