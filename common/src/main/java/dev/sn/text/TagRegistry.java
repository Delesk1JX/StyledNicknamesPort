package dev.sn.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The set of formatting tags a parser may use.
 *
 * <p>{@link #SAFE} holds the tags offered to players, {@link #DEFAULT} also holds the ones that are
 * only safe for admins. The mod builds a per player registry from {@link #SAFE} so that a player
 * without a permission cannot smuggle a tag past the config.
 */
public final class TagRegistry {
    private static final TagRegistry SAFE = new TagRegistry(true);
    private static final TagRegistry DEFAULT = new TagRegistry(true);

    static {
        BuiltinTags.register(DEFAULT);
        BuiltinTags.register(SAFE);
    }

    public static TagRegistry safe() {
        return SAFE;
    }

    public static TagRegistry defaults() {
        return DEFAULT;
    }

    public static TagRegistry create() {
        return new TagRegistry(false);
    }

    /**
     * A registry holding every player facing tag, to be narrowed down by permissions.
     */
    public static TagRegistry createSafe() {
        return SAFE.copy();
    }

    private final boolean global;
    private final List<TextTag> tags = new java.util.ArrayList<>();
    private final Map<String, TextTag> byName = new HashMap<>();
    private final Map<String, TextTag> byAlias = new HashMap<>();

    private TagRegistry(boolean global) {
        this.global = global;
    }

    public void register(TextTag tag) {
        if (this.byName.containsKey(tag.name())) {
            if (this.global) {
                throw new IllegalStateException("Duplicate tag identifier: " + tag.name());
            }

            this.tags.removeIf(existing -> existing.name().equals(tag.name()));
        }

        this.byName.put(tag.name(), tag);
        this.tags.add(tag);
        this.byAlias.put(tag.name(), tag);

        for (var alias : tag.aliases()) {
            this.byAlias.put(alias, tag);
        }
    }

    public void remove(TextTag tag) {
        if (this.global) {
            return;
        }

        if (this.tags.remove(tag)) {
            this.byName.values().removeIf(x -> x == tag);
            this.byAlias.values().removeIf(x -> x == tag);
        }
    }

    public TagRegistry copy() {
        var copy = new TagRegistry(false);

        for (var tag : this.tags) {
            copy.register(tag);
        }

        return copy;
    }

    public TextTag getTag(String name) {
        return this.byAlias.get(name);
    }

    public List<TextTag> getTags() {
        return Collections.unmodifiableList(this.tags);
    }

    public boolean isGlobal() {
        return this.global;
    }
}