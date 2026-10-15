
# AetherEnchantments

A Fabric mod for Minecraft 26.3 that introduces enchantment-based mechanics designed to enhance vanilla tools and gameplay.

Unlike traditional utility mods, AetherEnchantments uses vanilla enchantment books to unlock additional tool abilities and progression-based features.

## Features

### Lumberjack

Unlocks advanced tree interaction mechanics through an enchantment.

- Automatically chops connected tree logs.
- Supports multiple Lumberjack levels with increasing tree size limits.
- Works with supported axes.
- Crouch + right-click to strip an entire tree without breaking it.
- Supports normal trees and additional wood types.

**Tree limits:**

| Enchantment Level | Maximum Logs |
|---|---:|
| Lumberjack I | 8 |
| Lumberjack II | 40 |
| Lumberjack III | 256 |

### Indestructible

Prevents enchanted tools and armor from breaking.

- Prevents durability from reaching the broken state.
- Keeps equipment usable at minimum durability.
- Allows equipment to continue being used without being destroyed.

### Telekinesis

Automatically collects dropped items directly into the player's inventory.

- Collects items produced by blocks broken with supported enchanted tools.
- Supports axes, pickaxes, shovels, hoes, shears and brushes.
- Attempts to insert collected items into the inventory.
- Leaves items in the world if the inventory cannot hold them completely.

### Harvester

Provides area-based crop harvesting, replanting, fertilization and planting.

Works with enchanted hoes and supports three levels.

**Area sizes:**

| Enchantment Level | Area | Radius |
|---|---:|---:|
| Harvester I | 1×1 | 0 |
| Harvester II | 5×5 | 2 |
| Harvester III | 15×15 | 7 |

**Supported crops:**
- Wheat
- Carrots
- Potatoes
- Beetroot
- Nether Wart

**Harvesting**
- Harvests mature crops within the enchantment area.
- Immature crops are left untouched.
- Produces normal crop drops.
- Supports integration with Telekinesis.

**Automatic replanting**
- Replants harvested crops when the required seeds or crop items are available in the player's inventory.
- Consumes one planting item per successfully replanted crop.
- Supports all five listed crops.

**Fertilization**
- Right-click with Bone Meal to fertilize supported crops within the enchantment area.
- Consumes Bone Meal from the player's inventory.
- Supports wheat, carrots, potatoes and beetroot.
- Nether Wart cannot be fertilized.

**Area planting**
- Hold planting items in the offhand and right-click farmland.
- Automatically plants the corresponding crop across the enchantment area.
- Consumes one item per successfully planted crop.
- Only plants on valid farmland with an empty space above it.
- Supports wheat seeds, carrots, potatoes and beetroot seeds.

### Prospector

Prospector allows pickaxes to mine connected ore veins automatically, reducing the need to break each ore block individually.

**Supported Tool:** Pickaxes

**Maximum Blocks:** 256 per activation (including the original block).

**Levels:**

- **Prospector I:** Mines connected ores belonging to the same ore family as the original block. For example, an iron ore vein can contain both regular and deepslate iron ore, but not coal or gold.
- **Prospector II:** Mines all connected registered ore types, regardless of their ore family.

**Features:**

- Detects ores connected through their six adjacent faces. Diagonal blocks are not considered connected.
- Only blocks registered in the Prospector ore tag can be mined.
- Respects vanilla tool mining requirements. A tool cannot mine ores that require a higher mining tier.
- Inaccessible ores are skipped without preventing other accessible ores from being mined.
- Supports vanilla ore variants, including deepslate ores, Nether gold ore, Nether quartz ore, and Ancient Debris.
- Preserves normal block-breaking behavior, including Fortune, Silk Touch, tool durability, and other compatible enchantments.
- Compatible with Telekinesis and Indestructible.
- Avoids loading additional chunks during vein detection.

**Important:** The 256-block limit includes the original ore block. Prospector can therefore break up to 255 additional blocks per activation.


## Enchantment Overview

| Enchantment | Tool | Maximum Level | Main Ability |
|---|---|---:|---|
| Lumberjack | Axes | III | Tree chopping and stripping |
| Indestructible | Tools and armor | I | Prevents equipment from breaking |
| Telekinesis | Supported tools | I | Automatically collects drops |
| Harvester | Hoes | III | Area crop management |
| Prospector | Pickaxes | II | Mines connected ore veins|

## Requirements

- Minecraft 26.3
- Fabric Loader
- Fabric API

## Installation

1. Install Fabric Loader for Minecraft 26.3.
2. Install the matching Fabric API version.
3. Download the AetherEnchantments `.jar` file.
4. Place it in your Minecraft `mods` folder.
5. Launch the game using your Fabric profile.

## Development

AetherEnchantments is developed using:

- Java 25
- Minecraft 26.3
- Fabric Loader
- Fabric API
- Gradle

## License

AetherEnchantments is licensed under the MIT License.

You are free to use, modify and redistribute this project,
provided that the original copyright and license notices are retained.

See the [LICENSE](LICENSE) file for details.
