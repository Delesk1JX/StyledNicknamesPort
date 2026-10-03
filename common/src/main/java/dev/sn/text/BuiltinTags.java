package dev.sn.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The tags every registry starts from: the vanilla colours, the formatting flags and the styling
 * tags a nickname can reasonably use.
 *
 * <p>Deliberately absent are the tags that need content introduced after 1.20.1 (player heads, item
 * templates, dialogs, NBT paths), because they cannot be compiled against every target.
 */
final class BuiltinTags {
    private BuiltinTags() {
    }

    static void register(TagRegistry registry) {
        registerColors(registry);
        registerFormatting(registry);
        registerColorTag(registry);
        registerStyling(registry);
        registerGradients(registry);
        registerReset(registry);
    }

    private static void registerColors(TagRegistry registry) {
        Map<ChatFormatting, List<String>> aliases = new HashMap<>();
        aliases.put(ChatFormatting.GOLD, List.of("orange"));
        aliases.put(ChatFormatting.GRAY, List.of("grey", "light_gray", "light_grey"));
        aliases.put(ChatFormatting.LIGHT_PURPLE, List.of("pink"));
        aliases.put(ChatFormatting.DARK_PURPLE, List.of("purple"));
        aliases.put(ChatFormatting.DARK_GRAY, List.of("dark_grey"));

        for (var formatting : ChatFormatting.values()) {
            if (formatting.isFormat() || formatting == ChatFormatting.RESET) {
                continue;
            }

            var color = Colors.fromName(formatting.getName());

            if (color == null) {
                continue;
            }

            registry.register(TextTag.wrap(formatting.getName(), aliases.getOrDefault(formatting, List.of()),
                    "color", true, (nodes, args, parser) -> new ColorNode(nodes, color)));
        }
    }

    private static void registerFormatting(TagRegistry registry) {
        flag(registry, "bold", List.of("b"), ChatFormatting.BOLD);
        flag(registry, "underline", List.of("underlined", "u"), ChatFormatting.UNDERLINE);
        flag(registry, "strikethrough", List.of("st"), ChatFormatting.STRIKETHROUGH);
        flag(registry, "obfuscated", List.of("obf", "matrix"), ChatFormatting.OBFUSCATED);
        flag(registry, "italic", List.of("i", "em"), ChatFormatting.ITALIC);
    }

    private static void flag(TagRegistry registry, String name, List<String> aliases, ChatFormatting formatting) {
        registry.register(TextTag.wrap(name, aliases, "formatting", true,
                NodeCreator.flag((nodes, value) -> new BooleanStyleNode(nodes, formatting, value))));
    }

    private static void registerColorTag(TagRegistry registry) {
        registry.register(TextTag.wrap("color", List.of("colour", "c"), "color", true,
                (nodes, args, parser) -> new DynamicColorNode(nodes, parser.parseNode(args.get("value", 0, "white")))));
    }

    private static void registerStyling(TagRegistry registry) {
        registry.register(TextTag.wrap("click", List.of("custom_click"), "click_action", false,
                (nodes, args, parser) -> {
                    var value = args.getNext("value");

                    if (value == null) {
                        return new ParentNode(nodes);
                    }

                    return new ClickActionNode(nodes, resolveAction(args.getNext("type", "")), parser.parseNode(value));
                }));

        simpleClick(registry, "run_command", List.of("run_cmd"), ClickActionNode.RUN_COMMAND);
        simpleClick(registry, "suggest_command", List.of("cmd"), ClickActionNode.SUGGEST_COMMAND);
        simpleClick(registry, "open_url", List.of("url"), ClickActionNode.OPEN_URL);
        simpleClick(registry, "copy_to_clipboard", List.of("copy"), ClickActionNode.COPY_TO_CLIPBOARD);
        simpleClick(registry, "change_page", List.of("page"), ClickActionNode.CHANGE_PAGE);

        registry.register(TextTag.wrap("hover", "hover_event", true, (nodes, args, parser) -> {
            var type = args.get("type");
            var value = args.getNext("value", "");

            if (type != null) {
                type = type.toLowerCase(Locale.ROOT);

                if (type.equals("show_entity") || type.equals("entity")) {
                    var entityType = args.getNext("entity", "");
                    var uuid = args.getNext("uuid", "");
                    var name = parser.parseNode(args.get("name", 3, ""));

                    return HoverNode.entity(nodes, entityType, uuid, name);
                }            }

            return new HoverNode(nodes, parser.parseNode(value));
        }));

        registry.register(TextTag.wrap("insert", List.of("insertion"), "click_action", false,
                (nodes, args, parser) -> new InsertNode(nodes, parser.parseNode(args.get("value", 0)))));
    }

    private static void simpleClick(TagRegistry registry, String name, List<String> aliases, String action) {
        registry.register(TextTag.wrap(name, aliases, "click_action", false,
                (nodes, args, parser) -> new ClickActionNode(nodes, action, parser.parseNode(args.get("value", 0)))));
    }

    /**
     * Only the actions that exist on every target are accepted, so an untrusted nickname cannot
     * reach server side only clicks.
     */
    private static String resolveAction(String type) {
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "open_url", "url" -> ClickActionNode.OPEN_URL;
            case "run_command", "run_cmd" -> ClickActionNode.RUN_COMMAND;
            case "suggest_command", "cmd" -> ClickActionNode.SUGGEST_COMMAND;
            case "copy_to_clipboard", "copy" -> ClickActionNode.COPY_TO_CLIPBOARD;
            case "change_page", "page" -> ClickActionNode.CHANGE_PAGE;
            default -> ClickActionNode.SUGGEST_COMMAND;
        };
    }

    /**
 * Reads the colour list of a gradient.
 *
 * <p>The colours are separated by colons, which is also what separates a key from its value, so
 * reading them as arguments would turn the first colour into a key and join the rest. The raw
 * argument is therefore split on the separator directly.
 */
private static List<TextColor> gradientColors(String input) {
    var colors = new ArrayList<TextColor>();

    for (var part : SimpleArguments.split(input, ':')) {
        var color = Colors.parse(part);

        if (color != null) {
            colors.add(color);
        }
    }

    return colors;
}

private static void registerGradients(TagRegistry registry) {
        registry.register(TextTag.wrap("rainbow", List.of("rb"), "gradient", true, (nodes, args, parser) -> {
            float saturation = SimpleArguments.floatNumber(args.getNext("saturation", args.get("sat", args.get("s"))), 1);
            float value = SimpleArguments.floatNumber(args.getNext("value", args.get("val", args.get("v"))), 1);
            float frequency = SimpleArguments.floatNumber(args.getNext("frequency", args.get("freq", args.get("f"))), 1);
            float offset = SimpleArguments.floatNumber(args.getNext("offset", args.get("off", args.get("o"))), 0);

            return GradientNode.rainbow(saturation, value, frequency, offset, nodes);
        }));

        registry.register(TextTag.wrap("gradient", List.of("gr"), "gradient", true, (nodes, args, parser) -> {
            var colors = gradientColors(args.input());

            if (colors.isEmpty()) {
                return new ParentNode(nodes);
            }

            var type = args.get("type", "");

            if (type.equals("hvs")) {
                return new GradientNode(nodes, GradientNode.GradientProvider.colorsHvs(colors));
            } else if (type.equals("hard")) {
                return new GradientNode(nodes, GradientNode.GradientProvider.colorsHard(colors));
            }

            return new GradientNode(nodes, GradientNode.GradientProvider.colors(colors));
        }));

        registry.register(TextTag.wrap("hard_gradient", List.of("hgr"), "gradient", true, (nodes, args, parser) -> {
            var colors = gradientColors(args.input());

            if (colors.isEmpty()) {
                return new ParentNode(nodes);
            }

            return new GradientNode(nodes, GradientNode.GradientProvider.colorsHard(colors));
        }));
    }

    /**
     * Resets one aspect of a style, or all of them when {@code target} names nothing specific.
     */
    private static net.minecraft.network.chat.Style clear(net.minecraft.network.chat.Style style, String target) {
        switch (target) {
            case "hover" -> {
                return style.withHoverEvent(null);
            }
            case "click" -> {
                return style.withClickEvent(null);
            }
            case "color" -> {
                return style.withColor((TextColor) null);
            }
            case "insertion" -> {
                return style.withInsertion(null);
            }
            case "bold" -> {
                return style.withBold(null);
            }
            case "italic" -> {
                return style.withItalic(null);
            }
            case "underline" -> {
                return style.withUnderlined(null);
            }
            case "strikethrough" -> {
                return style.withStrikethrough(null);
            }
            case "obfuscated" -> {
                return style.withObfuscated(null);
            }
            case "all" -> {
                return net.minecraft.network.chat.Style.EMPTY;
            }
            default -> {
                return style;
            }
        }
    }

    private static void registerReset(TagRegistry registry) {
        registry.register(TextTag.wrap("clear_color", List.of("uncolor", "colorless"), "special", false,
                (nodes, args, parser) -> TextUtils.removeColors(TextNode.asSingle(nodes))));

        registry.register(TextTag.wrap("clear", "special", false,
                (nodes, args, parser) -> {
                    var target = args.get("value", 0, args.input());
                    return new TransformNode(nodes, new TransformNode.StyleTransformer(style -> clear(style, target)));
                }));
    }
}