package dev.sn.forge;

import dev.sn.core.Commands;
import dev.sn.core.ConfigManager;
import dev.sn.core.CoreMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mod entry point. The platform specific parts live in {@link ForgeBridge}, everything shared lives
 * in {@code ../common}.
 */
@Mod(CoreMod.ID)
public final class ForgeEntrypoint {
    public ForgeEntrypoint() {
        CoreMod.bootstrap(new ForgeBridge());
        CoreMod.bridge().bootstrap();

        MinecraftForge.EVENT_BUS.register(ForgeEntrypoint.class);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        Commands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ConfigManager.load(CoreMod.bridge().configDir());
    }
}