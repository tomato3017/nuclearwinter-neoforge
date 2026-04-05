package net.tomato3017.nuclearwinter.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.PlayerDataAttachment;
import net.tomato3017.nuclearwinter.radiation.RadiationEmitter;
import net.tomato3017.nuclearwinter.stage.StageBase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * When held in the main hand, periodically shows local sky-raycast Rads/sec on the action bar
 * and plays a click sound whose rate scales with the player's current radiation exposure.
 */
public class GeigerCounterItem extends Item {
    private static final int DISPLAY_INTERVAL = 10;
    private static final int MIN_CLICK_INTERVAL = 2;
    private static final int MAX_CLICK_INTERVAL = 40;

    private static final Map<UUID, Long> lastClickTick = new HashMap<>();

    public GeigerCounterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!isSelected) return;

        long gameTime = level.getGameTime();

        if (gameTime % DISPLAY_INTERVAL == 0) {
            StageBase stage = NuclearWinter.getStageManager().getStageForWorld(level.dimension());
            double radsPerSec = 0.0;
            if (stage != null && stage.getSkyEmission() > 0) {
                radsPerSec = RadiationEmitter.raycastDown(level, player.blockPosition(), stage.getSkyEmission());
            }
            player.displayClientMessage(Component.literal(String.format("☢ %.1f Rads/sec", radsPerSec)), true);
        }

        tickClickSound(player, gameTime);
    }

    private static void tickClickSound(ServerPlayer player, long gameTime) {
        PlayerDataAttachment data = player.getData(NWAttachmentTypes.PLAYER_DATA);
        double radsPerSec = data.lastReceivedRads() * 20.0 / Config.RAYCAST_INTERVAL_TICKS.get();
        int interval = calcClickInterval(radsPerSec);
        if (interval == 0) return;

        long last = lastClickTick.getOrDefault(player.getUUID(), 0L);
        if (gameTime - last >= interval) {
            float volume = (float) Math.min(0.4 + (radsPerSec * 0.02), 0.8);
            player.playNotifySound(NWSounds.GEIGER_CLICK.get(), SoundSource.PLAYERS, volume, 1.0f);
            lastClickTick.put(player.getUUID(), gameTime);
        }
    }

    /**
     * Maps rads/sec to a click interval in ticks. Returns 0 when there is no exposure.
     * At 1 rad/sec the interval is {@value MAX_CLICK_INTERVAL} ticks; it shrinks toward
     * {@value MIN_CLICK_INTERVAL} ticks as exposure rises.
     */
    private static int calcClickInterval(double radsPerSec) {
        if (radsPerSec <= 0) return 0;
        return (int) Math.max(MIN_CLICK_INTERVAL, MAX_CLICK_INTERVAL / radsPerSec);
    }

    public static void clearPlayerClickState(UUID uuid) {
        lastClickTick.remove(uuid);
    }
}
