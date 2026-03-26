package net.tomato3017.nuclearwinter.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Hazmat armor whose radiation mitigation per piece is {@link Config} tier value times a fixed slot weight.
 * Slot weights are shared with {@link net.tomato3017.nuclearwinter.radiation.PlayerRadHandler}.
 */
public class HazmatSuitItem extends ArmorItem {
    public static final float HELMET_WEIGHT = 0.15f;
    public static final float CHESTPLATE_WEIGHT = 0.40f;
    public static final float LEGGINGS_WEIGHT = 0.30f;
    public static final float BOOTS_WEIGHT = 0.15f;

    private static final DecimalFormat PERCENT_FMT = new DecimalFormat("0.0");

    private final HazmatTier tier;

    public HazmatSuitItem(Holder<ArmorMaterial> material, Type type, Properties properties, HazmatTier tier) {
        super(material, type, properties);
        this.tier = tier;
    }

    public HazmatTier getTier() {
        return tier;
    }

    public float getSlotWeight() {
        return switch (getType()) {
            case HELMET -> HELMET_WEIGHT;
            case CHESTPLATE -> CHESTPLATE_WEIGHT;
            case LEGGINGS -> LEGGINGS_WEIGHT;
            case BOOTS -> BOOTS_WEIGHT;
            default -> 0.0f;
        };
    }

    /** Contribution of this piece to total radiation reduction (0..1). */
    public double getPieceRadiationProtection() {
        return tier.getConfigProtection() * getSlotWeight();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        double piece = getPieceRadiationProtection();
        tooltipComponents.add(
                Component.translatable("item.nuclearwinter.hazmat.rad_protection",
                                PERCENT_FMT.format(piece * 100.0))
                        .withStyle(ChatFormatting.GRAY));
    }
}
