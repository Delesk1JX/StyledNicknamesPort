package dev.sn.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Typed bag of values passed to a parser while it turns nodes into a {@link net.minecraft.network.chat.Component}.
 *
 * <p>Keys are compared by identity, so a key instance is the only thing that has to be shared between
 * the code registering a placeholder and the code producing it.
 */
public final class ParserContext {
    public static final Key<Boolean> COMPACT_COMPONENT = Key.of("compact_component", Boolean.class);

    private Map<Key<?>, Object> map;
    private boolean copyOnWrite;

    private ParserContext(Map<Key<?>, Object> map, boolean copyOnWrite) {
        this.map = map;
        this.copyOnWrite = copyOnWrite;
    }

    public static ParserContext of() {
        return new ParserContext(new HashMap<>(), false);
    }

    public static <T> ParserContext of(Key<T> key, T object) {
        return of().with(key, object);
    }

    public <T> ParserContext with(Key<T> key, T object) {
        if (this.map.get(key) == object) {
            return this;
        }

        if (this.copyOnWrite) {
            this.map = new HashMap<>(this.map);
            this.copyOnWrite = false;
        }

        if (object == null) {
            this.map.remove(key);
        } else {
            this.map.put(key, object);
        }

        return this;
    }

    public <T> ParserContext withIfNotSet(Key<T> key, T object) {
        if (this.map.containsKey(key)) {
            return this;
        }

        return this.with(key, object);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) this.map.get(key);
    }

    public <T> T getOrElse(Key<T> key, T defaultValue) {
        @SuppressWarnings("unchecked")
        var value = (T) this.map.getOrDefault(key, defaultValue);
        return value;
    }

    public <T> T getOrElse(Key<T> key, Supplier<T> defaultValue) {
        @SuppressWarnings("unchecked")
        var value = (T) this.map.get(key);
        return value != null ? value : defaultValue.get();
    }

    public <T> T getOrThrow(Key<T> key) {
        return Objects.requireNonNull(get(key));
    }

    public boolean contains(Key<?> key) {
        return this.map.containsKey(key);
    }

    /**
     * Returns a context that can be safely mutated without affecting this one.
     */
    public ParserContext copy() {
        this.copyOnWrite = true;
        return new ParserContext(this.map, true);
    }

    public record Key<T>(String key, Class<T> type) {
        public static <T> Key<T> of(String key, Class<T> type) {
            return new Key<>(key, type);
        }

        public static <T> Key<T> of(String key) {
            return new Key<>(key, null);
        }
    }
}