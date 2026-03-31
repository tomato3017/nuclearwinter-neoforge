package net.tomato3017.nuclearwinter.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.PlayerDataAttachment;
import net.tomato3017.nuclearwinter.effects.EffectsGenerator;
import net.tomato3017.nuclearwinter.effects.NWMobEffects;
import net.tomato3017.nuclearwinter.item.HazmatSuitItem;
import net.tomato3017.nuclearwinter.stage.StageBase;

/**
 * Drives per-player radiation accumulation and passive drain on a configurable tick interval.
 * Calls {@link RadiationEmitter} to determine exposure, then reads/writes the player's
 * {@link net.tomato3017.nuclearwinter.data.PlayerDataAttachment} radiation pool.
 */
public class PlayerRadHandler {
    // TODO: Consider optimization pass for radiation calculations if performance becomes an issue

    public static boolean isImmune(ServerPlayer player) {
        return player.hasEffect(NWMobEffects.RADIATION_IMMUNITY);
    }

    public static void onPlayerTick(ServerPlayer player) {
        int interval = Config.RAYCAST_INTERVAL_TICKS.get();
        long currentTick = player.level().getGameTime();

        if (currentTick % interval != 0) return;
        if (isImmune(player)) return;

        StageBase stage = NuclearWinter.getStageManager()
                .getStageForWorld(player.level().dimension());
        if (stage == null || stage.getSkyEmission() <= 0) {
            drainIfUnexposed(player, currentTick, interval);
            return;
        }

        double skyEmission = stage.getSkyEmission();
        double radsPerSec = getExposure(player, skyEmission, currentTick);
        double hazmatProtection = getHazmatProtection(player);
        if (hazmatProtection > 0) {
            radsPerSec = radsPerSec * (1.0 - Math.min(hazmatProtection, 1.0));
        }
        double radsThisTick = radsPerSec * (interval / 20.0);

        PlayerDataAttachment data = player.getData(NWAttachmentTypes.PLAYER_DATA);
        double pool = data.radiationPool();
        double poolMax = Config.PLAYER_POOL_MAX.get();

        if (radsPerSec > 0) {
            pool = Math.min(pool + radsThisTick, poolMax);
        } else {
            double drainAmount = Config.PASSIVE_DRAIN_RATE.get() * (interval / 20.0);
            pool = Math.max(pool - drainAmount, 0.0);
        }

        player.setData(NWAttachmentTypes.PLAYER_DATA, data.withRadiationPool(pool));

        RadiationTier tier = RadiationTier.fromPool(pool, poolMax);
        EffectsGenerator.applyPlayerEffects(player, tier, interval);
    }

    private static double getExposure(ServerPlayer player, double skyEmission, long currentTick) {
        BlockPos currentPos = player.blockPosition();
        return RadiationEmitter.raycastDown(player.level(), currentPos, skyEmission);
    }

    /**
     * Sum of per-piece radiation reduction from worn {@link HazmatSuitItem} pieces
     * (config tier value × slot weight), capped at 1.0.
     */
    public static double getHazmatProtection(ServerPlayer player) {
        double total = 0.0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof HazmatSuitItem hazmat) {
                total += hazmat.getPieceRadiationProtection();
            }
        }
        return Math.min(total, 1.0);
    }

    private static void drainIfUnexposed(ServerPlayer player, long currentTick, int interval) {
        PlayerDataAttachment data = player.getData(NWAttachmentTypes.PLAYER_DATA);
        double pool = data.radiationPool();
        if (pool > 0) {
            double drainAmount = Config.PASSIVE_DRAIN_RATE.get() * (interval / 20.0);
            pool = Math.max(pool - drainAmount, 0.0);
            player.setData(NWAttachmentTypes.PLAYER_DATA, data.withRadiationPool(pool));
        }
    }
}
