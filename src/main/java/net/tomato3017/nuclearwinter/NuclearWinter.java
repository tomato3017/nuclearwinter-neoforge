package net.tomato3017.nuclearwinter;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.tomato3017.nuclearwinter.block.NWBlocks;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.item.NWItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(NuclearWinter.MODID)
public class NuclearWinter {
    public static final String MODID = "nuclearwinter";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NW_TAB = CREATIVE_MODE_TABS.register("nuclearwinter_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nuclearwinter"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> NWItems.LEAD_BLOCK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(NWItems.DEAD_GRASS.get());
                        output.accept(NWItems.DEAD_LEAVES.get());
                        output.accept(NWItems.PARCHED_DIRT.get());
                        output.accept(NWItems.WASTELAND_DUST.get());
                        output.accept(NWItems.CRACKED_STONE.get());
                        output.accept(NWItems.WASTELAND_RUBBLE.get());
                        output.accept(NWItems.DEADWOOD.get());
                        output.accept(NWItems.RUINED_PLANKS.get());
                        output.accept(NWItems.LEAD_BLOCK.get());
                        output.accept(NWItems.REINFORCED_CONCRETE.get());
                    }).build());

    public NuclearWinter(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        CREATIVE_MODE_TABS.register(modEventBus);
        NWAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        NWBlocks.BLOCKS.register(modEventBus);
        NWItems.ITEMS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("NuclearWinter common setup complete");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
