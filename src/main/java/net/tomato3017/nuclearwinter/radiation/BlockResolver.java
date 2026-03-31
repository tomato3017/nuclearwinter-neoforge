package net.tomato3017.nuclearwinter.radiation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.debug.BlockCaptureManager;
import net.tomato3017.nuclearwinter.stage.StageType;

import java.util.*;

/**
 * Maps blocks and block tags to radiation resistance values and chunk degradation rules.
 * Resistance values are supplied by {@link net.tomato3017.nuclearwinter.data.RadiationResistanceLoader} via
 * {@link #setRadiationResistance} on datapack reload. Degradation rules are supplied by
 * {@link net.tomato3017.nuclearwinter.data.DegradationRuleLoader} via {@link #setDegradationRules}.
 */
public class BlockResolver {
    private static volatile Map<TagKey<Block>, Double> tagResistanceMap = Map.of();
    private static volatile Map<Block, Double> blockOverrides = Map.of();
    private static volatile double defaultResistance = 1.0;

    private static volatile List<DegradationRule> stage1DegradationRules = List.of();
    private static volatile List<DegradationRule> stage2DegradationRules = List.of();
    private static volatile List<DegradationRule> stage3DegradationRules = List.of();
    private static volatile List<DegradationRule> stage4DegradationRules = List.of();

    /**
     * Replaces the radiation resistance maps with values parsed from datapack JSON.
     * Called by {@link net.tomato3017.nuclearwinter.data.RadiationResistanceLoader} on each resource reload.
     *
     * @param blockResistances  block-specific resistance overrides
     * @param tagResistances    tag-based resistance values (first matching tag wins in {@link #getResistance})
     * @param newDefaultResistance fallback resistance for blocks not matched by any entry
     */
    public static synchronized void setRadiationResistance(
            Map<Block, Double> blockResistances,
            Map<TagKey<Block>, Double> tagResistances,
            double newDefaultResistance) {
        blockOverrides = Collections.unmodifiableMap(new LinkedHashMap<>(blockResistances));
        tagResistanceMap = Collections.unmodifiableMap(new LinkedHashMap<>(tagResistances));
        defaultResistance = newDefaultResistance;

        NuclearWinter.LOGGER.info(
                "BlockResolver updated with {} tag resistances, {} block overrides, default={}",
                tagResistanceMap.size(), blockOverrides.size(), defaultResistance);
    }

    /**
     * Sets degradation rules for all four gameplay stages. Called by {@code DegradationRuleLoader}
     * after parsing and resolving datapack JSON files.
     *
     * @param stage1 effective rules for Stage 1 (internal index 2)
     * @param stage2 effective rules for Stage 2 (internal index 3)
     * @param stage3 effective rules for Stage 3 (internal index 4)
     * @param stage4 effective rules for Stage 4 (internal index 5)
     */
    public static synchronized void setDegradationRules(
            List<DegradationRule> stage1,
            List<DegradationRule> stage2,
            List<DegradationRule> stage3,
            List<DegradationRule> stage4) {
        stage1DegradationRules = List.copyOf(stage1);
        stage2DegradationRules = List.copyOf(stage2);
        stage3DegradationRules = List.copyOf(stage3);
        stage4DegradationRules = List.copyOf(stage4);
    }

    /**
     * Builds the effective rule list for a stage by optionally prepending its own rules
     * in front of inherited rules. First-match wins: if a current-stage rule has the same
     * matcher as an inherited rule, the inherited rule is dropped.
     */
    public static List<DegradationRule> buildEffectiveRules(
            List<DegradationRule> currentRules,
            List<DegradationRule> inheritedRules,
            boolean inheritPrevious) {
        if (!inheritPrevious || inheritedRules.isEmpty()) {
            return currentRules;
        }

        List<DegradationRule> effectiveRules = new ArrayList<>(currentRules);
        Set<Matcher> seenMatchers = new LinkedHashSet<>();
        for (DegradationRule rule : currentRules) {
            seenMatchers.add(rule.matcher());
        }

        for (DegradationRule inheritedRule : inheritedRules) {
            if (seenMatchers.add(inheritedRule.matcher())) {
                effectiveRules.add(inheritedRule);
            }
        }

        return effectiveRules;
    }

    /**
     * Parses a matcher token (a block id or {@code #tag} reference) and logs a warning on failure.
     *
     * @param matcherToken the raw match string from JSON
     * @param sourceName   descriptive source name for log messages
     * @return the parsed {@link Matcher}, or {@code null} if invalid
     */
    public static Matcher parseMatcher(String matcherToken, String sourceName) {
        if (matcherToken.startsWith("#")) {
            if (matcherToken.length() == 1) {
                warnInvalidRule(sourceName, matcherToken, "tag matcher is missing an id");
                return null;
            }

            ResourceLocation tagId = ResourceLocation.tryParse(matcherToken.substring(1));
            if (tagId == null) {
                warnInvalidRule(sourceName, matcherToken, "tag matcher '" + matcherToken + "' is not a valid resource location");
                return null;
            }

            return new TagMatcher(TagKey.create(Registries.BLOCK, tagId));
        }

        Block block = parseBlock(matcherToken, sourceName, false);
        if (block == null) {
            return null;
        }

        return new BlockMatcher(block);
    }

    /**
     * Parses a block id string and logs a warning on failure.
     *
     * @param token       the raw block id string
     * @param sourceName  descriptive source name for log messages
     * @param replacement if {@code true}, rejects tag references (replacements must be concrete blocks)
     * @return the parsed {@link Block}, or {@code null} if invalid
     */
    public static Block parseBlock(String token, String sourceName, boolean replacement) {
        if (replacement && token.startsWith("#")) {
            warnInvalidRule(sourceName, token, "replacement must be a block id, not a tag");
            return null;
        }

        ResourceLocation blockId = ResourceLocation.tryParse(token);
        if (blockId == null) {
            warnInvalidRule(sourceName, token, "block id '" + token + "' is not a valid resource location");
            return null;
        }

        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(blockId);
        if (block.isEmpty()) {
            warnInvalidRule(sourceName, token, "block id '" + token + "' was not found");
            return null;
        }

        return block.get();
    }

    private static void warnInvalidRule(String sourceName, String rawRule, String reason) {
        NuclearWinter.LOGGER.warn("Skipping invalid entry in {}: '{}' ({})", sourceName, rawRule, reason);
    }

    public static DegradationResult getDegradationResult(BlockState state, int stageIndex) {
        int clampedIndex = Math.min(stageIndex, StageType.MAX_INDEX);
        StageType type = StageType.fromIndex(clampedIndex);
        List<DegradationRule> rules = switch (type) {
            case STAGE_4 -> stage4DegradationRules;
            case STAGE_3 -> stage3DegradationRules;
            case STAGE_2 -> stage2DegradationRules;
            case STAGE_1 -> stage1DegradationRules;
            default -> null;
        };
        if (rules == null) return null;

        for (DegradationRule rule : rules) {
            if (rule.matches(state)) {
                return new DegradationResult(rule.replacement(), rule.passthrough(), rule.probability());
            }
        }

        BlockCaptureManager.recordDegradationMiss(state.getBlock());
        return null;
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

        if (!state.canOcclude()) {
            return 0.0;
        }

        BlockCaptureManager.recordRaycastMiss(block);
        return defaultResistance;
    }

    public record DegradationResult(Optional<Block> replacement, boolean passthrough, double probability) {
    }

    public interface Matcher {
        boolean matches(BlockState state);
    }

    public record DegradationRule(Matcher matcher, Optional<Block> replacement, boolean passthrough,
                                  double probability) {
        public boolean matches(BlockState state) {
            return matcher.matches(state);
        }
    }

    public record BlockMatcher(Block block) implements Matcher {
        @Override
        public boolean matches(BlockState state) {
            return state.getBlock() == block;
        }
    }

    public record TagMatcher(TagKey<Block> tag) implements Matcher {
        @Override
        public boolean matches(BlockState state) {
            return state.is(tag);
        }
    }
}
