package dev.sn.core;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * The few operations that differ between the targeted game versions.
 *
 * <p>Every version provides its own implementation of this interface, which is what lets the rest of
 * the mod stay in shared code. Three kinds of difference are covered: the permission system, the
 * NBT accessors, and whether a server thread check exists.
 */
public interface PlatformBridge {
    boolean hasPermission(CommandSourceStack source, int level);

    boolean hasPermission(ServerPlayer player, int level);

    /**
     * @return the directory the config file lives in
     */
    java.nio.file.Path configDir();

    /**
     * Reads the stored nickname.
     *
     * @return the nickname, or null when there is none
     */
    String readNickname(ServerPlayer player);

    boolean readNicknamePermission(ServerPlayer player);

    /**
     * Stores the nickname, or clears it when {@code nickname} is null or empty.
     */
    void writeNickname(ServerPlayer player, String nickname, boolean requirePermission);

    /**
     * @return whether the caller is on the thread the server ticks on, which is where parsing a
     *         nickname is safe
     */
    boolean isServerThread(net.minecraft.world.entity.Entity entity);

    /**
     * @return the name of the player, whose accessor differs between the targeted versions
     */
    String nameOf(ServerPlayer player);

    /**
     * Sets the platform up: registers commands and wires the shared state.
     */
    void bootstrap();

    /**
     * Called once the server has finished starting, so that the config is available to a nickname
     * read during world load.
     */
    default void onServerStarted() {
    }
}