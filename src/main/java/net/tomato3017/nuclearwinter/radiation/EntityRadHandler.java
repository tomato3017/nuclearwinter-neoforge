package net.tomato3017.nuclearwinter.radiation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.stage.StageBase;

/**
 * Applies direct magic damage to non-player {@link LivingEntity} instances from sky radiation
 * exposure computed by {@link RadiationEmitter}. Stateless (no pool); runs once per second on
 * the server via {@link net.neoforged.neoforge.event.tick.EntityTickEvent.Post}.
 */
public final class EntityRadHandler {

    private static final int TICKS_PER_SECOND = 20;

    private EntityRadHandler() {
    }

    public static void onLivingEntityTick(LivingEntity entity) {
        if (entity instanceof ServerPlayer) {
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

        double exposure = RadiationEmitter.raycastDown(level, entity.blockPosition(), stage.getSkyEmission());
        if (exposure <= Config.FLOOR_CONSTANT.get()) {
            return;
        }

        float damage = (float) (exposure * Config.ENTITY_RADIATION_DAMAGE_SCALE.get());
        entity.hurt(entity.damageSources().magic(), damage);
    }
}
