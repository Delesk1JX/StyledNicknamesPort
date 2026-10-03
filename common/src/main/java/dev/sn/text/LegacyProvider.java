package dev.sn.text;

/**
 * Simplified Text Format provider: like {@link ModernProvider} but the argument separator is
 * {@code :} only, and {@code <r>} resets the whole stack.
 */
public class LegacyProvider extends AbstractTagProvider {
    public LegacyProvider(TagRegistry registry) {
        super(registry);
    }

    @Override
    public boolean isValidTag(String tag, TagLikeParser.Context context) {
        var peek = context.peekId();

        return tag.equals("r") || tag.equals("reset")
                || tag.startsWith("#")
                || this.registry.getTag(tag) != null
                || tag.equals("/") || (peek != null && tag.equals("/" + peek));
    }

    @Override
    public void handleTag(String id, String argument, TagLikeParser.Context context) {
        if (this.handleUniversal(id, context, true)) {
            return;
        }

        var tag = this.registry.getTag(id);

        if (tag == null) {
            return;
        }

        this.openOrEmit(tag, StringArgs.ordered(argument, ':'), context);
    }
}