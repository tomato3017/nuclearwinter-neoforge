package net.tomato3017.nuclearwinter.stage;

public class StageFactory {
    public static final int MAX_STAGE_INDEX = StageType.MAX_INDEX;

    public static StageBase create(int stageIndex) {
        return StageType.fromIndex(stageIndex).create();
    }

    public static String getStageName(int stageIndex) {
        return StageType.fromIndex(stageIndex).getDisplayName();
    }
}
