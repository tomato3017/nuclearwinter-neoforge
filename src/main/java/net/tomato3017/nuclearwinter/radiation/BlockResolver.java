package net.tomato3017.nuclearwinter.radiation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Maps blocks and block tags to radiation resistance values loaded from {@link Config}.
 * A higher resistance value means the block attenuates more radiation per layer in the raycast.
 * Call {@link #init()} on server start; use {@link #registerBlockOverride(Block, double)} to add mod-specific entries afterward.
 */
public class BlockResolver {
    private static volatile Map<TagKey<Block>, Double> tagResistanceMap = Map.of();
    private static volatile Map<Block, Double> blockOverrides = Map.of();
    private static volatile double defaultResistance = 1.0;

    private static volatile List<DegradationRule> stage2DegradationRules = List.of();
    private static volatile List<DegradationRule> stage3DegradationRules = List.of();
    private static volatile List<DegradationRule> stage4DegradationRules = List.of();

    public static synchronized void init() {
        LinkedHashMap<TagKey<Block>, Double> newTagResistanceMap = new LinkedHashMap<>();
        LinkedHashMap<Block, Double> newBlockOverrides = new LinkedHashMap<>();

        double newDefaultResistance = Config.RESISTANCE_STONE.get();

        newTagResistanceMap.put(BlockTags.DIRT, Config.RESISTANCE_DIRT.get());
        newTagResistanceMap.put(BlockTags.LOGS, Config.RESISTANCE_WOOD.get());
        newTagResistanceMap.put(BlockTags.PLANKS, Config.RESISTANCE_WOOD.get());
        newTagResistanceMap.put(BlockTags.WOODEN_FENCES, Config.RESISTANCE_WOOD.get());
        newTagResistanceMap.put(BlockTags.WOODEN_DOORS, Config.RESISTANCE_WOOD.get());
        newTagResistanceMap.put(BlockTags.WOODEN_SLABS, Config.RESISTANCE_WOOD.get());
        newTagResistanceMap.put(BlockTags.WOODEN_STAIRS, Config.RESISTANCE_WOOD.get());
        newTagResistanceMap.put(BlockTags.STONE_ORE_REPLACEABLES, Config.RESISTANCE_STONE.get());
        newTagResistanceMap.put(BlockTags.DEEPSLATE_ORE_REPLACEABLES, Config.RESISTANCE_DEEPSLATE.get());

        newBlockOverrides.put(Blocks.OBSIDIAN, Config.RESISTANCE_DEEPSLATE.get());
        newBlockOverrides.put(Blocks.CRYING_OBSIDIAN, Config.RESISTANCE_DEEPSLATE.get());
        newBlockOverrides.put(Blocks.IRON_BLOCK, Config.RESISTANCE_IRON.get());
        newBlockOverrides.put(Blocks.WATER, Config.RESISTANCE_WATER.get());
        newBlockOverrides.put(Blocks.GRAVEL, Config.RESISTANCE_DIRT.get());

        StageOptions stage2Options = parseStageOptions(Config.CHUNK_PROCESSING_STAGE2_OPTIONS, "chunkProcessing.stage2Options", false);
        StageOptions stage3Options = parseStageOptions(Config.CHUNK_PROCESSING_STAGE3_OPTIONS, "chunkProcessing.stage3Options", true);
        StageOptions stage4Options = parseStageOptions(Config.CHUNK_PROCESSING_STAGE4_OPTIONS, "chunkProcessing.stage4Options", true);

        if (stage2Options.inheritPrevious()) {
            warnInvalidStageOption("chunkProcessing.stage2Options", "inherit=true", "Stage 2 cannot inherit from a previous stage");
            stage2Options = new StageOptions(false);
        }

        List<DegradationRule> newStage2Rules = buildEffectiveRules(
                parseDegradationRules(Config.CHUNK_PROCESSING_STAGE2_RULES, "chunkProcessing.stage2Rules"),
                List.of(),
                stage2Options.inheritPrevious());
        List<DegradationRule> newStage3Rules = buildEffectiveRules(
                parseDegradationRules(Config.CHUNK_PROCESSING_STAGE3_RULES, "chunkProcessing.stage3Rules"),
                newStage2Rules,
                stage3Options.inheritPrevious());
        List<DegradationRule> newStage4Rules = buildEffectiveRules(
                parseDegradationRules(Config.CHUNK_PROCESSING_STAGE4_RULES, "chunkProcessing.stage4Rules"),
                newStage3Rules,
                stage4Options.inheritPrevious());

        defaultResistance = newDefaultResistance;
        tagResistanceMap = Collections.unmodifiableMap(newTagResistanceMap);
        blockOverrides = Collections.unmodifiableMap(newBlockOverrides);
        stage2DegradationRules = List.copyOf(newStage2Rules);
        stage3DegradationRules = List.copyOf(newStage3Rules);
        stage4DegradationRules = List.copyOf(newStage4Rules);

        NuclearWinter.LOGGER.info(
                "BlockResolver initialized with {} tag resistances, {} block overrides, {} stage2 rules, {} stage3 rules, {} stage4 rules",
                tagResistanceMap.size(), blockOverrides.size(), stage2DegradationRules.size(), stage3DegradationRules.size(), stage4DegradationRules.size());
    }

    private static List<DegradationRule> parseDegradationRules(ModConfigSpec.ConfigValue<String> configValue, String configPath) {
        List<DegradationRule> rules = new ArrayList<>();
        for (String rawRule : configValue.get().lines().toList()) {
            if (rawRule.isBlank()) continue;
            Optional<DegradationRule> parsedRule = parseDegradationRule(rawRule, configPath);
            parsedRule.ifPresent(rules::add);
        }
        return rules;
    }

    private static StageOptions parseStageOptions(ModConfigSpec.ConfigValue<List<? extends String>> configValue, String configPath, boolean defaultInheritPrevious) {
        boolean inheritPrevious = defaultInheritPrevious;
        for (String rawOption : configValue.get()) {
            String option = rawOption == null ? "" : rawOption.trim();
            if (option.isEmpty()) {
                warnInvalidStageOption(configPath, String.valueOf(rawOption), "option is empty");
                continue;
            }

            int equalsIndex = option.indexOf('=');
            if (equalsIndex < 0) {
                warnInvalidStageOption(configPath, rawOption, "missing '='");
                continue;
            }

            String key = option.substring(0, equalsIndex).trim();
            String value = option.substring(equalsIndex + 1).trim();
            if (key.equals("inherit")) {
                if (!value.equals("true") && !value.equals("false")) {
                    warnInvalidStageOption(configPath, rawOption, "inherit must be true or false");
                    continue;
                }

                inheritPrevious = Boolean.parseBoolean(value);
            } else {
                warnInvalidStageOption(configPath, rawOption, "unknown option '" + key + "'");
            }
        }

        return new StageOptions(inheritPrevious);
    }

    private static List<DegradationRule> buildEffectiveRules(List<DegradationRule> currentRules, List<DegradationRule> inheritedRules, boolean inheritPrevious) {
        if (!inheritPrevious || inheritedRules.isEmpty()) {
            return currentRules;
        }

        List<DegradationRule> effectiveRules = new ArrayList<>(currentRules);
        Set<Matcher> seenMatchers = new LinkedHashSet<>();
        for (DegradationRule rule : currentRules) {
            seenMatchers.add(rule.matcher());
        }

        // First-match-wins means a current-stage matcher completely overrides any inherited matcher-equivalent rule.
        for (DegradationRule inheritedRule : inheritedRules) {
            if (seenMatchers.add(inheritedRule.matcher())) {
                effectiveRules.add(inheritedRule);
            }
        }

        return effectiveRules;
    }

    private static Optional<DegradationRule> parseDegradationRule(String rawRule, String configPath) {
        String trimmedRule = rawRule.trim();
        if (trimmedRule.isEmpty()) {
            warnInvalidRule(configPath, rawRule, "rule is empty");
            return Optional.empty();
        }

        int separatorIndex = trimmedRule.indexOf("->");
        if (separatorIndex < 0) {
            warnInvalidRule(configPath, rawRule, "missing '->' separator");
            return Optional.empty();
        }

        String matcherToken = trimmedRule.substring(0, separatorIndex).trim();
        String rightSide = trimmedRule.substring(separatorIndex + 2).trim();
        if (matcherToken.isEmpty()) {
            warnInvalidRule(configPath, rawRule, "matcher is empty");
            return Optional.empty();
        }
        if (rightSide.isEmpty()) {
            warnInvalidRule(configPath, rawRule, "replacement is empty");
            return Optional.empty();
        }

        Matcher matcher = parseMatcher(matcherToken, configPath, rawRule);
        if (matcher == null) {
            return Optional.empty();
        }

        String[] rightSideParts = rightSide.split("\\|");
        String replacementToken = rightSideParts[0].trim();
        if (replacementToken.isEmpty()) {
            warnInvalidRule(configPath, rawRule, "replacement is empty");
            return Optional.empty();
        }

        Block replacement = parseBlock(replacementToken, configPath, rawRule, true);
        if (replacement == null) {
            return Optional.empty();
        }

        boolean passthrough = false;
        boolean sawPassthrough = false;
        for (int i = 1; i < rightSideParts.length; i++) {
            String option = rightSideParts[i].trim();
            if (option.isEmpty()) {
                warnInvalidRule(configPath, rawRule, "contains an empty option");
                return Optional.empty();
            }

            int equalsIndex = option.indexOf('=');
            if (equalsIndex < 0) {
                warnInvalidRule(configPath, rawRule, "option '" + option + "' is missing '='");
                return Optional.empty();
            }

            String key = option.substring(0, equalsIndex).trim();
            String value = option.substring(equalsIndex + 1).trim();
            if (key.equals("passthrough")) {
                if (sawPassthrough) {
                    warnInvalidRule(configPath, rawRule, "duplicate passthrough option");
                    return Optional.empty();
                }
                if (!value.equals("true") && !value.equals("false")) {
                    warnInvalidRule(configPath, rawRule, "passthrough must be true or false");
                    return Optional.empty();
                }
                passthrough = Boolean.parseBoolean(value);
                sawPassthrough = true;
            } else {
                warnInvalidRule(configPath, rawRule, "unknown option '" + key + "'");
                return Optional.empty();
            }
        }

        return Optional.of(new DegradationRule(matcher, replacement, passthrough));
    }

    private static Matcher parseMatcher(String matcherToken, String configPath, String rawRule) {
        if (matcherToken.startsWith("#")) {
            if (matcherToken.length() == 1) {
                warnInvalidRule(configPath, rawRule, "tag matcher is missing an id");
                return null;
            }

            ResourceLocation tagId = ResourceLocation.tryParse(matcherToken.substring(1));
            if (tagId == null) {
                warnInvalidRule(configPath, rawRule, "tag matcher '" + matcherToken + "' is not a valid resource location");
                return null;
            }

            return new TagMatcher(TagKey.create(Registries.BLOCK, tagId));
        }

        Block block = parseBlock(matcherToken, configPath, rawRule, false);
        if (block == null) {
            return null;
        }

        return new BlockMatcher(block);
    }

    private static Block parseBlock(String token, String configPath, String rawRule, boolean replacement) {
        if (replacement && token.startsWith("#")) {
            warnInvalidRule(configPath, rawRule, "replacement must be a block id, not a tag");
            return null;
        }

        ResourceLocation blockId = ResourceLocation.tryParse(token);
        if (blockId == null) {
            warnInvalidRule(configPath, rawRule, "block id '" + token + "' is not a valid resource location");
            return null;
        }

        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(blockId);
        if (block.isEmpty()) {
            warnInvalidRule(configPath, rawRule, "block id '" + token + "' was not found");
            return null;
        }

        return block.get();
    }

    private static void warnInvalidRule(String configPath, String rawRule, String reason) {
        NuclearWinter.LOGGER.warn("Skipping invalid chunk degradation rule in {}: '{}' ({})", configPath, rawRule, reason);
    }

    private static void warnInvalidStageOption(String configPath, String rawOption, String reason) {
        NuclearWinter.LOGGER.warn("Skipping invalid chunk degradation option in {}: '{}' ({})", configPath, rawOption, reason);
    }

    public static DegradationResult getDegradationResult(BlockState state, int stageIndex) {
        List<DegradationRule> rules;
        if (stageIndex >= 5) {
            rules = stage4DegradationRules;
        } else if (stageIndex == 4) {
            rules = stage3DegradationRules;
        } else if (stageIndex == 3) {
            rules = stage2DegradationRules;
        } else {
            return null;
        }

        for (DegradationRule rule : rules) {
            if (rule.matches(state)) {
                return new DegradationResult(rule.replacement(), rule.passthrough());
            }
        }

        return null;
    }

    public static Block getDegradedBlock(BlockState state, int stageIndex) {
        DegradationResult result = getDegradationResult(state, stageIndex);
        return result == null ? null : result.replacement();
    }

    public static synchronized void registerBlockOverride(Block block, double resistance) {
        LinkedHashMap<Block, Double> overrides = new LinkedHashMap<>(blockOverrides);
        overrides.put(block, resistance);
        blockOverrides = Collections.unmodifiableMap(overrides);
    }

    public static boolean canRadiationPassThrough(BlockState state) {
        return state.isAir() ||
                state.is(BlockTags.LEAVES) ||
                !state.canOcclude();
    }

    public static double getResistance(BlockState state) {
        Map<Block, Double> overrides = blockOverrides;
        Map<TagKey<Block>, Double> tagResistances = tagResistanceMap;
        Block block = state.getBlock();

        if (state.isAir()) return 0.0;

        Double override = overrides.get(block);
        if (override != null) return override;

        for (var entry : tagResistances.entrySet()) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        if (canRadiationPassThrough(state)) {
            return 0.0;
        }

        return defaultResistance;
    }

    public record DegradationResult(Block replacement, boolean passthrough) {
    }

    private record StageOptions(boolean inheritPrevious) {
    }

    private interface Matcher {
        boolean matches(BlockState state);
    }

    private record DegradationRule(Matcher matcher, Block replacement, boolean passthrough) {
        private boolean matches(BlockState state) {
            return matcher.matches(state);
        }
    }

    private record BlockMatcher(Block block) implements Matcher {
        @Override
        public boolean matches(BlockState state) {
            return state.getBlock() == block;
        }
    }

    private record TagMatcher(TagKey<Block> tag) implements Matcher {
        @Override
        public boolean matches(BlockState state) {
            return state.is(tag);
        }
    }
}
