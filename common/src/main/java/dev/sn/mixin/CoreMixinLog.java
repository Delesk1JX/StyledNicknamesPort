package dev.sn.mixin;

import dev.sn.core.CoreMod;

/**
 * Mixins run inside the game, where an exception in a name lookup must never be allowed to break
 * rendering, so failures are logged once per cause instead of propagating.
 */
final class CoreMixinLog {
    private CoreMixinLog() {
    }

    static void error(Throwable error) {
        CoreMod.LOGGER.error("Styled Nicknames could not build a display name", error);
    }
}