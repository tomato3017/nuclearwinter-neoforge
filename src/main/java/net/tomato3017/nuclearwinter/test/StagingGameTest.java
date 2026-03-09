package net.tomato3017.nuclearwinter.test;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.WorldDataAttachment;
import net.tomato3017.nuclearwinter.stage.GracePeriod;
import net.tomato3017.nuclearwinter.stage.Stage0;
import net.tomato3017.nuclearwinter.stage.StageBase;
import net.tomato3017.nuclearwinter.stage.StageManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("nuclearwinter")
@PrefixGameTestTemplate(false)
public class StagingGameTest {

    @GameTest(template = "empty_1x1")
    public void stageManagerStartsAtStage0(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        StageManager mgr = NuclearWinter.getStageManager();
        StageBase stage = mgr.getStageForWorld(level.dimension());
        helper.assertTrue(stage != null, "Stage should not be null");
        helper.assertTrue(stage.getStageIndex() == Stage0.INDEX, "Should start at Stage 0");
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void startApocalypseMovesToGrace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        StageManager mgr = NuclearWinter.getStageManager();
        mgr.startApocalypse(level);
        StageBase stage = mgr.getStageForWorld(level.dimension());
        helper.assertTrue(stage.getStageIndex() == GracePeriod.INDEX,
                "After start, should be at Grace Period");
        // Clean up
        mgr.stopApocalypse(level);
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void setStageUpdatesAttachment(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        StageManager mgr = NuclearWinter.getStageManager();
        mgr.setStage(level, 4);
        WorldDataAttachment data = level.getData(NWAttachmentTypes.WORLD_DATA);
        helper.assertTrue(data.stageIndex() == 4, "Attachment should reflect stage 4");
        // Clean up
        mgr.setStage(level, 0);
        helper.succeed();
    }
}
