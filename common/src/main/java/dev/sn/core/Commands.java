package dev.sn.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * {@code /nickname}, {@code /nickname set}, {@code /nickname clear}, {@code /realname} and the
 * administrative {@code /styled-nicknames} tree.
 */
public final class Commands {
    private static final Predicate<CommandSourceStack> MAY_USE_NICKNAMES = Permissions::mayUseNicknames;

    private static final SuggestionProvider<CommandSourceStack> OWN_NICKNAME = (context, builder) ->
            SharedSuggestionProvider.suggest(currentNickname(context.getSource().getPlayer()), builder);

    private static final SuggestionProvider<CommandSourceStack> OTHER_NICKNAME = (context, builder) ->
            SharedSuggestionProvider.suggest(currentNickname(EntityArgument.getPlayer(context, "player")), builder);

    private static final SuggestionProvider<CommandSourceStack> ONLINE_NICKNAMES = (context, builder) -> {
        var names = context.getSource().getServer().getPlayerList().getPlayers().stream()
                .map(NicknameHolder::of)
                .map(NicknameHolder::getOutput)
                .filter(Objects::nonNull)
                .map(Component::getString)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        return SharedSuggestionProvider.suggest(names, builder);
    };

    private Commands() {
    }

public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var nickname = dispatcher.register(
                literal("nickname")
                        .requires(MAY_USE_NICKNAMES)
                        .then(literal("set")
                                .then(argument("nickname", StringArgumentType.greedyString())
                                        .suggests(OWN_NICKNAME)
                                        .executes(Commands::set)))
                        .then(literal("clear").executes(Commands::clear)));

        // /nick is a shorter alias, kept because the upstream mod has it.
        dispatcher.register(
                literal("nick")
                        .requires(MAY_USE_NICKNAMES)
                        .redirect(nickname));

        dispatcher.register(
                literal("realname")
                        .requires(MAY_USE_NICKNAMES)
                        .then(argument("nickname", StringArgumentType.greedyString())
                                .suggests(ONLINE_NICKNAMES)
                                .executes(Commands::realname)));

        dispatcher.register(
                literal("styled-nicknames")
                        .executes(Commands::about)
                        .then(literal("reload")
                                .requires(Permissions::mayReloadConfig)
                                .executes(Commands::reload))
                        .then(literal("set")
                                .requires(Permissions::mayChangeOthers)
                                .then(argument("player", EntityArgument.player())
                                        .then(argument("nickname", StringArgumentType.greedyString())
                                                .suggests(OTHER_NICKNAME)
                                                .executes(Commands::setOther))))
                        .then(literal("clear")
                                .requires(Permissions::mayChangeOthers)
                                .then(argument("player", EntityArgument.player())
                                        .executes(Commands::clearOther))));
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var nickname = StringArgumentType.getString(context, "nickname");
        var config = ConfigManager.get();

        if (config.data.maxLength > 0) {
            var rendered = ParserUtils.parseNickname(player, nickname).getString();

            if (rendered.length() > config.data.maxLength && !Permissions.mayIgnoreLengthLimit(context.getSource())) {
                context.getSource().sendSuccess(() -> config.tooLongMessage, false);
                return 0;
            }
        }

        if (nickname.contains(" ") && !config.data.allowSpacesInNicknames) {
            context.getSource().sendSuccess(() -> config.spacesNotAllowedMessage, false);
            return 0;
        }

        var holder = NicknameHolder.of(player);
        holder.setNickname(nickname, true);

        context.getSource().sendSuccess(() -> config.render(config.changedMessage, holder.placeholderValue()), false);
        return 1;
    }

private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        NicknameHolder.of(player).setNickname(null, false);

        var config = ConfigManager.get();
        var name = player.getName();

        context.getSource().sendSuccess(() -> config.render(config.resetMessage, x -> name), false);
        return 1;
    }

    private static int setOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");
        var nickname = StringArgumentType.getString(context, "nickname");

        NicknameHolder.of(player).setNickname(nickname, false);

        context.getSource().sendSuccess(() -> Component.translatable("Changed nickname of %s to %s",
                player.getDisplayName(), NicknameHolder.of(player).getOutputOrVanilla()), false);

        return 1;
    }

    private static int clearOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");
        NicknameHolder.of(player).setNickname(null, false);

        context.getSource().sendSuccess(() -> Component.translatable("Cleared nickname of %s", player.getDisplayName()), false);
        return 1;
    }

    private static int realname(CommandContext<CommandSourceStack> context) {
        var nickname = StringArgumentType.getString(context, "nickname");
        Map<ServerPlayer, Component> found = new java.util.LinkedHashMap<>();

        for (var player : context.getSource().getServer().getPlayerList().getPlayers()) {
            var output = NicknameHolder.of(player).getOutput();

            if (output != null && output.getString().equals(nickname)) {
                found.put(player, output);
            }
        }

        if (found.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No player with that nickname is currently online."));
            return 0;
        }

        if (found.size() > 1) {
            context.getSource().sendSuccess(() -> Component.translatable("Found %s players with that nickname:", found.size()), false);
        }

        found.forEach((player, output) -> context.getSource().sendSuccess(
                () -> Component.translatable("The real name of %s is %s.", player.getDisplayName(), player.getScoreboardName()), false));

        return found.size();
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        var reloaded = ConfigManager.load(CoreMod.bridge().configDir());

        if (reloaded) {
            context.getSource().sendSuccess(() -> Component.literal("Reloaded config!"), false);
        } else {
            context.getSource().sendFailure(Component.literal("Could not read the config, defaults are in use.")
                    .withStyle(ChatFormatting.RED));
        }

        return reloaded ? 1 : 0;
    }

    private static int about(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(CoreMod.NAME)
                .withStyle(ChatFormatting.BLUE)
                .append(Component.literal(" - " + ModInfo.VERSION).withStyle(ChatFormatting.WHITE)), false);

        return 1;
    }

    private static Collection<String> currentNickname(ServerPlayer player) {
        if (player == null) {
            return Collections.emptyList();
        }

        var nickname = NicknameHolder.of(player).getNickname();
        return nickname != null ? Collections.singletonList(nickname) : Collections.emptyList();
    }

}