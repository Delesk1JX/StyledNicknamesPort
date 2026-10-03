package dev.sn.mixin;

import dev.sn.core.Config;
import dev.sn.core.CoreMod;
import dev.sn.core.ConfigManager;
import dev.sn.core.NicknameCache;
import dev.sn.core.NicknameHolder;
import dev.sn.core.NicknameStorage;
import dev.sn.core.ParserUtils;
import dev.sn.core.Permissions;
import dev.sn.text.ParserContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

/**
 * Holds the nickname of one player.
 *
 * <p>The state lives on the player's connection handler because that object is created together
 * with the player and discarded with it. Two values are kept: the nickname as typed, and the same
 * text already parsed into a component, so that rendering a name many times per frame does not
 * re-parse anything.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements NicknameHolder {
    @Shadow
    public ServerPlayer player;

    @Unique
    private String styledNicknames$nickname = null;

    @Unique
    private Component styledNicknames$parsed = null;

    /** True when the nickname only adds formatting to the real name. */
    @Unique
    private boolean styledNicknames$formattingOnly = false;

    /** True when the player chose the nickname themselves, so its formatting needs permission checks. */
    @Unique
    private boolean styledNicknames$selfChosen = true;

    @Override
    public void loadData() {
        NicknameStorage.apply(this.player);
    }

    @Override
    public void setNickname(String nickname, boolean requirePermission) {
        if (nickname == null || nickname.isEmpty() || (requirePermission && !mayUseNicknames())) {
            this.styledNicknames$nickname = null;
            this.styledNicknames$parsed = null;
            this.styledNicknames$selfChosen = false;
            NicknameStorage.write(this.player, null, false);
        } else {
            this.styledNicknames$nickname = nickname;
            this.styledNicknames$selfChosen = requirePermission;
            NicknameStorage.write(this.player, nickname, requirePermission);

            // A nickname the player typed is parsed with only the tags their permissions allow, while
            // one set by the server is parsed with everything available.
            var parsed = ParserUtils.parseNickname(requirePermission ? this.player : null, nickname);

            this.styledNicknames$formattingOnly = parsed.getString().equalsIgnoreCase(CoreMod.bridge().nameOf(this.player));
            this.styledNicknames$parsed = parsed;
        }

        broadcastTabListName();

        if (this.player instanceof NicknameCache cache) {
            cache.styledNicknames$invalidateCache();
        }
    }

    private boolean mayUseNicknames() {
        var config = ConfigManager.get().data;
        return config.allowByDefault || Permissions.check(this.player, Permissions.LEVEL_GAMEMASTERS);
    }

    /**
     * Tells every client that has this player in its list about the new name.
     */
    private void broadcastTabListName() {
        if (!ConfigManager.get().data.changePlayerListName || this.player.connection == null) {
            return;
        }

        var server = this.player.level().getServer();

        if (server != null) {
            server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, this.player));
        }
    }

    @Override
    public String getNickname() {
        return this.styledNicknames$nickname;
    }

    @Override
    public Component getParsedNickname() {
        return this.styledNicknames$parsed;
    }

    @Override
    public MutableComponent getOutput() {
        if (this.styledNicknames$parsed == null) {
            return null;
        }

        var config = ConfigManager.get();
        var node = this.styledNicknames$formattingOnly ? config.nicknameFormatColor : config.nicknameFormat;

        return node.toComponent(ParserContext.of(Config.KEY, placeholderValue())).copy();
    }

    @Override
    public MutableComponent getOutputOrVanilla() {
        if (this.styledNicknames$parsed != null) {
            return getOutput();
        }

        return this.player.getName().copy();
    }

    @Override
    public boolean requiresPermission() {
        return this.styledNicknames$selfChosen;
    }

    @Override
    public boolean shouldDisplay() {
        if (this.styledNicknames$parsed == null) {
            return false;
        }

        if (!this.styledNicknames$selfChosen) {
            return true;
        }

        var config = ConfigManager.get().data;
        return config.allowByDefault || Permissions.check(this.player, Permissions.LEVEL_GAMEMASTERS);
    }

    @Override
    public Function<String, Component> placeholderValue() {
        var name = this.styledNicknames$parsed == null
                ? this.player.getName().copy()
                : getOutputOrVanilla();

        return x -> name;
    }
}