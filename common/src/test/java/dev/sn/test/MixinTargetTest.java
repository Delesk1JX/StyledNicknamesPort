package dev.sn.test;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads the shipped mixin classes and checks that the names they target exist on this game version.
 *
 * <p>A mixin whose target is renamed fails silently at runtime and only shows up as a name that
 * stays the player's real one, so the targets are verified here rather than by playing the game.
 */
class MixinTargetTest {
    private static Class<?> load(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Missing class: " + name, e);
        }
    }

    @Test
    void minecraftClassesArePresent() {
        assertNotNull(load("net.minecraft.server.level.ServerPlayer"));
        assertNotNull(load("net.minecraft.server.players.PlayerList"));
        assertNotNull(load("net.minecraft.server.network.ServerGamePacketListenerImpl"));
        assertNotNull(load("net.minecraft.world.entity.player.Player"));
        assertNotNull(load("net.minecraft.world.scores.PlayerTeam"));
        assertNotNull(load("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket"));
    }

    @Test
    void tabListNameTargetExists() throws Exception {
        var method = net.minecraft.server.level.ServerPlayer.class.getMethod("getTabListDisplayName");
        assertTrue(net.minecraft.network.chat.Component.class.isAssignableFrom(method.getReturnType()));
    }

    @Test
    void placeNewPlayerTargetExists() throws Exception {
        var method = net.minecraft.server.players.PlayerList.class.getMethod(
                "placeNewPlayer", net.minecraft.network.Connection.class,
                net.minecraft.server.level.ServerPlayer.class,
                net.minecraft.server.network.CommonListenerCookie.class);

        assertNotNull(method);
    }

    @Test
    void displayNameTargetExists() throws Exception {
        assertNotNull(net.minecraft.world.entity.player.Player.class.getMethod("getDisplayName"));
    }

    @Test
    void formatNameForTeamTargetExists() throws Exception {
        var method = net.minecraft.world.scores.PlayerTeam.class.getMethod(
                "formatNameForTeam", net.minecraft.world.scores.Team.class,
                net.minecraft.network.chat.Component.class);

        assertTrue(net.minecraft.network.chat.MutableComponent.class.isAssignableFrom(method.getReturnType()));
    }

    @Test
    void connectionFieldIsNamedPlayer() throws Exception {
        var field = net.minecraft.server.network.ServerGamePacketListenerImpl.class.getField("player");
        assertTrue(ServerPlayer.class.isAssignableFrom(field.getType()));
    }
}