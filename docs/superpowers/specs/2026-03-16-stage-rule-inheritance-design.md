# Stage Rule Inheritance Design

**Goal:** Let stage 3 and stage 4 chunk degradation mappings inherit the previous stage's effective mappings by default, while allowing each stage to opt out via separate stage option lists.

## Scope

- Keep `stage2Rules`, `stage3Rules`, and `stage4Rules` as ordered multiline string rule blocks.
- Add `stage2Options`, `stage3Options`, and `stage4Options` as list-valued config entries.
- Support `inherit=true` and `inherit=false` in option lists.
- Keep runtime lookup in `BlockResolver.getDegradationResult(...)` as first-match-wins against one effective rule list per stage.

## Chosen Behavior

- Stage 2 never inherits because there is no prior degradation stage.
- Stage 3 and stage 4 inherit the previous stage's effective mappings by default.
- If `inherit=false` is present in a stage option list, that stage ignores inherited mappings and uses only its own rules.
- Later-stage rules always win over inherited rules because effective lists are built with the current stage's rules first and inherited rules appended afterward.
- Exact duplicate inherited matchers are removed while merging so the effective list stays smaller and clearer.

## Config Shape

```toml
[chunkProcessing]
	stage2Options = []
	stage2Rules = """
	minecraft:grass_block -> nuclearwinter:dead_grass
	#minecraft:dirt -> nuclearwinter:parched_dirt
	"""

	stage3Options = ["inherit=true"]
	stage3Rules = """
	minecraft:grass_block -> nuclearwinter:wasteland_dust
	"""

	stage4Options = ["inherit=false"]
	stage4Rules = """
	#minecraft:leaves -> minecraft:air | passthrough=true
	"""
```

## Implementation Notes

- `Config.java` gains three list config values plus comments describing supported option strings.
- `BlockResolver.java` gains option parsing and a small stage config model for `inheritPrevious` plus stage-owned rules.
- Effective rule lists are rebuilt during `BlockResolver.init()` so config reloads pick up both rules and options.
- `ChunkProcessor.java` stays unchanged because it already consumes `BlockResolver.getDegradationResult(...)`.

## Testing

- Add GameTests that temporarily override stage option/rule config values, rebuild `BlockResolver`, and assert:
  - stage 3 inherits stage 2 by default
  - stage 4 can disable inheritance with `inherit=false`
  - later-stage rules run before inherited broader rules
