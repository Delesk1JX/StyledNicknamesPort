package dev.sn.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed arguments of a single tag, for example {@code red} in {@code <gradient:red:blue>}.
 *
 * <p>Supports both positional ({@code a:b:c}) and keyed ({@code key=value}) access, plus nested
 * maps written as {@code key={...}}.
 */
public final class StringArgs {
    private static final StringArgs EMPTY = new StringArgs("");

    private final List<String> ordered = new ArrayList<>();
    private final Map<String, String> keyed = new HashMap<>();
    private final Map<String, StringArgs> keyedMaps = new HashMap<>();
    private final String input;

    private int currentOrdered = 0;

    private StringArgs(String input) {
        this.input = input;
    }

    public static StringArgs ordered(String input, char separator) {
        var args = new StringArgs(input);
        args.ordered.addAll(SimpleArguments.split(input, separator));
        return args;
    }

    public static StringArgs keyed(String input, char separator, char map) {
        return keyed(input, separator, map, true, SimpleArguments.DEFAULT_WRAPPERS);
    }

    public static StringArgs keyed(String input, char separator, char map, boolean hasMaps, char[] wrappers) {
        if (input == null) {
            return StringArgs.empty();
        }

        var args = new StringArgs(input);
        decompose(input, 0, separator, map, hasMaps, wrappers, (char) 0, (key, value) -> {
            if (key != null) {
                args.keyed.put(key, value != null ? SimpleArguments.unwrap(value, wrappers) : "");
            }
        }, args.keyedMaps::put);

        return args;
    }

    public static StringArgs full(String input, char separator, char map) {
        return full(input, separator, map, true, SimpleArguments.DEFAULT_WRAPPERS);
    }

    public static StringArgs full(String input, char separator, char map, boolean hasMaps, char[] wrappers) {
        var args = new StringArgs(input);

        decompose(input, 0, separator, map, hasMaps, wrappers, (char) 0, (key, value) -> {
            if (key != null) {
                args.keyed.put(key, value != null ? SimpleArguments.unwrap(value, wrappers) : "");

                if (value == null) {
                    args.ordered.add(SimpleArguments.unwrap(key, wrappers));
                }
            }
        }, args.keyedMaps::put);

        // "a:b" is read as the key "a" with the value "b", which loses the positional view that tags
        // such as <gradient> rely on. The ordered list therefore always mirrors the input split on
        // the separator, so a tag can read either view depending on what it needs.
        args.ordered.clear();
        args.ordered.addAll(SimpleArguments.split(input, separator));

        return args;
    }

    private interface KeyConsumer {
        void accept(String key, String value);
    }

    private interface MapConsumer {
        void accept(String key, StringArgs args);
    }

    private static int decompose(String input, int offset, char separator, char map, boolean hasMaps,
                                 char[] wrappers, char stopAt, KeyConsumer consumer, MapConsumer mapConsumer) {
        String key = null;
        String value = null;
        var builder = new StringBuilder();
        char wrap = 0;
        int i = offset;

        for (; i < input.length(); i++) {
            var chr = input.charAt(i);
            var chrN = i != input.length() - 1 ? input.charAt(i + 1) : 0;

            if (chr == stopAt && wrap == 0) {
                break;
            } else if (key != null && builder.length() == 0 && hasMaps && (chr == '{' || chr == '[') && wrap == 0) {
                var ordered = new ArrayList<String>();
                var keyed = new HashMap<String, String>();
                var keyedMaps = new HashMap<String, StringArgs>();

                var ti = decompose(input, i + 1, separator, map, true, wrappers,
                        chr == '{' ? '}' : ']', (keyx, valuex) -> {
                            if (keyx != null) {
                                keyed.put(keyx, valuex != null ? SimpleArguments.unwrap(valuex, wrappers) : "");

                                if (valuex == null) {
                                    ordered.add(SimpleArguments.unwrap(keyx, wrappers));
                                }
                            }
                        }, keyedMaps::put);

                if (ti == input.length()) {
                    builder.append(chr);
                } else {
                    var arg = new StringArgs(input.substring(i, ti));
                    arg.ordered.addAll(ordered);
                    arg.keyed.putAll(keyed);
                    arg.keyedMaps.putAll(keyedMaps);

                    mapConsumer.accept(key, arg);
                    key = null;
                    i = ti;
                }
            } else if (chr == map && wrap == 0 && key == null) {
                key = builder.toString();
                builder = new StringBuilder();
            } else if ((chr == '\\' && chrN != 0) || (chrN != 0 && chr == chrN && SimpleArguments.isWrapCharacter(chr, wrappers))) {
                builder.append(chrN);
                i++;
            } else if (SimpleArguments.isWrapCharacter(chr, wrappers) && (wrap == 0 || wrap == chr)) {
                wrap = wrap == 0 ? chr : 0;
            } else if (chr == separator && wrap == 0) {
                if (builder.length() == 0 && key == null) {
                    consumer.accept(null, null);
                    continue;
                }

                if (key == null) {
                    key = builder.toString();
                } else {
                    value = builder.toString();
                }

                consumer.accept(key, value);
                key = null;
                value = null;
                builder = new StringBuilder();
            } else {
                builder.append(chr);
            }
        }

        if (key != null) {
            consumer.accept(key, builder.length() == 0 ? null : builder.toString());
        } else if (builder.length() != 0) {
            consumer.accept(builder.toString(), null);
        }

        return i;
    }

    public static StringArgs empty() {
        return EMPTY;
    }

    public static StringArgs emptyNew() {
        return new StringArgs("");
    }

    public String input() {
        return input;
    }

    public String get(String name) {
        return keyed.get(name);
    }

    public String get(String name, String defaultValue) {
        return keyed.getOrDefault(name, defaultValue);
    }

    /**
     * Keyed value, falling back to the positional argument at {@code id}.
     */
    public String get(String name, int id) {
        var value = keyed.get(name);

        if (value != null) {
            return value;
        }

        if (id < ordered.size()) {
            return ordered.get(id);
        }

        return null;
    }

    public String get(String name, int id, String defaultValue) {
        var value = get(name, id);
        return value != null ? value : defaultValue;
    }

    public StringArgs getNested(String name) {
        return keyedMaps.get(name);
    }

    public StringArgs getNestedOrEmpty(String name) {
        return keyedMaps.getOrDefault(name, EMPTY);
    }

    public String getNext(String name) {
        var value = keyed.get(name);

        if (value != null) {
            return value;
        }

        if (currentOrdered < ordered.size()) {
            return ordered.get(currentOrdered++);
        }

        return null;
    }

    public String getNext(String name, String defaultValue) {
        var value = getNext(name);
        return value != null ? value : defaultValue;
    }

    public boolean contains(String key) {
        return keyed.containsKey(key);
    }

    public boolean isEmpty() {
        return keyed.isEmpty() && ordered.isEmpty();
    }

    public List<String> ordered() {
        return Collections.unmodifiableList(this.ordered);
    }

    public int size() {
        return Math.max(this.keyed.size(), this.ordered.size());
    }

    @Override
    public String toString() {
        return "StringArgs{ordered=" + ordered + ", keyed=" + keyed + "}";
    }
}