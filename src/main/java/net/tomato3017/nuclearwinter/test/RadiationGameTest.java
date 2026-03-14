package net.tomato3017.nuclearwinter.test;

import net.tomato3017.nuclearwinter.radiation.BlockResolver;
import net.tomato3017.nuclearwinter.radiation.RadiationEmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("nuclearwinter")
@PrefixGameTestTemplate(false)
/**
 * GameTests covering core radiation math: block resistance values from {@link BlockResolver}
 * and raycast behaviour from {@link RadiationEmitter}.
 */
public class RadiationGameTest {

    @GameTest(template = "empty_1x1")
    public void stoneResistanceIsBaseline(GameTestHelper helper) {
        double resistance = BlockResolver.getResistance(Blocks.STONE.defaultBlockState());
        helper.assertTrue(Math.abs(resistance - 1.0) < 0.001,
                "Stone resistance should be 1.0, got " + resistance);
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void airResistanceIsZero(GameTestHelper helper) {
        double resistance = BlockResolver.getResistance(Blocks.AIR.defaultBlockState());
        helper.assertTrue(resistance == 0.0,
                "Air resistance should be 0.0, got " + resistance);
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void raycastOpenSkyReturnsSkyEmission(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(0, 1, 0));
        double result = RadiationEmitter.raycastDown(helper.getLevel(), pos, 333.0);
        helper.assertTrue(result > 300.0,
                "Open sky raycast should return near sky emission, got " + result);
        helper.succeed();
    }
}
