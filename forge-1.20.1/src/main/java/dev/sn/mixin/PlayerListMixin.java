package dev.sn.mixin;

import dev.sn.core.NicknameHolder;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Loads the stored nickname while a player is joining.
 *
 * <p>The injection runs right after the client is told about its own permission level, which is late
 * enough for the player's data to be loaded and early enough that the first packet the player
 * receives already carries the nickname.
 *
 * <p>This lives in the platform source set because {@code placeNewPlayer} takes a login cookie on
 * NeoForge but not on Forge.
 */
@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel"
                            + "(Lnet/minecraft/server/level/ServerPlayer;)V",
                    shift = At.Shift.AFTER))
    private void styledNicknames$loadNickname(Connection connection, ServerPlayer player, CallbackInfo ci) {
        NicknameHolder.of(player).loadData();
    }
}