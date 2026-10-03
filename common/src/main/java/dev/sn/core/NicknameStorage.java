package dev.sn.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * Persists nicknames.
 *
 * <p>The upstream mod stores them through the Player Data API, which has no Forge or NeoForge
 * release. Player persistent NBT is used instead: the game saves it with the rest of the player's
 * data, so a nickname survives a restart without the mod owning any file format.
 *
 * <p>The NBT accessors differ between the targeted game versions, so the three reads and the one
 * write go through {@link PlatformBridge}, which each version implements on its own.
 */
public final class NicknameStorage {
    /** Namespace inside the player's persistent data. */
    private static final String KEY = CoreMod.ID;

    private NicknameStorage() {
    }

    public static String read(ServerPlayer player) {
        var tag = CoreMod.bridge().readNickname(player);
        return tag == null || tag.isEmpty() ? null : tag;
    }

    public static boolean requiresPermission(ServerPlayer player) {
        return CoreMod.bridge().readNicknamePermission(player);
    }

    public static void write(ServerPlayer player, String nickname, boolean requirePermission) {
        CoreMod.bridge().writeNickname(player, nickname, requirePermission);
    }

    /**
     * The namespace and the field names, shared so that a platform implementation cannot drift.
     */
    public static final class Keys {
        public static final String NAMESPACE = CoreMod.ID;
        public static final String NICKNAME = "nickname";
        public static final String PERMISSION = "permission";

        private Keys() {
        }

        public static CompoundTag empty() {
            return new CompoundTag();
        }
    }

    /**
     * Reads the stored nickname and applies it, called once a player has joined.
     */
    public static void apply(ServerPlayer player) {
        var nickname = read(player);

        if (nickname != null && !nickname.isEmpty()) {
            NicknameHolder.of(player).setNickname(nickname, requiresPermission(player));
        }
    }
}