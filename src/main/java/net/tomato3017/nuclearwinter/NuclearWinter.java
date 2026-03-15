package net.tomato3017.nuclearwinter;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tomato3017.nuclearwinter.block.NWBlocks;
import net.tomato3017.nuclearwinter.command.DebugCommand;
import net.tomato3017.nuclearwinter.command.NuclearWinterCommand;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.datagen.NWBlockTagsProvider;
import net.tomato3017.nuclearwinter.item.NWItems;
import net.tomato3017.nuclearwinter.radiation.BlockResolver;
import net.tomato3017.nuclearwinter.radiation.PlayerRadHandler;
import net.tomato3017.nuclearwinter.stage.StageBase;
import net.tomato3017.nuclearwinter.stage.StageManager;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(NuclearWinter.MODID)
public class NuclearWinter {
    public static final String MODID = "nuclearwinter";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static StageManager stageManager = new StageManager();

    public static StageManager getStageManager() {
        return stageManager;
    }

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
        modEventBus.addListener(NuclearWinter::onGatherData);

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
        stageManager.init(event.getServer());
        BlockResolver.init();
        BlockResolver.registerBlockOverride(NWBlocks.LEAD_BLOCK.get(), Config.RESISTANCE_LEAD.get());
        BlockResolver.registerBlockOverride(NWBlocks.REINFORCED_CONCRETE.get(), Config.RESISTANCE_REINFORCED_CONCRETE.get());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        stageManager.shutdown();
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            stageManager.onWorldLoad(serverLevel);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            stageManager.onWorldUnload(serverLevel);
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        StageBase stage = stageManager.getStageForWorld(serverLevel.dimension());
        if (stage == null) return;

        stage.onChunkUnloaded(serverLevel, chunk);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        StageBase stage = stageManager.getStageForWorld(serverLevel.dimension());
        if (stage == null) return;

        stage.onChunkLoaded(serverLevel, chunk);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        stageManager.tickAllStages();
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerRadHandler.onPlayerTick(serverPlayer);
            DebugCommand.onPlayerTick(serverPlayer);
        }


    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NuclearWinterCommand.register(event.getDispatcher());
    }

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper efh = event.getExistingFileHelper();
        gen.addProvider(event.includeServer(),
                new NWBlockTagsProvider(output, lookupProvider, efh));
    }
}
