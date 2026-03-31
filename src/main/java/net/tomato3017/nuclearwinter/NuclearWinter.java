package net.tomato3017.nuclearwinter;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
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
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tomato3017.nuclearwinter.block.NWBlocks;
import net.tomato3017.nuclearwinter.command.DebugCommand;
import net.tomato3017.nuclearwinter.command.NuclearWinterCommand;
import net.tomato3017.nuclearwinter.data.DegradationRuleLoader;
import net.tomato3017.nuclearwinter.data.RadiationResistanceLoader;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.PlayerDataAttachment;
import net.tomato3017.nuclearwinter.datagen.NWBlockTagsProvider;
import net.tomato3017.nuclearwinter.effects.NWMobEffects;
import net.tomato3017.nuclearwinter.item.NWItems;
import net.tomato3017.nuclearwinter.radiation.EntityRadHandler;
import net.tomato3017.nuclearwinter.radiation.PlayerRadHandler;
import net.tomato3017.nuclearwinter.radiation.RadiationTier;
import net.tomato3017.nuclearwinter.stage.StageBase;
import net.tomato3017.nuclearwinter.stage.StageManager;
import net.tomato3017.nuclearwinter.tag.NWEntityTypeTags;
import net.tomato3017.nuclearwinter.world.NWBiomeModifiers;
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
                        output.accept(NWItems.WASTELAND_DIRT.get());
                        output.accept(NWItems.WASTELAND_RUBBLE.get());
                        output.accept(NWItems.DEADWOOD.get());
                        output.accept(NWItems.RUINED_PLANKS.get());
                        output.accept(NWItems.RUINED_PLANKS_FENCE.get());
                        output.accept(NWItems.RUINED_PLANKS_FENCE_GATE.get());
                        output.accept(NWItems.RUINED_PLANKS_SLAB.get());
                        output.accept(NWItems.RUINED_PLANKS_STAIRS.get());
                        output.accept(NWItems.LEAD_ORE.get());
                        output.accept(NWItems.DEEPSLATE_LEAD_ORE.get());
                        output.accept(NWItems.RAW_LEAD.get());
                        output.accept(NWItems.LEAD_INGOT.get());
                        output.accept(NWItems.LEAD_BLOCK.get());
                        output.accept(NWItems.REINFORCED_CONCRETE.get());
                        output.accept(NWItems.RADAWAY.get());
                        output.accept(NWItems.GEIGER_COUNTER.get());
                        output.accept(NWItems.DOSIMETER.get());
                        output.accept(NWItems.HAZMAT_T1_HELMET.get());
                        output.accept(NWItems.HAZMAT_T1_CHESTPLATE.get());
                        output.accept(NWItems.HAZMAT_T1_LEGGINGS.get());
                        output.accept(NWItems.HAZMAT_T1_BOOTS.get());
                        output.accept(NWItems.HAZMAT_T2_HELMET.get());
                        output.accept(NWItems.HAZMAT_T2_CHESTPLATE.get());
                        output.accept(NWItems.HAZMAT_T2_LEGGINGS.get());
                        output.accept(NWItems.HAZMAT_T2_BOOTS.get());
                        output.accept(NWItems.HAZMAT_T3_HELMET.get());
                        output.accept(NWItems.HAZMAT_T3_CHESTPLATE.get());
                        output.accept(NWItems.HAZMAT_T3_LEGGINGS.get());
                        output.accept(NWItems.HAZMAT_T3_BOOTS.get());
                    }).build());

    public NuclearWinter(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(NuclearWinter::onGatherData);

        CREATIVE_MODE_TABS.register(modEventBus);
        NWAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        NWBlocks.BLOCKS.register(modEventBus);
        NWItems.ITEMS.register(modEventBus);
        NWMobEffects.MOB_EFFECTS.register(modEventBus);
        NWBiomeModifiers.SERIALIZERS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("NuclearWinter common setup complete");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        stageManager.init(event.getServer());
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

    /**
     * Blocks sky-lit natural living spawns once the surface becomes a wasteland. Entity types in
     * {@link NWEntityTypeTags#SURFACE_RADIATION_IMMUNE} are exempt so custom post-apocalypse mobs
     * can opt out later.
     */
    @SubscribeEvent
    public void onMobSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (!LivingEntity.class.isAssignableFrom(event.getEntityType().getBaseClass())) {
            return;
        }
        if (event.getEntityType().is(NWEntityTypeTags.SURFACE_RADIATION_IMMUNE)) {
            return;
        }
        if (!isSuppressedSurfaceSpawnType(event.getSpawnType())) {
            return;
        }
        ServerLevelAccessor level = event.getLevel();
        if (level.getBrightness(LightLayer.SKY, event.getPos()) <= 0) {
            return;
        }
        ServerLevel serverLevel = level.getLevel();

        StageBase stage = stageManager.getStageForWorld(serverLevel.dimension());
        if (stage == null) {
            return;
        }
        if (stage.getStageIndex() >= Config.SURFACE_LIVING_SPAWN_BLOCK_MIN_STAGE.get()) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
        }
    }

    private static boolean isSuppressedSurfaceSpawnType(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION;
    }

    @SubscribeEvent
    public void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        EntityRadHandler.onLivingEntityTick(living);
    }

    @SubscribeEvent
    public void onPlayerHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerDataAttachment data = player.getData(NWAttachmentTypes.PLAYER_DATA);
        RadiationTier tier = RadiationTier.fromPool(data.radiationPool(), Config.PLAYER_POOL_MAX.get());

        switch (tier) {
            case CONTAMINATED:
                event.setAmount(event.getAmount() * 0.75f);
                break;
            case IRRADIATED:
                event.setAmount(event.getAmount() * 0.25f);
                break;
            case POISONED:
            case CRITICAL:
            case FATAL:
                event.setCanceled(true);
                break;
            default:
                break;
        }
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DegradationRuleLoader());
        event.addListener(new RadiationResistanceLoader());
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
