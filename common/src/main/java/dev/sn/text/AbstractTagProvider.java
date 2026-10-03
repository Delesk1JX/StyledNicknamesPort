package dev.sn.text;

/**
 * Shared handling of closing tags, hex colours and the QuickText/STF differences.
 */
abstract class AbstractTagProvider implements TagLikeParser.Provider {
    protected final TagRegistry registry;

    AbstractTagProvider(TagRegistry registry) {
        this.registry = registry;
    }

    /**
     * Handles {@code </}, {@code </*>}, {@code <r>} and hex literals, which behave the same way in
     * every syntax. Returns true when the tag was consumed.
     */
    protected boolean handleUniversal(String id, TagLikeParser.Context context, boolean resetSupported) {
        var peek = context.peekId();

        if (id.equals("/") || (peek != null && (id.equals("/" + peek) || (peek.startsWith("#") && id.equals("/c"))))) {
            context.pop();
            return true;
        }

        if (id.equals("/*") || (resetSupported && (id.equals("r") || id.equals("reset")))) {
            context.pop(context.size());
            return true;
        }

        if (id.length() > 1 && id.charAt(0) == '/') {
            var name = id.substring(1);
            context.popInclusive(name::equals);
            return true;
        }

        if (id.length() > 1 && id.charAt(0) == ';') {
            context.popUntilOnly(id.substring(1));
            return true;
        }

        if (id.startsWith("#")) {
            var color = Colors.fromHex(id.substring(1));

            if (color != null) {
                context.push("c", children -> new ColorNode(children, color));
                return true;
            }
        }

        return false;
    }

    protected void openOrEmit(TextTag tag, StringArgs args, TagLikeParser.Context context) {
        if (tag.selfContained()) {
            context.addNode(tag.creator().createTextNode(TextNode.array(), args, context.parser()));
        } else {
            context.push(tag.name(), children -> tag.creator().createTextNode(children, args, context.parser()));
        }
    }
}