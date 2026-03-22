# NuclearWinter Resource Pack Template

This folder is a ready-to-use resource pack for swapping NuclearWinter block textures without rebuilding the mod.

## How to use

1. Edit the PNG files in `assets/nuclearwinter/textures/block/`.
2. Keep the same file names.
3. Keep each image at `16x16` unless you intentionally want higher resolution art.
4. Copy the whole `resourcepack-template` folder into your Minecraft `resourcepacks` folder, or zip its contents and use the zip as a pack.
5. Enable the pack above the mod's default resources in Minecraft.

## Quick export

Run `./gradlew exportResourcePackTemplate` from the repo root to create `build/resourcepacks/nuclearwinter-resourcepack-template.zip`.

You can also run `./gradlew build` and it will be produced alongside the normal build outputs.

Run `./export-resourcepack-template.sh` from the repo root to create `build/resourcepacks/nuclearwinter-resourcepack-template.zip`.

## Included texture slots

- `cracked_stone.png`
- `dead_grass_top.png`
- `dead_grass_side.png`
- `dead_grass_bottom.png`
- `dead_leaves.png`
- `parched_dirt.png`
- `wasteland_dust.png`
- `wasteland_rubble.png`
- `deadwood_side.png`
- `deadwood_top.png`
- `ruined_planks.png`
- `lead_block.png`
- `reinforced_concrete.png`

## Notes

- `dead_grass` now uses separate top, side, and bottom textures.
- `deadwood` now uses log-style axis rotation, so `deadwood_side.png` is the bark and `deadwood_top.png` is the end grain.
- The included PNGs are starter placeholders copied from the mod's current textures.
