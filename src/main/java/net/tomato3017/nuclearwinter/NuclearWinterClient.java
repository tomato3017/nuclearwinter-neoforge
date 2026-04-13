package net.tomato3017.nuclearwinter;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.tomato3017.nuclearwinter.block.NWBlocks;
import net.tomato3017.nuclearwinter.client.GeigerSoundHandler;
import net.tomato3017.nuclearwinter.util.SampleWorldInstaller;

/**
 * Client-only mod class. Safe to reference client-only Minecraft classes here.
 * Handles client lifecycle events and registers client-side handlers such as block colors.
 */
@Mod(value = NuclearWinter.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = NuclearWinter.MODID, value = Dist.CLIENT)
public class NuclearWinterClient {
    public NuclearWinterClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(NuclearWinterClient::onRegisterBlockColors);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        SampleWorldInstaller.install();
    }

    @SubscribeEvent
    static void onClientPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        GeigerSoundHandler.stopAll();
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        GeigerSoundHandler.tick();
    }

    /**
     * Tints the grayscale vanilla grass textures on dead_grass with a golden-olive color
     * sampled from dead_grass_colormap.png, applied uniformly regardless of biome.
     */
    static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> 0x7A5C10,
                NWBlocks.DEAD_GRASS.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> 0x7A6A52,
                NWBlocks.DEAD_LEAVES.get()
        );
    }
}
