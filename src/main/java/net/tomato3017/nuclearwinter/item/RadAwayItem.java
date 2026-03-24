package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.effects.NWMobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Consumable item that applies the RadAway mob effect, draining radiation over time.
 */
public class RadAwayItem extends Item {
    public RadAwayItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int duration = Config.RADAWAY_DURATION_TICKS.get();
            player.addEffect(new MobEffectInstance(NWMobEffects.RADAWAY, duration, 0, false, true, true));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
