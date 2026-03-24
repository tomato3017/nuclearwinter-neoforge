package net.tomato3017.nuclearwinter.effects;

import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.PlayerDataAttachment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Beneficial effect applied by the RadAway item; drains the player's radiation pool
 * evenly over the configured duration.
 */
public class RadAwayEffect extends MobEffect {
    public RadAwayEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55FF55);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            double totalReduction = Config.RADAWAY_REDUCTION.get();
            int durationTicks = Config.RADAWAY_DURATION_TICKS.get();
            double drainPerTick = totalReduction / durationTicks;

            PlayerDataAttachment data = player.getData(NWAttachmentTypes.PLAYER_DATA);
            double newPool = Math.max(data.radiationPool() - drainPerTick, 0.0);
            player.setData(NWAttachmentTypes.PLAYER_DATA, data.withRadiationPool(newPool));
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
