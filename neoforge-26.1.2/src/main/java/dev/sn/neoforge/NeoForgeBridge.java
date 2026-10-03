package dev.sn.neoforge;

import dev.sn.core.ConfigManager;
import dev.sn.core.CoreMod;
import dev.sn.core.NicknameStorage;
import dev.sn.core.PlatformBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.nio.file.Path;

/**
 * Minecraft 26.1.2 on NeoForge.
 *
 * <p>This version has a real permission model: a source carries a {@code PermissionSet} and a check
 * is made against a {@link Permission}. A permission here means "at least this operator level",
 * which is what the shared {@link dev.sn.core.Permissions} constants express.
 *
 * <p>The NBT accessors return {@link java.util.Optional} instead of a default value, which is why
 * every read below goes through {@code orElse}.
 */
public final class NeoForgeBridge implements PlatformBridge {
    @Override
    public boolean hasPermission(CommandSourceStack source, int level) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(levelOf(level)));
    }

    @Override
    public boolean hasPermission(ServerPlayer player, int level) {
        return player.permissions().hasPermission(new Permission.HasCommandLevel(levelOf(level)));
    }

    private static PermissionLevel levelOf(int level) {
        return switch (level) {
            case 0 -> PermissionLevel.ALL;
            case 1 -> PermissionLevel.MODERATORS;
            case 2 -> PermissionLevel.GAMEMASTERS;
            case 3 -> PermissionLevel.ADMINS;
            default -> PermissionLevel.OWNERS;
        };
    }

    @Override
    public Path configDir() {
        return Path.of("config");
    }

    @Override
    public String readNickname(ServerPlayer player) {
        return tag(player).getString(NicknameStorage.Keys.NICKNAME).orElse(null);
    }

    @Override
    public boolean readNicknamePermission(ServerPlayer player) {
        return tag(player).getByte(NicknameStorage.Keys.PERMISSION).orElse((byte) 0) > 0;
    }

    @Override
    public void writeNickname(ServerPlayer player, String nickname, boolean requirePermission) {
        var data = player.getPersistentData();
        var tag = data.getCompoundOrEmpty(NicknameStorage.Keys.NAMESPACE).copy();

        if (nickname == null || nickname.isEmpty()) {
            tag.remove(NicknameStorage.Keys.NICKNAME);
            tag.remove(NicknameStorage.Keys.PERMISSION);
        } else {
            tag.putString(NicknameStorage.Keys.NICKNAME, nickname);
            tag.putByte(NicknameStorage.Keys.PERMISSION, (byte) (requirePermission ? 1 : 0));
        }

        data.put(NicknameStorage.Keys.NAMESPACE, tag);
    }

    private static net.minecraft.nbt.CompoundTag tag(ServerPlayer player) {
        return player.getPersistentData().getCompoundOrEmpty(NicknameStorage.Keys.NAMESPACE);
    }

    @Override
    public boolean isServerThread(net.minecraft.world.entity.Entity entity) {
        var level = entity.level();
        var server = level == null ? null : level.getServer();

        return server == null || Thread.currentThread() == server.getRunningThread();
    }

    @Override
    public String nameOf(ServerPlayer player) {
        return player.getGameProfile().name();
    }

    @Override
    public void bootstrap() {
        dev.sn.text.EventProvider.install(NeoClickEvents.INSTANCE, NeoHoverEvents.INSTANCE);
    }

    @Override
    public void onServerStarted() {
        ConfigManager.load(configDir());
        CoreMod.LOGGER.info("{} {} ready", CoreMod.NAME, dev.sn.core.ModInfo.VERSION);
    }
}