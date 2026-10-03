package dev.sn.text;

import net.minecraft.network.chat.Style;

import java.util.Arrays;

/**
 * {@code <click>} and its shorthands.
 *
 * <p>The action is kept as its name rather than as a constant, because the class that holds the
 * click actions was reshaped between the targeted game versions. Building the event is left to
 * {@link ClickEvents}, which each version implements.
 *
 * <p>Only the actions that exist on every target are reachable, so an untrusted nickname cannot
 * trigger a server side only click.
 */
public final class ClickActionNode extends SimpleStylingNode {
    public static final String OPEN_URL = ClickEvents.OPEN_URL;
    public static final String RUN_COMMAND = ClickEvents.RUN_COMMAND;
    public static final String SUGGEST_COMMAND = ClickEvents.SUGGEST_COMMAND;
    public static final String COPY_TO_CLIPBOARD = ClickEvents.COPY_TO_CLIPBOARD;
    public static final String CHANGE_PAGE = ClickEvents.CHANGE_PAGE;

    private final String action;
    private final TextNode value;

    public ClickActionNode(TextNode[] children, String action, TextNode value) {
        super(children);
        this.action = action;
        this.value = value;
    }

    public String action() {
        return this.action;
    }

    public TextNode value() {
        return this.value;
    }

    @Override
    protected Style style(ParserContext context) {
        return EventProvider.clicks().style(this.action, this.value.toComponent(context).getString());
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new ClickActionNode(children, this.action, this.value);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        return new ClickActionNode(children, this.action, TextNode.asSingle(parser.parseNodes(this.value)));
    }

    @Override
    public boolean isDynamicNoChildren() {
        return this.value.isDynamic();
    }

    @Override
    public String toString() {
        return "ClickActionNode{action=" + this.action + ", children=" + Arrays.toString(getChildren()) + "}";
    }
}