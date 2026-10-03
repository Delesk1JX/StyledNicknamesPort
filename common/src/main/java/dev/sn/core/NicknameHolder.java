package dev.sn.core;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

/**
 * The nickname of one player.
 *
 * <p>Implemented by the mixin on the player's network handler, so that the nickname lives on the
 * same object that owns the connection and is gone with it.
 */
public interface NicknameHolder {
    /**
     * Stand-in used when a lookup finds no player, so callers never have to null check.
     */
    NicknameHolder EMPTY = new NicknameHolder() {
        @Override
        public void setNickname(String nickname, boolean requirePermission) {
        }

        @Override
        public String getNickname() {
            return null;
        }

        @Override
        public Component getParsedNickname() {
            return null;
        }

        @Override
        public MutableComponent getOutput() {
            return null;
        }

        @Override
        public MutableComponent getOutputOrVanilla() {
            return Component.empty();
        }

        @Override
        public boolean requiresPermission() {
            return false;
        }

        @Override
        public void loadData() {
        }

        @Override
        public boolean shouldDisplay() {
            return false;
        }

        @Override
        public Function<String, Component> placeholderValue() {
            return x -> Component.empty();
        }
    };

    static NicknameHolder of(ServerPlayer player) {
        return player == null || player.connection == null ? EMPTY : NicknameHolder.of(player.connection);
    }

    static NicknameHolder of(Object connection) {
        return connection instanceof NicknameHolder holder ? holder : EMPTY;
    }

    /**
     * Sets the nickname and persists it.
     *
     * @param requirePermission whether the nickname was chosen by the player themselves, which
     *                          decides whether formatting has to be checked against permissions
     */
    void setNickname(String nickname, boolean requirePermission);

    /**
     * @return the nickname as the player typed it, or null when there is none
     */
    String getNickname();

    /**
     * @return the nickname with its formatting applied, or null when there is none
     */
    Component getParsedNickname();

    /**
     * @return the formatted nickname, or null when there is none
     */
    MutableComponent getOutput();

    /**
     * @return the formatted nickname, falling back to the real name
     */
    MutableComponent getOutputOrVanilla();

    boolean requiresPermission();

    /**
     * Reads the stored nickname, called once the player has joined.
     */
    void loadData();

    /**
     * Whether the nickname should be shown to this player. A nickname a player chose without
     * permission is hidden from everyone who lacks it.
     */
    boolean shouldDisplay();

    /**
     * @return a resolver for the {@code ${nickname}} placeholder
     */
    Function<String, Component> placeholderValue();
}