package net.tomato3017.nuclearwinter.radiation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.stage.StageBase;
import net.tomato3017.nuclearwinter.stage.StageType;
import net.tomato3017.nuclearwinter.tag.NWEntityTypeTags;

/**
 * Applies direct magic damage to non-player {@link LivingEntity} instances from sky radiation
 * exposure computed by {@link RadiationEmitter}. Once Stage 3 begins, sky-lit non-immune living
 * entities additionally take a guaranteed wasteland damage tick so outdoor life dies off quickly
 * while genuinely sheltered entities still survive.
 */
public final class EntityRadHandler {

    private static final int TICKS_PER_SECOND = 20;

    private EntityRadHandler() {
    }

    public static void onLivingEntityTick(LivingEntity entity) {
        if (entity instanceof ServerPlayer || entity.getType().is(NWEntityTypeTags.SURFACE_RADIATION_IMMUNE)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if ((level.getGameTime() + entity.getId()) % TICKS_PER_SECOND != 0) {
            return;
        }

        StageBase stage = NuclearWinter.getStageManager().getStageForWorld(level.dimension());
        if (stage == null || stage.getSkyEmission() <= 0) {
            return;
        }

        if (!stage.getStageType().isAtLeast(StageType.STAGE_3)) {
            return;
        }

        float damage = getRadiationDamage(level, entity, stage);
        if (damage <= 0.0f) {
            return;
        }

        entity.hurt(entity.damageSources().magic(), damage);
    }

    private static float getRadiationDamage(ServerLevel level, LivingEntity entity, StageBase stage) {
        float damage = 0.0f;

        if (stage.getStageIndex() >= Config.SURFACE_LIVING_DEATH_MIN_STAGE.get()
                && level.getBrightness(LightLayer.SKY, entity.blockPosition()) > 0) {
            damage = Math.max(damage, Config.SURFACE_LIVING_DAMAGE_PER_SEC.get().floatValue());
        }

        return damage;
    }
}
