package net.tomato3017.nuclearwinter.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.tomato3017.nuclearwinter.radiation.BlockResolver;
import org.slf4j.Logger;

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
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern STAGE_PATTERN = Pattern.compile("^stage([1-4])$");

    public DegradationRuleLoader() {
        super(GSON, "degradation_rules");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        TreeMap<Integer, DegradationStageData> stageDataMap = new TreeMap<>();
        Map<Integer, ResourceLocation> stageOwners = new HashMap<>();
        boolean conflict = false;

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            String path = entry.getKey().getPath();
            Matcher pathMatcher = STAGE_PATTERN.matcher(path);
            if (!pathMatcher.matches()) {
                LOGGER.warn("Ignoring degradation rules file with unexpected name: {}", entry.getKey());
                continue;
            }

            int stageNum = Integer.parseInt(pathMatcher.group(1));

            ResourceLocation previousOwner = stageOwners.get(stageNum);
            if (previousOwner != null) {
                LOGGER.error("Conflicting degradation rules for stage {}: both {} and {} define it. " +
                        "Aborting reload; keeping previously loaded rules.", stageNum, previousOwner, entry.getKey());
                conflict = true;
                continue;
            }
            stageOwners.put(stageNum, entry.getKey());

            DataResult<DegradationStageData> result = DegradationStageData.CODEC.parse(
                    com.mojang.serialization.JsonOps.INSTANCE, entry.getValue());
            if (result.error().isPresent()) {
                LOGGER.error("Failed to parse degradation rules for {}: {}",
                        entry.getKey(), result.error().get().message());
                continue;
            }

            result.result().ifPresent(data -> stageDataMap.put(stageNum, data));
        }

        if (conflict) {
            return;
        }

        // Resolve inheritance in stage order so each stage can inherit from the previous effective list
        TreeMap<Integer, List<BlockResolver.DegradationRule>> effectiveRulesMap = new TreeMap<>();

        for (Map.Entry<Integer, DegradationStageData> entry : stageDataMap.entrySet()) {
            int stageNum = entry.getKey();
            DegradationStageData data = entry.getValue();
            String sourceName = "degradation_rules/stage" + stageNum + ".json";

            List<BlockResolver.DegradationRule> parsedRules = parseAndValidateRules(data.rules(), sourceName);
            List<BlockResolver.DegradationRule> previousEffective = effectiveRulesMap.getOrDefault(stageNum - 1, List.of());

            List<BlockResolver.DegradationRule> effective = BlockResolver.buildEffectiveRules(
                    parsedRules, previousEffective, data.inherit());
            effectiveRulesMap.put(stageNum, effective);
        }

        List<BlockResolver.DegradationRule> stage1Rules = effectiveRulesMap.getOrDefault(1, List.of());
        List<BlockResolver.DegradationRule> stage2Rules = effectiveRulesMap.getOrDefault(2, List.of());
        List<BlockResolver.DegradationRule> stage3Rules = effectiveRulesMap.getOrDefault(3, List.of());
        List<BlockResolver.DegradationRule> stage4Rules = effectiveRulesMap.getOrDefault(4, List.of());

        BlockResolver.setDegradationRules(stage1Rules, stage2Rules, stage3Rules, stage4Rules);

        LOGGER.info("Loaded degradation rules: {} stage1, {} stage2, {} stage3, {} stage4",
                stage1Rules.size(), stage2Rules.size(), stage3Rules.size(), stage4Rules.size());
    }

    private List<BlockResolver.DegradationRule> parseAndValidateRules(List<DegradationRuleEntry> entries, String sourceName) {
        List<BlockResolver.DegradationRule> rules = new ArrayList<>();

        for (DegradationRuleEntry entry : entries) {
            BlockResolver.Matcher matcher = BlockResolver.parseMatcher(entry.match(), sourceName);
            if (matcher == null) continue;

            Optional<Block> replacement;
            if (entry.replacement().isEmpty()) {
                if (!entry.passthrough()) {
                    LOGGER.warn("Skipping rule in {}: no replacement specified and passthrough is false (match={}). " +
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
                LOGGER.warn("Skipping rule in {}: probability {} is not between 0.0 and 1.0 (match={})",
                        sourceName, probability, entry.match());
                continue;
            }

            rules.add(new BlockResolver.DegradationRule(matcher, replacement, entry.passthrough(), probability));
        }

        return rules;
    }
}
