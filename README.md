# Description

Minecraft's [`keepInventory`](https://minecraft.wiki/w/Game_rule#keepInventory) game rule provides an important feature for players, but it unfortunately fails to account for mixed preferences in servers. Keep It Personal is a Fabric mod which seeks to remedy this by providing commands to individually customize what players drop on death. With these commands, players can:
* Choose what they drop on death without affecting others
* Customize what parts of their inventory are dropped

Keep It Personal is designed to be lightweight and compatible; it's entirely server-side and is fully compatible with vanilla clients.

# Installation

To install this mod, simply add the JAR to the `mods` directory in the root directory of the server.

# Usage

This mod uses a single command, `/kip`, along with multiple subcommands as outlined below.

## Death Preferences

The following table contains the options a player can choose to keep on death.

|     Name     | Description                                                                |
|:------------:|:---------------------------------------------------------------------------|
|   `armor`    | The player's equipped helmet, breastplate, leggings, and boots             |
|  `offhand`   | The item held in the player's off-hand                                     |
|   `hotbar`   | Items in the player's hotbar                                               |
| `inventory`  | Items in the player's main inventory, excluding the options above          |
|   `cursed`   | Items in the player's inventory that are enchanted with Curse of Vanishing |
| `experience` | The player's experience                                                    |

## Subcommands

### View All Preferences

To view a list of your current preferences, run `/kip`.

### View Individual Preference

To view whether you have selected a specific preference, run `/kip <preference>`.

### Keep Everything on Death

To keep everything in your inventory on death, run `/kip everything`.

### Clear All Preferences

To clear your preferences and drop everything on death, run `/kip nothing`.

### Set a Specific Preference

To set a specific preference, run `/kip <preference> <true | false>`, where `true` indicates that you wish to keep the preferred items on death, and `false` indicates you wish to drop them.

# Configuration

Keep It Personal can be configured in the `config/keep_it_personal.toml` file. If you are unfamiliar with [TOML](https://toml.io/en/), it is highly recommended that you learn the specification for ease of configuration.

## Disable Specific Preferences

If you want to disable a specific set of preferences from being used by anyone, you can use the `preferences.disabled` property. For example, the following
disables every preference but `experience`.

```toml
[preferences]
disabled = ['armor', 'offhand', 'hotbar', 'inventory', 'cursed']
```

### Defaults

By default, no preferences are disabled.

## Enable Specific Preferences

If you want a specific set of preferences to _always_ be enabled, you can similarly use the `preferences.enabled` property. For example, the following forces `armor` and `offhand` to be enabled.

```toml
[preferences]
enabled = ['armor', 'offhand']
```

### Defaults

By default, no preferences are enabled.

# Permissions

This mod optionally supports [fabric-permissions-api](https://modrinth.com/mod/fabric-permissions-api) for command permissions. Each command can be controlled with a specific permission. The following table contains the full list of permissions.

|               Permission                | Description                                                 |
|:---------------------------------------:|:------------------------------------------------------------|
|       `keep_it_personal.kip`        | View all the preferences they have selected                 |
| `keep_it_personal.kip.<preference>` | View and update a specific preference                       |
|  `keep_it_personal.kip.everything`  | Add every available preference to selected preferences      |
|   `keep_it_personal.kip.nothing`    | Remove every available preference from selected preferences |

## Permission Configuration Properties

For servers which do not use [fabric-permissions-api](https://modrinth.com/mod/fabric-permissions-api) or also use OP permission levels, you may specify a default permission level for these commands. This is set as `permissions.permissionLevel` in the configuration file.

For example, to let anyone with permission level 1 be able to use this command, you would specify the following in `keep_it_personal.toml`.

```toml
[permissions]
permissionLevel = 1
```

By default, the permission level is `0`. Note that the permission level overrides permissions set in the [fabric-permissions-api](https://modrinth.com/mod/fabric-permissions-api).

### Defaults

The default permission level is 0 which is given to every player. If you wish to control permissions with a permission
manager, you should update the permission level accordingly.
