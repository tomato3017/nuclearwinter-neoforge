package net.tomato3017.nuclearwinter.stage.events;

public interface StageEventFactory {
    boolean canStageActivate(long currentTick, long lastTimeActivated);

    boolean tryActivate();

    StageEvent create();
}
