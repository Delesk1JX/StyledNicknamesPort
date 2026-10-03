package dev.sn.text;

/**
 * Supplies the platform specific hover and click events to the parser.
 *
 * <p>Both classes behind them were reshaped between the targeted game versions, so each version
 * implements them and installs the result here. This keeps the text package version independent.
 */
public final class EventProvider {
    private static volatile ClickEvents clicks;
    private static volatile HoverEvents hovers;

    private EventProvider() {
    }

    public static void install(ClickEvents clickEvents, HoverEvents hoverEvents) {
        clicks = clickEvents;
        hovers = hoverEvents;
    }

    public static ClickEvents clicks() {
        var events = clicks;

        if (events == null) {
            throw new IllegalStateException("Click events were used before the platform was set up");
        }

        return events;
    }

    public static HoverEvents hovers() {
        var events = hovers;

        if (events == null) {
            throw new IllegalStateException("Hover events were used before the platform was set up");
        }

        return events;
    }
}