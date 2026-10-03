package dev.sn.neoforge;

import dev.sn.core.Commands;
import dev.sn.core.ConfigManager;
import dev.sn.core.CoreMod;
import dev.sn.core.ModInfo;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Mod entry point. The platform specific parts live in {@link NeoForgeBridge}, everything shared
 * lives in {@code ../common}.
 */
@Mod(CoreMod.ID)
public final class NeoForgeEntrypoint {
    public NeoForgeEntrypoint(IEventBus modBus) {
        CoreMod.bootstrap(new NeoForgeBridge());
        CoreMod.bridge().bootstrap();
        NeoForge.EVENT_BUS.register(NeoForgeEntrypoint.class);
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