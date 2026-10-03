package dev.sn.core;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * Permission checks.
 *
 * <p>NeoForge and Forge have no node based permission API like Fabric, so a permission is expressed
 * as an operator level. Each game version implements {@link PlatformBridge} in its own source set,
 * which is what keeps this shared code free of loader specific branches.
 */
public final class Permissions {
    /** Everyone, including players who are not operators. */
    public static final int LEVEL_ALL = 0;
    /** Players who may moderate. */
    public static final int LEVEL_MODERATORS = 1;
    /** Players who may manage other players' nicknames. */
    public static final int LEVEL_GAMEMASTERS = 2;
    /** Operators. */
    public static final int LEVEL_ADMINS = 3;

    private Permissions() {
    }

    public static boolean check(CommandSourceStack source, int level) {
        return CoreMod.bridge().hasPermission(source, level);
    }

    public static boolean check(ServerPlayer player, int level) {
        return CoreMod.bridge().hasPermission(player, level);
    }

    /**
     * Whether the player may use a formatting tag that the config does not enable by default.
     */
    public static boolean checkFormatting(ServerPlayer player, String tagName) {
        return check(player, LEVEL_GAMEMASTERS);
    }

    public static boolean mayUseNicknames(CommandSourceStack source) {
        return ConfigManager.get().data.allowByDefault || check(source, LEVEL_GAMEMASTERS);
    }

    public static boolean mayChangeOthers(CommandSourceStack source) {
        return check(source, LEVEL_ADMINS);
    }

    public static boolean mayReloadConfig(CommandSourceStack source) {
        return check(source, LEVEL_ADMINS);
    }

    public static boolean mayIgnoreLengthLimit(CommandSourceStack source) {
        return check(source, LEVEL_GAMEMASTERS);
    }
}