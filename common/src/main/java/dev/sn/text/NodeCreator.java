package dev.sn.text;

import java.util.function.Function;

/**
 * Builds the node a {@link TextTag} stands for.
 */
@FunctionalInterface
public interface NodeCreator {
    TextNode createTextNode(TextNode[] nodes, StringArgs args, NodeParser parser);

    /**
     * For tags that ignore their children, such as {@code <lang>}.
     */
    static NodeCreator self(Function<StringArgs, TextNode> function) {
        return (nodes, args, parser) -> function.apply(args);
    }

    /**
     * For boolean flags such as {@code <bold>}, where the value may be given as {@code value=false}.
     */
    static NodeCreator flag(TagFunction function) {
        return (nodes, args, parser) -> function.apply(nodes, SimpleArguments.bool(args.get("value", 0), true));
    }

    /** Creates a node that wraps the nodes that follow the tag. */
    @FunctionalInterface
    interface TagFunction {
        TextNode apply(TextNode[] nodes, Boolean value);
    }
}