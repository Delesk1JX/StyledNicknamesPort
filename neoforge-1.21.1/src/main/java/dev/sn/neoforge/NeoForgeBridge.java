package dev.sn.neoforge;

import dev.sn.core.ConfigManager;
import dev.sn.core.CoreMod;
import dev.sn.core.NicknameStorage;
import dev.sn.core.PlatformBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;

import java.nio.file.Path;

/**
 * Minecraft 1.21.1 on NeoForge.
 *
 * <p>Permission checks on this version are a plain {@code int} operator level, and the NBT
 * accessors return a default value directly rather than an {@link java.util.Optional}.
 */
public final class NeoForgeBridge implements PlatformBridge {
    @Override
    public boolean hasPermission(CommandSourceStack source, int level) {
        return source.hasPermission(level);
    }

    @Override
    public boolean hasPermission(ServerPlayer player, int level) {
        return player.hasPermissions(level);
    }

    @Override
    public Path configDir() {
        return Path.of("config");
    }

    @Override
    public String readNickname(ServerPlayer player) {
        var tag = tag(player);
        return tag.contains(NicknameStorage.Keys.NICKNAME) ? tag.getString(NicknameStorage.Keys.NICKNAME) : null;
    }

    @Override
    public boolean readNicknamePermission(ServerPlayer player) {
        return tag(player).getByte(NicknameStorage.Keys.PERMISSION) > 0;
    }

    @Override
    public void writeNickname(ServerPlayer player, String nickname, boolean requirePermission) {
        var data = player.getPersistentData();
        var tag = data.getCompound(NicknameStorage.Keys.NAMESPACE).copy();

        if (nickname == null || nickname.isEmpty()) {
            tag.remove(NicknameStorage.Keys.NICKNAME);
            tag.remove(NicknameStorage.Keys.PERMISSION);
        } else {
            tag.putString(NicknameStorage.Keys.NICKNAME, nickname);
            tag.putByte(NicknameStorage.Keys.PERMISSION, (byte) (requirePermission ? 1 : 0));
        }

        data.put(NicknameStorage.Keys.NAMESPACE, tag);
    }

    private static CompoundTag tag(ServerPlayer player) {
        return player.getPersistentData().getCompound(NicknameStorage.Keys.NAMESPACE);
    }

    @Override
    public boolean isServerThread(net.minecraft.world.entity.Entity entity) {
        var level = entity.level();
        var server = level == null ? null : level.getServer();

        return server == null || Thread.currentThread() == server.getRunningThread();
    }

    @Override
    public String nameOf(ServerPlayer player) {
        return player.getGameProfile().getName();
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