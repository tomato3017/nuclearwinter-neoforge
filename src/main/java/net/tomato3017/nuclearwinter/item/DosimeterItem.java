package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Film-badge dosimeter that independently absorbs radiation while in the hotbar.
 * Tracks cumulative exposure in a {@link DosimeterData} component on the stack.
 * The vanilla item durability bar is used as the always-visible absorption indicator.
 * Right-click while held to get a momentary action-bar readout of absorbed rads.
 * The item never breaks; at saturation it gains an enchantment glint.
 */
public class DosimeterItem extends Item {

    public DosimeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!isInHotbar(slotId)) return;
        if (level.getGameTime() % Config.RAYCAST_INTERVAL_TICKS.get() != 0) return;

        DosimeterData data = stack.getOrDefault(NWDataComponents.DOSIMETER_DATA, DosimeterData.DEFAULT);
        double maxRads = Config.DOSIMETER_MAX_RADS.get();

        if (data.isSaturated(maxRads)) return;

        double radsThisTick = player.getData(NWAttachmentTypes.PLAYER_DATA).lastReceivedRads();
        if (radsThisTick <= 0) return;

        double newAbsorbed = Math.min(data.absorbedRads() + radsThisTick, maxRads);
        stack.set(NWDataComponents.DOSIMETER_DATA, new DosimeterData(newAbsorbed));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            showReadout(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        DosimeterData data = stack.get(NWDataComponents.DOSIMETER_DATA);
        if (data == null) return false;
        return data.isSaturated(Config.DOSIMETER_MAX_RADS.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        DosimeterData data = stack.get(NWDataComponents.DOSIMETER_DATA);
        double maxRads = Config.DOSIMETER_MAX_RADS.get();

        if (data == null || data.absorbedRads() <= 0) {
            tooltipComponents.add(
                    Component.translatable("item.nuclearwinter.dosimeter.tooltip.no_readings")
                            .withStyle(ChatFormatting.GRAY));
            return;
        }

        if (data.isSaturated(maxRads)) {
            tooltipComponents.add(
                    Component.translatable("item.nuclearwinter.dosimeter.tooltip.saturated")
                            .withStyle(ChatFormatting.RED));
        } else {
            tooltipComponents.add(
                    Component.literal(String.format("%.0f / %.0f Rads", data.absorbedRads(), maxRads))
                            .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        DosimeterData data = stack.get(NWDataComponents.DOSIMETER_DATA);
        return data != null && data.absorbedRads() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        DosimeterData data = stack.getOrDefault(NWDataComponents.DOSIMETER_DATA, DosimeterData.DEFAULT);
        double maxRads = Config.DOSIMETER_MAX_RADS.get();
        double ratio = Math.min(data.absorbedRads() / maxRads, 1.0);
        return Math.round(13.0f * (float) ratio);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        DosimeterData data = stack.getOrDefault(NWDataComponents.DOSIMETER_DATA, DosimeterData.DEFAULT);
        double maxRads = Config.DOSIMETER_MAX_RADS.get();
        return colorForRatio(data.absorbedRads() / maxRads);
    }

    private void showReadout(ServerPlayer player, ItemStack stack) {
        DosimeterData data = stack.getOrDefault(NWDataComponents.DOSIMETER_DATA, DosimeterData.DEFAULT);
        double absorbed = data.absorbedRads();
        double maxRads = Config.DOSIMETER_MAX_RADS.get();

        Component message;
        if (data.isSaturated(maxRads)) {
            message = Component.translatable("item.nuclearwinter.dosimeter.readout.saturated")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000)).withBold(true));
        } else {
            int color = colorForRatio(absorbed / maxRads);
            message = Component.literal(String.format("▮ Absorbed: %.0f / %.0f Rads", absorbed, maxRads))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
        }
        player.displayClientMessage(message, true);
    }

    private static boolean isInHotbar(int slotId) {
        return slotId >= 0 && slotId < Inventory.getSelectionSize();
    }

    private static int colorForRatio(double ratio) {
        ratio = Math.min(ratio, 1.0);
        int red = (int) (255 * ratio);
        int green = (int) (255 * (1.0 - ratio));
        return (red << 16) | (green << 8);
    }
}
