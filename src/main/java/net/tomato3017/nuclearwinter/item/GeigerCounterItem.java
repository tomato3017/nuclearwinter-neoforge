package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.radiation.RadiationEmitter;
import net.tomato3017.nuclearwinter.stage.StageBase;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * When held in the main hand, periodically shows local sky-raycast Rads/sec on the action bar.
 */
public class GeigerCounterItem extends Item {
    private static final int DISPLAY_INTERVAL = 10;

    public GeigerCounterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!isSelected) return;

        if (level.getGameTime() % DISPLAY_INTERVAL != 0) return;

        StageBase stage = NuclearWinter.getStageManager().getStageForWorld(level.dimension());
        double radsPerSec = 0.0;
        if (stage != null && stage.getSkyEmission() > 0) {
            radsPerSec = RadiationEmitter.raycastDown(level, player.blockPosition(), stage.getSkyEmission());
        }

        player.displayClientMessage(Component.literal(String.format("☢ %.1f Rads/sec", radsPerSec)), true);
    }
}
