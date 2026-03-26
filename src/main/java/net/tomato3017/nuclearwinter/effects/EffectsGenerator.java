package net.tomato3017.nuclearwinter.effects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.radiation.RadiationTier;

/**
 * Applies mob effects to a {@link ServerPlayer} based on their current {@link RadiationTier}.
 */
public class EffectsGenerator {
    private static final int EFFECT_DURATION = 60;
    private static final int DARKNESS_DURATION = 200;

    public static void applyPlayerEffects(ServerPlayer player, RadiationTier tier, int interval) {
        if (player.gameMode.isCreative()) return;
        switch (tier) {
            case FATAL -> {
                applyDamage(player, Config.FATAL_RADIATION_DAMAGE_PER_SEC.get().floatValue(), interval);
                applyDarkness(player);
                applyWeakness(player, 2);
                applySlowness(player, 2);
                applyHunger(player, 1);
            }
            case CRITICAL -> {
                applyDarkness(player);
                applyWeakness(player, 1);
                applySlowness(player, 1);
                applyHunger(player, 0);
            }
            case POISONED -> {
                applyWeakness(player, 0);
                applyHunger(player, 0);
                applySlowness(player, 0);
            }
            case IRRADIATED -> {
                applySlowness(player, 1);
                applyHunger(player, 0);
            }
            default -> {
            }
        }
    }

    public static void clearRadiationEffects(ServerPlayer player) {
        // Effects have short durations and expire naturally.
        // Darkness has a residual (DARKNESS_DURATION ticks) but fades on its own.
    }

    private static void applySlowness(ServerPlayer player, int amplifier) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION, amplifier, false, false, true));
    }

    private static void applyHunger(ServerPlayer player, int amplifier) {
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, EFFECT_DURATION, amplifier, false, false, true));
    }

    private static void applyWeakness(ServerPlayer player, int amplifier) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION, amplifier, false, false, true));
    }

    private static void applyDarkness(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_DURATION, 0, false, false, true));
    }

    private static void applyDamage(ServerPlayer player, float hpPerSec, int interval) {
        float damageApplied = hpPerSec * (interval / 20.0f);
        player.hurt(player.damageSources().magic(), damageApplied);
    }
}
