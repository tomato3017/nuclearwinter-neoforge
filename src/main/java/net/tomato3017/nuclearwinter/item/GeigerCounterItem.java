package net.tomato3017.nuclearwinter.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.network.GeigerLevel;
import net.tomato3017.nuclearwinter.network.GeigerLevelPayload;
import net.tomato3017.nuclearwinter.radiation.RadiationEmitter;
import net.tomato3017.nuclearwinter.stage.StageBase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * When held in the main hand, periodically shows local sky-raycast Rads/sec on the action bar
 * and sends the client a {@link GeigerLevelPayload} so it can play the matching looping ambient track.
 * Always measures raw sky emission via {@link RadiationEmitter#raycastDown} — before suit protection —
 * so readings reflect environmental radiation regardless of what the player is wearing.
 * Threshold calibration is supplied by the {@link GeigerCounterMode} passed at construction time.
 */
public class GeigerCounterItem extends Item {
    private static final int DISPLAY_INTERVAL = 10;

    private static final Map<UUID, GeigerLevel> lastSentLevel = new HashMap<>();

    private final GeigerCounterMode mode;

    public GeigerCounterItem(Properties properties, GeigerCounterMode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;

        // Not the selected (offhand) item — silence the geiger unless the main hand is also a
        // geiger counter (which will handle sound itself on its own tick).
        if (!isSelected) {
            if (!(player.getMainHandItem().getItem() instanceof GeigerCounterItem)) {
                sendLevelIfChanged(player, GeigerLevel.NONE);
            }
            return;
        }

        double radsPerSec = 0.0;
        StageBase stage = NuclearWinter.getStageManager().getStageForWorld(level.dimension());
        if (stage != null && stage.getSkyEmission() > 0) {
            radsPerSec = RadiationEmitter.raycastDown(level, player.blockPosition(), stage.getSkyEmission());
        }

        long gameTime = level.getGameTime();
        if (gameTime % DISPLAY_INTERVAL == 0) {
            if (radsPerSec > this.mode.maxRads()) {
                player.displayClientMessage(Component.literal("Off Scale"), true);
            } else {
                player.displayClientMessage(Component.literal(String.format("☢ %.1f Rads/sec", radsPerSec)), true);
            }
        }

        GeigerLevel newLevel = GeigerLevel.fromRadsPerSec(radsPerSec, mode.maxRads());
        sendLevelIfChanged(player, newLevel);
    }

    private static void sendLevelIfChanged(ServerPlayer player, GeigerLevel newLevel) {
        GeigerLevel current = lastSentLevel.getOrDefault(player.getUUID(), GeigerLevel.NONE);
        if (current == newLevel) return;

        if (newLevel == GeigerLevel.NONE) {
            lastSentLevel.remove(player.getUUID());
        } else {
            lastSentLevel.put(player.getUUID(), newLevel);
        }
        PacketDistributor.sendToPlayer(player, new GeigerLevelPayload(newLevel));
    }

    public static void clearPlayerState(UUID uuid) {
        lastSentLevel.remove(uuid);
    }

    /**
     * Sends {@link GeigerLevel#NONE} if the player has an active Geiger level tracked but is no
     * longer holding any Geiger counter in their main hand. Called every server tick to ensure
     * the client loop stops promptly when the item is dropped or deleted.
     */
    public static void clearPlayerStateIfNotHeld(ServerPlayer player) {
        if (!lastSentLevel.containsKey(player.getUUID())) return;
        if (!(player.getMainHandItem().getItem() instanceof GeigerCounterItem)) {
            sendLevelIfChanged(player, GeigerLevel.NONE);
        }
    }
}
