# MaceTPA

MaceTPA is a modern teleport request plugin for PaperMC (and Folia) Minecraft servers. It provides `/tpa`, `/tpahere`, `/back` and related commands with safe teleporting, combat protection, anti-spam, sounds, and a configurable GUI — all with MiniMessage-based messages.

## Features

- **Teleport requests** — `/tpa`, `/tpahere`, `/tpaccept`, `/tpdeny`, `/tpacancel`, and auto-accept via `/tpauto`
- **`/back`** — return to your previous location
- **Safe teleporting** — avoids placing players inside blocks, lava, or other unsafe spots, with a configurable nearby-search radius
- **Combat protection** — built-in combat tracking plus soft integrations with Vault, PlaceholderAPI, DeluxeCombat, CombatLogX, and CombatPlus
- **Anti-spam & cooldowns** — configurable request cooldowns, teleport delay, and per-minute request limits with temporary blocks
- **Confirmation GUI** — optional confirm menu before sending requests, with clickable accept/deny buttons
- **Sounds & actionbar countdown** — fully configurable per-action sounds and an actionbar teleport countdown
- **Blocked worlds** — disable teleport commands in specific worlds (e.g. PvP arenas, dungeons, events)
- **Player settings** — per-player toggles for receiving requests and confirmation prompts, editable via `/tpasettings`
- **Folia support** — uses the appropriate scheduler automatically (Bukkit or Folia)
- **Config auto-updater** — automatically adds new config options after updates, with backups

## Requirements

- Java 17+
- PaperMC (or Folia) 1.20.4+

### Optional soft dependencies

- Vault
- PlaceholderAPI
- DeluxeCombat / CombatLogX / CombatPlus

## Building

This project uses Gradle.

```bash
./gradlew build          # build the plugin jar
./gradlew obfuscate       # build an obfuscated release jar via ProGuard
```

The compiled jar will be in `build/libs/`.

## Installation

1. Download or build `MaceTPA.jar`.
2. Place it in your server's `plugins/` folder.
3. Restart the server.
4. Configure `plugins/MaceTPA/config.yml`, `messages.yml`, and `menus.yml` to your liking, then run `/macetpa reload`.

## Commands

| Command | Description |
| --- | --- |
| `/tpa <player>` | Send a teleport request to a player |
| `/tpahere <player>` | Ask a player to teleport to you |
| `/tpaccept [player]` | Accept a teleport request |
| `/tpdeny [player]` | Deny a teleport request |
| `/tpacancel` | Cancel your outgoing teleport request |
| `/tpauto` | Toggle auto-accepting teleport requests |
| `/tpasettings` | Open the TPA settings menu |
| `/back` | Teleport to your previous location |
| `/macetpa` | Main admin/info command |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `macetpa.use` | true | Base permission to use the plugin |
| `macetpa.tpa` | true | Send `/tpa` requests |
| `macetpa.tpahere` | true | Send `/tpahere` requests |
| `macetpa.accept` | true | Accept requests |
| `macetpa.deny` | true | Deny requests |
| `macetpa.cancel` | true | Cancel requests |
| `macetpa.auto` | true | Use `/tpauto` |
| `macetpa.settings` | true | Use `/tpasettings` |
| `macetpa.reload` | op | Reload the plugin |
| `macetpa.admin` | op | Admin notifications and commands |
| `macetpa.bypass.cooldown` | op | Bypass request cooldown |
| `macetpa.bypass.delay` | op | Bypass teleport delay |
| `macetpa.bypass.antispam` | op | Bypass anti-spam limits |

## License

All rights reserved.
