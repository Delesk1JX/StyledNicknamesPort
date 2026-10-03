package dev.sn.text;

import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Parses text containing tags such as {@code <red>}, keeping a stack of open tags so that
 * {@code </red>} closes exactly the right one.
 */
public abstract class TagLikeParser implements NodeParser {
    /** QuickText. */
    public static final TagFormat TAGS = TagFormat.of('<', '>', ':');
    /** Simplified Text Format. */
    public static final TagFormat TAGS_LEGACY = LegacyFormat.INSTANCE;
    /** Placeholders written as {@code ${name}}. */
    public static final TagFormat PLACEHOLDER_USER = TagFormat.of("${", "}", "");
    /** Placeholders written as {@code %name%}. */
    public static final TagFormat PLACEHOLDER = TagFormat.of('%', '%', ' ');

    private static final TextNode[] EMPTY = new TextNode[0];

    Context newContext() {
        return new Context(this, "");
    }

    public static TagLikeParser of(TagFormat format, Provider provider) {
        return new SingleTagLikeParser(format, provider);
    }

    public static TagLikeParser placeholder(TagFormat format, ParserContext.Key<Function<String, Component>> key) {
        return of(format, Provider.placeholder(key));
    }

    @Override
    public TextNode[] parseNodes(TextNode input) {
        var context = new Context(this, "");
        this.parse(input, context);
        return context.toTextNode();
    }

    private void parse(TextNode node, Context context) {
        if (node instanceof LiteralNode literal) {
            context.input = literal.value();
            this.handleLiteral(literal.value(), context);
        } else if (node instanceof ParentTextNode parent) {
            var size = context.size();
            context.pushWithParser(null, parent::copyWith);

            for (var child : parent.getChildren()) {
                this.parse(child, context);
            }

            context.pop(context.size() - size);
        } else {
            context.addNode(node);
        }
    }

    protected abstract void handleLiteral(String value, Context context);

    /**
     * Emits the literal text before the tag, then lets the provider consume the tag.
     *
     * @return the position to continue from, or -1 when nothing is left
     */
    protected final int handleTag(String value, int pos, TagFormat.Tag tag, Provider provider, Context context) {
        var modified = provider.modifyTag(tag, context);

        if (modified == null) {
            context.addNode(new LiteralNode(value.substring(pos)));
            return -1;
        }

        if (modified.start() != 0 && modified.start() != pos) {
            context.addNode(new LiteralNode(value.substring(pos, modified.start())));
        }

        context.currentPos = modified.start();
        provider.handleTag(modified.id(), modified.argument(), context);

        return modified.end();
    }

    /** Decides what a tag means and turns it into a node. */
    public interface Provider {
        boolean isValidTag(String tag, Context context);

        void handleTag(String id, String argument, Context context);

        default TagFormat.Tag modifyTag(TagFormat.Tag tag, Context context) {
            return tag;
        }

        static Provider placeholder(ParserContext.Key<Function<String, Component>> key) {
            return new Provider() {
                @Override
                public boolean isValidTag(String tag, Context context) {
                    return true;
                }

                @Override
                public void handleTag(String id, String argument, Context context) {
                    context.addNode(new DynamicTextNode(id, key));
                }
            };
        }
    }

    /**
     * Stack of open tags. {@link Scope#id} is the name of the tag that pushed the scope, which is
     * what {@code </name>} matches against.
     */
    public static final class Context {
        private final Deque<Scope> stack = new ArrayDeque<>();
        private final TagLikeParser parser;

        private int currentPos;
        private String input = "";

        Context(TagLikeParser parser, String input) {
            this.parser = parser;
            this.input = input;
            this.stack.push(Scope.parent());
        }

        public String input() {
            return this.input;
        }

        public int currentTagPos() {
            return this.currentPos;
        }

        public NodeParser parser() {
            return this.parser;
        }

        public int size() {
            return this.stack.size() - 1;
        }

        public boolean contains(String id) {
            for (var scope : this.stack) {
                if (id.equals(scope.id)) {
                    return true;
                }
            }

            return false;
        }

        public String peekId() {
            return this.stack.peek().id;
        }

        public void pop() {
            if (this.stack.size() > 1) {
                var scope = this.stack.pop();
                this.stack.peek().nodes.add(scope.collapse(this.parser));
            }
        }

        public void pop(int count) {
            count = Math.min(count, this.stack.size() - 1);

            for (var i = 0; i < count; i++) {
                var scope = this.stack.pop();
                this.stack.peek().nodes.add(scope.collapse(this.parser));
            }
        }

        /**
         * Closes scopes until the named one is closed, closing everything nested inside it.
         */
        public void pop(String id) {
            if (!this.contains(id)) {
                return;
            }

            while (this.stack.size() > 1) {
                var scope = this.stack.pop();
                this.stack.peek().nodes.add(scope.collapse(this.parser));

                if (id.equals(scope.id)) {
                    return;
                }
            }
        }

        public void pop(Predicate<String> stopPredicate) {
            while (this.stack.size() > 1) {
                if (stopPredicate.test(this.stack.peek().id)) {
                    return;
                }

                var scope = this.stack.pop();
                this.stack.peek().nodes.add(scope.collapse(this.parser));
            }
        }

        /**
         * Closes everything above the named tag but keeps the scopes opened inside it, so that
         * {@code ;name} re-opens a tag without losing its nesting.
         */
        public void popUntilOnly(String id) {
            if (!this.contains(id)) {
                return;
            }

            var reopened = new ArrayDeque<Scope>();

            while (this.stack.size() > 1) {
                var scope = this.stack.pop();
                this.stack.peek().nodes.add(scope.collapse(this.parser));

                if (id.equals(scope.id)) {
                    while (!reopened.isEmpty()) {
                        this.stack.push(reopened.pop());
                    }

                    return;
                }

                reopened.push(new Scope(scope.id, new ArrayList<>(), scope.merger()));
            }
        }

        public void popInclusive(Predicate<String> stopPredicate) {
            while (this.stack.size() > 1) {
                var scope = this.stack.pop();
                this.stack.peek().nodes.add(scope.collapse(this.parser));

                if (stopPredicate.test(scope.id)) {
                    return;
                }
            }
        }

        public void pushParent() {
            this.stack.push(Scope.parent());
        }

        public void push(String id, Function<TextNode[], TextNode> merge) {
            this.stack.push(Scope.enclosing(id, merge));
        }

        public void pushWithParser(String id, BiFunction<TextNode[], NodeParser, TextNode> merge) {
            this.stack.push(Scope.enclosingParsed(id, merge));
        }

        public void addNode(TextNode node) {
            this.stack.peek().nodes.add(node);
        }

        public TextNode[] toTextNode() {
            while (!this.stack.isEmpty()) {
                var scope = this.stack.pop();

                if (this.stack.isEmpty()) {
                    return scope.nodes().toArray(EMPTY);
                }

                this.stack.peek().nodes.add(scope.collapse(this.parser));
            }

            return EMPTY;
        }

        private record Scope(String id, List<TextNode> nodes,
                             BiFunction<TextNode[], NodeParser, TextNode> merger) {
            static Scope parent() {
                return enclosingParsed(null, (a, b) -> new ParentNode(a));
            }

            static Scope enclosing(String id, Function<TextNode[], TextNode> merge) {
                return enclosingParsed(id, (a, b) -> merge.apply(a));
            }

            static Scope enclosingParsed(String id, BiFunction<TextNode[], NodeParser, TextNode> merge) {
                return new Scope(id, new ArrayList<>(), merge);
            }

            TextNode collapse(NodeParser parser) {
                return this.merger.apply(this.nodes.toArray(EMPTY), parser);
            }
        }
    }
}