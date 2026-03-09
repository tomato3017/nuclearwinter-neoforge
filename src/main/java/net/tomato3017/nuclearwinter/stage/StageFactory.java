package net.tomato3017.nuclearwinter.stage;

public class StageFactory {
    public static final int MAX_STAGE_INDEX = 5;

    public static StageBase create(int stageIndex) {
        return switch (stageIndex) {
            case 0 -> new Stage0();
            case 1 -> new GracePeriod();
            case 2 -> new Stage1();
            case 3 -> new Stage2();
            case 4 -> new Stage3();
            case 5 -> new Stage4();
            default -> throw new IllegalArgumentException("Unknown stage index: " + stageIndex);
        };
    }

    public static String getStageName(int stageIndex) {
        return switch (stageIndex) {
            case 0 -> "Inactive";
            case 1 -> "Grace Period";
            case 2 -> "Stage 1";
            case 3 -> "Stage 2";
            case 4 -> "Stage 3";
            case 5 -> "Stage 4";
            default -> "Unknown";
        };
    }
}
