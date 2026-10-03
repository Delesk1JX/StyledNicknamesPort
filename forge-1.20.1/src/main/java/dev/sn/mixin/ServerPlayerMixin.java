package dev.sn.mixin;

import dev.sn.core.ConfigManager;
import dev.sn.core.CoreMod;
import dev.sn.core.NicknameHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the name shown in the player list, which the config keeps switched off by default.
 *
 * <p>This mixin lives in the platform source set because the constructor of {@code Player} takes
 * different arguments on each targeted game version, and a mixin has to declare a matching one.
 * {@code getTabListDisplayName} itself has the same shape everywhere.
 *
 * <p>The game caches a player's tab list name once it has been computed, so the replacement has to
 * happen on every call rather than only the first.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos spawn, float angle, com.mojang.authlib.GameProfile gameProfile) {
        super(level, spawn, angle, gameProfile);
    }

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void styledNicknames$replaceTabListName(CallbackInfoReturnable<Component> cir) {
        try {
            if (!ConfigManager.get().data.changePlayerListName) {
                return;
            }

            var holder = NicknameHolder.of(this);

            if (holder.shouldDisplay()) {
                var output = holder.getOutput();

                if (output != null) {
                    cir.setReturnValue(PlayerTeam.formatNameForTeam(this.getTeam(), output));
                }
            }
        } catch (Throwable e) {
            CoreMod.LOGGER.error("Styled Nicknames could not build a tab list name", e);
        }
    }
}