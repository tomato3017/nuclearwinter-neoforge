package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

public class Stage4 extends StageBase {
    public Stage4() {
        super(StageType.STAGE_4.getIndex(), Config.STAGE4_DURATION.get(), Config.STAGE4_SKY_EMISSION.get());
    }
}
