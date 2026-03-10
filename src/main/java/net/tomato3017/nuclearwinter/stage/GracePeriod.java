package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

public class GracePeriod extends StageBase {
    public GracePeriod() {
        super(StageType.GRACE_PERIOD.getIndex(), Config.GRACE_DURATION.get(), 0.0);
    }
}
