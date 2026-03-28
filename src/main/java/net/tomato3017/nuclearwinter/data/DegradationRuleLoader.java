package net.tomato3017.nuclearwinter.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.radiation.BlockResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads degradation rules from {@code data/<namespace>/degradation_rules/stage[1-4].json} files.
 * Parses each stage file via {@link DegradationStageData} Codecs, validates block ids using
 * {@link BlockResolver}, resolves inheritance across stages, and pushes the final rule lists
 * to {@link BlockResolver#setDegradationRules}.
 */
public class DegradationRuleLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Pattern STAGE_PATTERN = Pattern.compile("^stage([1-4])$");

    public DegradationRuleLoader() {
        super(GSON, "degradation_rules");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        TreeMap<Integer, DegradationStageData> stageDataMap = loadStageData(object);
        if (stageDataMap == null) {
            return;
        }

        TreeMap<Integer, List<BlockResolver.DegradationRule>> effectiveRulesMap = resolveEffectiveRules(stageDataMap);
        applyResolvedRules(effectiveRulesMap);
    }

    private TreeMap<Integer, DegradationStageData> loadStageData(Map<ResourceLocation, JsonElement> object) {
        TreeMap<Integer, DegradationStageData> stageDataMap = new TreeMap<>();
        Map<Integer, ResourceLocation> stageOwners = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            Integer stageNum = getStageNumber(entry.getKey());
            if (stageNum == null) {
                continue;
            }

            if (!claimStageOwner(stageOwners, stageNum, entry.getKey())) {
                return null;
            }

            DegradationStageData stageData = parseStageData(entry.getKey(), entry.getValue());
            if (stageData != null) {
                stageDataMap.put(stageNum, stageData);
            }
        }

        return stageDataMap;
    }

    private TreeMap<Integer, List<BlockResolver.DegradationRule>> resolveEffectiveRules(
            TreeMap<Integer, DegradationStageData> stageDataMap) {
        TreeMap<Integer, List<BlockResolver.DegradationRule>> effectiveRulesMap = new TreeMap<>();

        for (Map.Entry<Integer, DegradationStageData> entry : stageDataMap.entrySet()) {
            int stageNum = entry.getKey();
            DegradationStageData stageData = entry.getValue();
            List<BlockResolver.DegradationRule> parsedRules =
                    parseAndValidateRules(stageData.rules(), getStageSourceName(stageNum));
            List<BlockResolver.DegradationRule> previousEffective = effectiveRulesMap.getOrDefault(stageNum - 1, List.of());
            List<BlockResolver.DegradationRule> effectiveRules = BlockResolver.buildEffectiveRules(
                    parsedRules, previousEffective, stageData.inherit());

            effectiveRulesMap.put(stageNum, effectiveRules);
        }

        return effectiveRulesMap;
    }

    private void applyResolvedRules(TreeMap<Integer, List<BlockResolver.DegradationRule>> effectiveRulesMap) {
        List<BlockResolver.DegradationRule> stage1Rules = getStageRules(effectiveRulesMap, 1);
        List<BlockResolver.DegradationRule> stage2Rules = getStageRules(effectiveRulesMap, 2);
        List<BlockResolver.DegradationRule> stage3Rules = getStageRules(effectiveRulesMap, 3);
        List<BlockResolver.DegradationRule> stage4Rules = getStageRules(effectiveRulesMap, 4);

        dumpEffectiveRules(effectiveRulesMap);
        BlockResolver.setDegradationRules(stage1Rules, stage2Rules, stage3Rules, stage4Rules);

        NuclearWinter.LOGGER.info("Loaded degradation rules: {} stage1, {} stage2, {} stage3, {} stage4",
                stage1Rules.size(), stage2Rules.size(), stage3Rules.size(), stage4Rules.size());
    }

    private void dumpEffectiveRules(TreeMap<Integer, List<BlockResolver.DegradationRule>> effectiveRulesMap) {
        if (!NuclearWinter.LOGGER.isDebugEnabled()) {
            return;
        }

        for (Map.Entry<Integer, List<BlockResolver.DegradationRule>> entry : effectiveRulesMap.entrySet()) {
            int stage = entry.getKey();
            List<BlockResolver.DegradationRule> rules = entry.getValue();
            NuclearWinter.LOGGER.debug("Stage {} effective degradation rules ({}):", stage, rules.size());
            for (BlockResolver.DegradationRule rule : rules) {
                NuclearWinter.LOGGER.debug("  {} -> {} (passthrough={}, p={})",
                        formatMatcher(rule.matcher()),
                        formatReplacement(rule.replacement()),
                        rule.passthrough(),
                        rule.probability());
            }
        }
    }

    private List<BlockResolver.DegradationRule> parseAndValidateRules(List<DegradationRuleEntry> entries, String sourceName) {
        List<BlockResolver.DegradationRule> rules = new ArrayList<>();

        for (DegradationRuleEntry entry : entries) {
            BlockResolver.Matcher matcher = BlockResolver.parseMatcher(entry.match(), sourceName);
            if (matcher == null) continue;

            Optional<Block> replacement;
            if (entry.replacement().isEmpty()) {
                if (!entry.passthrough()) {
                    NuclearWinter.LOGGER.warn("Skipping rule in {}: no replacement specified and passthrough is false (match={}). " +
                            "This rule would have no effect; add passthrough: true to skip the block silently.",
                            sourceName, entry.match());
                    continue;
                }
                replacement = Optional.empty();
            } else {
                Block block = BlockResolver.parseBlock(entry.replacement(), sourceName, true);
                if (block == null) continue;
                replacement = Optional.of(block);
            }

            double probability = entry.probability();
            if (probability < 0.0 || probability > 1.0) {
                NuclearWinter.LOGGER.warn("Skipping rule in {}: probability {} is not between 0.0 and 1.0 (match={})",
                        sourceName, probability, entry.match());
                continue;
            }

            rules.add(new BlockResolver.DegradationRule(matcher, replacement, entry.passthrough(), probability));
        }

        return rules;
    }

    private Integer getStageNumber(ResourceLocation resourceLocation) {
        Matcher pathMatcher = STAGE_PATTERN.matcher(resourceLocation.getPath());
        if (!pathMatcher.matches()) {
            NuclearWinter.LOGGER.warn("Ignoring degradation rules file with unexpected name: {}", resourceLocation);
            return null;
        }

        return Integer.parseInt(pathMatcher.group(1));
    }

    private boolean claimStageOwner(Map<Integer, ResourceLocation> stageOwners, int stageNum, ResourceLocation resourceLocation) {
        ResourceLocation previousOwner = stageOwners.putIfAbsent(stageNum, resourceLocation);
        if (previousOwner == null) {
            return true;
        }

        NuclearWinter.LOGGER.error(
                "Conflicting degradation rules for stage {}: both {} and {} define it. Aborting reload; keeping previously loaded rules.",
                stageNum,
                previousOwner,
                resourceLocation);
        return false;
    }

    private DegradationStageData parseStageData(ResourceLocation resourceLocation, JsonElement jsonElement) {
        DataResult<DegradationStageData> result = DegradationStageData.CODEC.parse(JsonOps.INSTANCE, jsonElement);
        if (result.error().isPresent()) {
            NuclearWinter.LOGGER.error("Failed to parse degradation rules for {}: {}",
                    resourceLocation, result.error().get().message());
            return null;
        }

        return result.result().orElse(null);
    }

    private String getStageSourceName(int stageNum) {
        return "degradation_rules/stage" + stageNum + ".json";
    }

    private List<BlockResolver.DegradationRule> getStageRules(
            TreeMap<Integer, List<BlockResolver.DegradationRule>> effectiveRulesMap,
            int stageNum) {
        return effectiveRulesMap.getOrDefault(stageNum, List.of());
    }

    private String formatMatcher(BlockResolver.Matcher matcher) {
        if (matcher instanceof BlockResolver.TagMatcher tagMatcher) {
            return "#" + tagMatcher.tag().location();
        }
        if (matcher instanceof BlockResolver.BlockMatcher blockMatcher) {
            return BuiltInRegistries.BLOCK.getKey(blockMatcher.block()).toString();
        }

        return matcher.toString();
    }

    private String formatReplacement(Optional<Block> replacement) {
        return replacement
                .map(block -> BuiltInRegistries.BLOCK.getKey(block).toString())
                .orElse("(none)");
    }
}
