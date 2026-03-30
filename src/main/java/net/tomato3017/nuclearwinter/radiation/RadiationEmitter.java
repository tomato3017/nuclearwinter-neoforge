package net.tomato3017.nuclearwinter.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.tomato3017.nuclearwinter.Config;

/**
 * Performs a downward raycast from the world ceiling to the player's position,
 * attenuating the sky radiation by each solid block's resistance value.
 * Returns 0 once radiation drops below the configured floor constant.
 */
public class RadiationEmitter {

    public static double raycastDown(Level level, BlockPos playerPos, double skyEmission) {
        if (skyEmission <= 0) return 0.0;

        double floorConstant = Config.FLOOR_CONSTANT.get();
        double currentRad = skyEmission;
        // TODO Apply the floor check before the block loop so open-sky exposure below the floor constant returns 0.0.

        int skyLight = level.getBrightness(LightLayer.SKY, playerPos);
        double penalty = Config.SKY_LIGHT_RESISTANCE_PENALTY.get();
        double normalizedLight = skyLight / 15.0;
        double effectivenessMult = 1.0 - (penalty * normalizedLight * normalizedLight);

        //TODO adjust to height map instead of max build height
        int topY = level.getMaxBuildHeight();
        int playerY = playerPos.getY();

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(
                playerPos.getX(), topY, playerPos.getZ());

        for (int y = topY - 1; y >= playerY; y--) {
            mutablePos.setY(y);
            BlockState state = level.getBlockState(mutablePos);

            if (state.isAir()) continue;

            double resistance = BlockResolver.getResistance(state);
            if (resistance <= 0) continue;

            // TODO Adjust block resistances to match new formula.
            currentRad *= Math.pow(0.5, resistance * effectivenessMult);

            if (currentRad < floorConstant) {
                return 0.0;
            }
        }

        return currentRad;
    }
}
