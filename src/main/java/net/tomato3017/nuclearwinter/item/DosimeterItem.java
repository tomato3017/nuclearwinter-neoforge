package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.PlayerDataAttachment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * When carried in the hotbar, periodically shows the player's radiation pool with a color gradient.
 */
public class DosimeterItem extends Item {
    private static final int DISPLAY_INTERVAL = 20;

    public DosimeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!isInHotbar(slotId)) return;
        if (level.getGameTime() % DISPLAY_INTERVAL != 0) return;

        PlayerDataAttachment data = player.getData(NWAttachmentTypes.PLAYER_DATA);
        double pool = data.radiationPool();
        int color = getColorForRads(pool);

        player.displayClientMessage(
                Component.literal(String.format("▮ Radiation: %.0f Rads", pool))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                true);
    }

    private static boolean isInHotbar(int slotId) {
        return slotId >= 0 && slotId < Inventory.getSelectionSize();
    }

    public static int getColorForRads(double pool) {
        double fullRed = Config.DOSIMETER_FULL_RED.get();
        double ratio = Math.min(pool / fullRed, 1.0);

        int red = (int) (255 * ratio);
        int green = (int) (255 * (1.0 - ratio));
        return (red << 16) | (green << 8);
    }
}
