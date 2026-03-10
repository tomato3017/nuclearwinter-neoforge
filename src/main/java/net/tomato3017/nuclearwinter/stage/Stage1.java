package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

public class Stage1 extends StageBase {
    public Stage1() {
        super(StageType.STAGE_1.getIndex(), Config.STAGE1_DURATION.get(), Config.STAGE1_SKY_EMISSION.get());
    }
}
