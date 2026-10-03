package dev.sn.mixin;

import dev.sn.core.ConfigManager;
import dev.sn.core.CoreMod;
import dev.sn.core.NicknameCache;
import dev.sn.core.NicknameHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Replaces the name a player is shown with, wherever it is displayed.
 *
 * <p>{@code getDisplayName} is called very often while a player list or a chat message is rendered,
 * so the result is cached for the rest of the tick. The cache is dropped whenever the nickname
 * changes, and when the name is requested from a thread that is not the server thread the original
 * value is returned, because parsing a nickname there would not be safe.
 */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements NicknameCache {
    @Unique
    private Component styledNicknames$cachedName = null;

    @Unique
    private int styledNicknames$cachedTick = -999;

    public PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyArg(
            method = "getDisplayName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/scores/PlayerTeam;formatNameForTeam"
                            + "(Lnet/minecraft/world/scores/Team;Lnet/minecraft/network/chat/Component;)"
                            + "Lnet/minecraft/network/chat/MutableComponent;"))
    private Component styledNicknames$replaceName(Component original) {
        try {
            if (!ConfigManager.get().data.changeDisplayName) {
                return original;
            }

            if (this.styledNicknames$cachedTick == this.tickCount) {
                return this.styledNicknames$cachedName != null ? this.styledNicknames$cachedName : original;
            }

            // Parsing a nickname walks the text tree, which is only safe on the server thread.
            if (!CoreMod.bridge().isServerThread(this)) {
                return original;
            }

            var holder = NicknameHolder.of(this);

            if (holder.shouldDisplay()) {
                var name = holder.getOutput();

                if (name != null) {
                    this.styledNicknames$cachedName = name;
                    this.styledNicknames$cachedTick = this.tickCount;
                    return name;
                }
            }

            this.styledNicknames$cachedName = null;
            this.styledNicknames$cachedTick = this.tickCount;
        } catch (Throwable e) {
            CoreMixinLog.error(e);
        }

        return original;
    }

    @Override
    public void styledNicknames$invalidateCache() {
        this.styledNicknames$cachedName = null;
        this.styledNicknames$cachedTick = -999;
    }
}