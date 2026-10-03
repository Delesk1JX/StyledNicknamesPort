package dev.sn.text;

/**
 * QuickText provider: {@code <tag>}, {@code <#rrggbb>}, {@code <tag:argument>} and {@code </tag>}.
 */
public class ModernProvider extends AbstractTagProvider {
    public ModernProvider(TagRegistry registry) {
        super(registry);
    }

    @Override
    public boolean isValidTag(String tag, TagLikeParser.Context context) {
        return tag.equals("/*")
                || tag.startsWith("#")
                || this.registry.getTag(tag) != null
                || tag.equals("/")
                || (tag.length() > 1 && tag.charAt(0) == '/' && context.contains(tag.substring(1)))
                || (tag.length() > 1 && tag.charAt(0) == ';' && context.contains(tag.substring(1)));
    }

    @Override
    public void handleTag(String id, String argument, TagLikeParser.Context context) {
        if (this.handleUniversal(id, context, false)) {
            return;
        }

        var tag = this.registry.getTag(id);

        if (tag == null) {
            return;
        }

        this.openOrEmit(tag, StringArgs.full(argument, ' ', ':'), context);
    }
}