package net.tomato3017.nuclearwinter.stage.events;

import java.util.concurrent.ThreadLocalRandom;

public class RadStormEventFactory implements StageEventFactory {

    @Override
    public boolean canStageActivate(long currentTick, long lastTimeActivated) {
        return true;
    }

    @Override
    public boolean tryActivate() {
        return ThreadLocalRandom.current().nextInt(100) < 90;
    }

    @Override
    public StageEvent create() {
        return new RadStormEvent();
    }
}
