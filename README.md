# Curios Creative Supplement

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-62b47a?style=flat-square)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-47.1.0%2B-e04e14?style=flat-square)](https://files.minecraftforge.net)
[![Curios](https://img.shields.io/badge/Curios-5.4.0%2B-8a63d2?style=flat-square)](https://www.curseforge.com/minecraft/mc-mods/curios)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

Equip any valid [Curios](https://www.curseforge.com/minecraft/mc-mods/curios) item into any Curios slot, **without owning it**. Works in creative mode by default, and can be opened up to operators or all players in survival.

Click a slot, search for an item, click it, done. No more digging through creative tabs, grabbing an item and shift-clicking it into place just to test a curio or set up a build.

<!-- TODO: add screenshots to docs/ and embed them here, e.g.
![Item picker](docs/picker.png)
-->

## Features

- **Searchable item picker** for every Curios slot, including cosmetic slots. Type to filter by name or item id.
- **Mod filter** to narrow the list to a single mod's items, or browse all mods at once.
- **Only valid items are listed.** The picker uses Curios' own slot and tag checks, so you never see something that can't go in that slot.
- **Nothing is taken from or given to your inventory.** Equipped items are fresh copies; clearing a slot just deletes the copy.
- **Server-authoritative.** The client only sends a slot and an item id. The server re-checks permissions, slot validity, `canEquip`/`canUnequip`, Curse of Binding and the blacklist before anything changes.
- **Built for big modpacks.** The item list is scanned incrementally within a small per-tick time budget, so opening the picker never hitches. A single broken modded item can't break it either.
- **Operator controls.** Toggle the feature and choose who may use it from an in-game settings screen. Changes apply immediately and sync to every connected player.
- **No mixins.** Everything hooks into Forge's screen events, and the Curios screen is detected by its slots rather than its class, so it keeps working if Curios reworks its UI.
- **Optional controller support** through [Controllable](https://www.curseforge.com/minecraft/mc-mods/controllable) (experimental, see below).

## Usage

Open your Curios inventory, then:

| Action | Result |
| --- | --- |
| **Left-click** a slot | Open the item picker for that slot |
| **Right-click** a filled slot | Delete the item in that slot |
| **Shift-click**, or click while holding an item | Normal Curios behaviour, untouched |
| **Settings button** (top-right of the Curios panel) | Open the settings screen |

These only apply while the feature is available to you (see [Who can use it](#who-can-use-it)).

The settings screen is also reachable from **Mods → Curios Creative Supplement → Config**, including from the main menu, where it edits the local config file directly.

## Who can use it

| Mode | Config value | Allowed |
| --- | --- | --- |
| Creative Only (default) | `CREATIVE_ONLY` | Players in creative mode |
| Creative + Survival-Op | `CREATIVE_AND_OP` | Creative players, plus operators in any game mode |
| All | `ALL` | Everyone, including survival players |

Spectators can never use it. "Operator" means permission level 2 or higher, or the owner of a singleplayer world.

## Configuration

The config lives at `config/curioscreative-common.toml`. It is loaded on both client and server, and editing it by hand while a server is running pushes the new state to all clients.

| Key | Default | Description |
| --- | --- | --- |
| `general.enabled` | `true` | Master switch for the whole feature. |
| `general.gameMode` | `CREATIVE_ONLY` | Who may use it: `CREATIVE_ONLY`, `CREATIVE_AND_OP` or `ALL`. |
| `rules.bypassCanEquip` | `false` | Ignore `ICurio#canEquip` / `canUnequip` restrictions. Slot and tag validity are still enforced. |
| `rules.bypassBindingCurse` | `false` | Allow replacing or removing items with Curse of Binding. |
| `rules.itemBlacklist` | `[]` | Item ids that can never be equipped through this mod, e.g. `["modid:some_item"]`. |

## Installation

1. Install [Minecraft Forge](https://files.minecraftforge.net) for 1.20.1 (47.1.0 or newer).
2. Install [Curios](https://www.curseforge.com/minecraft/mc-mods/curios) (5.4.0 or newer, below 6.0).
3. Drop `curioscreative-<version>.jar` into your `mods` folder.

**On servers, install it on both the server and every client.** The mod uses its own network channel, so a client without it will be refused.

## Compatibility

| | Supported |
| --- | --- |
| Minecraft | 1.20.1 |
| Loader | Forge 47.1.0+ |
| Required | Curios 5.4.0+ (< 6.0) |
| Optional | Controllable 0.20.0+ (client only) |
| Java | 17 |

Items are equipped as default stacks (no NBT). Anything Curios itself accepts for a slot, from any mod, will appear in the picker.

## Controller support (experimental)

With [Controllable](https://www.curseforge.com/minecraft/mc-mods/controllable) installed, the picker and settings screens are built from standard widgets, so Controllable's normal GUI navigation (D-pad / stick and A) works in them. A small optional compat layer adds shortcuts:

| Button | In the picker |
| --- | --- |
| Left / Right bumper | Previous / next page |
| Y | Cycle the mod filter |
| B | Close |
| Select (rebindable) | Open settings from the game or Curios screen |

The compat layer is compiled only when you build with a Controllable file id (see below) and has not yet been verified against every Controllable release. Without it, everything else works normally.

## Building from source

Requires JDK 17.

```bash
git clone https://github.com/santoskarlde-ops/Curios-Creative-Supplement.git
cd Curios-Creative-Supplement
./gradlew build
```

The jar is written to `build/libs/`. Use `./gradlew runClient` or `./gradlew runServer` to test in a dev environment.

To build **with** the Controllable compat layer, set the CurseForge file id of a Controllable 1.20.1 release in `gradle.properties`:

```properties
controllable_file_id=1234567
```

Leave it empty to build without it. The `compat/controllable` package is then excluded from compilation entirely.

### Project layout

```
dev.curioscreative
├── CuriosCreativeSupplement   mod entry point
├── Config / ModeSetting       common config and the who-can-use-it modes
├── CurioChecks                thin wrappers over Curios' validity checks
├── server/                    authoritative equip logic and login sync
├── network/                   equip, settings-update and settings-sync packets
├── client/                    picker, settings screen, item cache, screen events
└── compat/controllable/       optional controller shortcuts
```

## Credits

- This mod was **made with AI**.
- Ideas, and **creative direction came from me**.
- Inspired by [TaCZ Creative Supplement](https://www.curseforge.com/minecraft/mc-mods/tacz-creative-supplement).
- Built on the [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) by TheIllusiveC4.

## Contributing

Bug reports and pull requests are welcome. When reporting a bug, please include your Minecraft, Forge, Curios and mod versions, and the `latest.log` if something crashed.

## License

Released under the [MIT License](LICENSE).
