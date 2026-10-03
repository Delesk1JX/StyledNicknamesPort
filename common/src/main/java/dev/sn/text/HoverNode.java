package dev.sn.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Arrays;

/**
 * {@code <hover>} support.
 *
 * <p>The event itself is built by {@link HoverEvents}, which each game version implements, because
 * the class behind hover events was reshaped between the targeted versions.
 */
public final class HoverNode extends SimpleStylingNode {
    private final Action action;
    private final TextNode text;
    private final String entityType;
    private final String uuid;
    private final TextNode entityName;

    public HoverNode(TextNode[] children, TextNode text) {
        this(children, Action.TEXT, text, null, null, null);
    }

    private HoverNode(TextNode[] children, Action action, TextNode text,
                      String entityType, String uuid, TextNode entityName) {
        super(children);
        this.action = action;
        this.text = text;
        this.entityType = entityType;
        this.uuid = uuid;
        this.entityName = entityName;
    }

    public static HoverNode entity(TextNode[] children, String entityType, String uuid, TextNode name) {
        return new HoverNode(children, Action.ENTITY, null, entityType, uuid, name);
    }

    @Override
    protected Style style(ParserContext context) {
        if (this.action == Action.ENTITY) {
            var name = this.entityName == null ? Component.empty() : this.entityName.toComponent(context, true);
            return EventProvider.hovers().entity(this.entityType, this.uuid, name);
        }

        var text = this.text == null ? "" : this.text.toComponent(context, true).getString();
        return EventProvider.hovers().text(text);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new HoverNode(children, this.action, this.text, this.entityType, this.uuid, this.entityName);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        return new HoverNode(children, this.action,
                this.text != null ? TextNode.asSingle(parser.parseNodes(this.text)) : null,
                this.entityType, this.uuid,
                this.entityName != null ? TextNode.asSingle(parser.parseNodes(this.entityName)) : null);
    }

    @Override
    public boolean isDynamicNoChildren() {
        return (this.text != null && this.text.isDynamic())
                || (this.entityName != null && this.entityName.isDynamic());
    }

    @Override
    public String toString() {
        return "HoverNode{action=" + this.action + ", children=" + Arrays.toString(getChildren()) + "}";
    }

    private enum Action {
        TEXT,
        ENTITY
    }

}