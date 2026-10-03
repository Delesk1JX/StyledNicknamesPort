package dev.sn.text;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A single formatting tag, for example {@code red}, {@code bold} or {@code rainbow}.
 *
 * @param name          canonical name used in the config and in {@link TagRegistry}
 * @param aliases       alternative spellings, for example {@code u} for {@code underline}
 * @param type          grouping used by the config ("color", "formatting", ...)
 * @param userSafe      whether the tag is offered by {@link TagRegistry#safe()}
 * @param selfContained whether the tag produces a node on its own instead of wrapping what follows
 * @param creator       builds the node once the arguments are known
 */
public record TextTag(String name, List<String> aliases, String type, boolean userSafe,
                      boolean selfContained, NodeCreator creator) {
    public TextTag {
        aliases = List.copyOf(aliases);
    }

    /** Wraps the nodes that follow the tag. */
    public static TextTag wrap(String name, Collection<String> aliases, String type, boolean userSafe,
                               NodeCreator creator) {
        return new TextTag(name, List.copyOf(aliases), type, userSafe, false, creator);
    }

    public static TextTag wrap(String name, String type, boolean userSafe, NodeCreator creator) {
        return wrap(name, List.of(), type, userSafe, creator);
    }

    /** Produces a node of its own and ignores what follows it. */
    public static TextTag leaf(String name, Collection<String> aliases, String type, boolean userSafe,
                               Function<StringArgs, TextNode> creator) {
        return new TextTag(name, List.copyOf(aliases), type, userSafe, true, NodeCreator.self(creator));
    }

    public static TextTag leaf(String name, String type, boolean userSafe, Function<StringArgs, TextNode> creator) {
        return leaf(name, List.of(), type, userSafe, creator);
    }

    public boolean hasAlias(String alias) {
        return this.aliases.contains(alias);
    }

    public List<String> allNames() {
        var all = new java.util.ArrayList<String>(this.aliases.size() + 1);
        all.add(this.name);
        all.addAll(this.aliases);
        return Collections.unmodifiableList(all);
    }
}