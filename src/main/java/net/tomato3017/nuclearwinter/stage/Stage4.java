package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

public class Stage4 extends StageBase {
    public static final int INDEX = 5;

    public Stage4() {
        super(INDEX, Config.STAGE4_DURATION.get(), Config.STAGE4_SKY_EMISSION.get());
    }
}
