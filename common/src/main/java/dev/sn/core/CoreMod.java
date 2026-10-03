package dev.sn.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared entry points and the mod's logger.
 */
public final class CoreMod {
    public static final String ID = "stylednicknames";
    public static final String NAME = "Styled Nicknames";

    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    private CoreMod() {
    }

    /**
     * Stores the platform implementation. A platform calls this from its own {@code bootstrap},
     * which is why the method does not call back into the platform.
     */
    public static void bootstrap(PlatformBridge platform) {
        BridgeHolder.set(platform);
    }

    /**
     * Holds the platform bridge, because a version specific implementation lives in a source set that
     * shared code cannot reference at compile time.
     */
    private static final class BridgeHolder {
        private static volatile PlatformBridge bridge;

        private BridgeHolder() {
        }

        static void set(PlatformBridge value) {
            bridge = value;
        }

        static PlatformBridge get() {
            var current = bridge;

            if (current == null) {
                throw new IllegalStateException(
                        "The mod was used before its platform was set up");
            }

            return current;
        }
    }

    public static PlatformBridge bridge() {
        return BridgeHolder.get();
    }
}