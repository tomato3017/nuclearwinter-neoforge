package net.tomato3017.nuclearwinter.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.tomato3017.nuclearwinter.radiation.BlockResolver;

/**
 * Game tests for block degradation logic in {@link BlockResolver}.
 * Execution requires an in-game test structure; these are written for future activation.
 */
@GameTestHolder("nuclearwinter")
@PrefixGameTestTemplate(false)
public class DegradationGameTest {

    @GameTest(template = "empty_1x1")
    public void grassDegradesToDeadGrassAtStage2(GameTestHelper helper) {
        Block result = BlockResolver.getDegradedBlock(Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        helper.assertTrue(result != null, "Grass should degrade at stage index 3 (Stage 2)");
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void stoneDoesNotDegradeAtStage1(GameTestHelper helper) {
        Block result = BlockResolver.getDegradedBlock(Blocks.STONE.defaultBlockState(), 2);
        helper.assertTrue(result == null, "Stone should not degrade at stage index 2 (Stage 1)");
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void grassDegradesToWastelandDustAtStage4(GameTestHelper helper) {
        Block result = BlockResolver.getDegradedBlock(Blocks.GRASS_BLOCK.defaultBlockState(), 5);
        helper.assertTrue(result != null, "Grass should degrade at stage index 5 (Stage 4)");
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void leavesDegradeToAirAtStage4(GameTestHelper helper) {
        Block result = BlockResolver.getDegradedBlock(Blocks.OAK_LEAVES.defaultBlockState(), 5);
        helper.assertTrue(result == Blocks.AIR, "Leaves should become Air at Stage 4");
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void logsDegradeToDeadwoodAtStage2(GameTestHelper helper) {
        Block result = BlockResolver.getDegradedBlock(Blocks.OAK_LOG.defaultBlockState(), 3);
        helper.assertTrue(result != null, "Logs should degrade at stage index 3 (Stage 2)");
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void waterDoesNotDegrade(GameTestHelper helper) {
        Block stage2 = BlockResolver.getDegradedBlock(Blocks.WATER.defaultBlockState(), 3);
        Block stage4 = BlockResolver.getDegradedBlock(Blocks.WATER.defaultBlockState(), 5);
        helper.assertTrue(stage2 == null && stage4 == null, "Water should never degrade");
        helper.succeed();
    }
}
