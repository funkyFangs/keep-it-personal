# Description

Minecraft's [`keepInventory`](https://minecraft.wiki/w/Game_rule#keepInventory) game rule provides an important feature for players, but it unfortunately fails to account for mixed preferences in servers. Keep It Personal is a Fabric mod which seeks to remedy this by providing commands to individually customize what players drop on death. With these commands, players can:
* Choose what they drop on death without affecting others
* Customize what parts of their inventory are dropped

Keep It Personal is designed to be lightweight and compatible; it's entirely server-side and is fully compatible with vanilla clients.

# Installation

To install this mod, simply add the JAR to the `mods` directory in the root directory of the server.

# Usage

This mod uses a single command, `/keeping`, along with multiple subcommands as outlined below.

## Death Preferences

The following table contains the options a player can choose to keep on death.

| Name | Description |
| :-: | :-- |
| `armor` | The player's equipped helmet, breastplate, leggings, and boots |
| `offhand` | The item held in the player's off-hand |
| `hotbar` | Items in the player's hotbar |
| `inventory` | Items in the player's main inventory, excluding the options above |
| `experience` | The player's experience |
| `everything` | All of the above |
| `nothing` | None of the above |

## Subcommands

### View All Preferences

To view a list of your current preferences, run `/keeping`.

### View Individual Preference

To view whether you have selected a specific preference, run `/keeping <preference>`.

### Keep Everything on Death

To keep everything in your inventory on death, run `/keeping everything`.

### Clear All Preferences

To clear your preferences and drop everything on death, run `/keeping nothing`.

### Set a Specific Preference

To set a specific preference, run `/keeping <preference> <true | false>`, where `true` indicates that you wish to keep the preferred items on death, and `false` indicates you wish to drop them.

# Configuration

Configuration support is planned but not currently available.

# Permissions

Permission support is planned but not currently available.
