# Changelog

All notable changes to this project are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/).

## [1.0.0]

Initial release.

### Added
- Searchable item picker for any Curios slot, including cosmetic slots, with a per-mod filter and paging.
- Left-click a Curios slot to pick an item, right-click a filled slot to delete it. Shift-click and held-item clicks keep vanilla Curios behaviour.
- Server-authoritative equipping: slot validity, `canEquip` / `canUnequip`, Curse of Binding and item blacklist are all checked on the server. Only item ids are sent over the network.
- In-game settings screen (button on the Curios panel and via Mods → Config) with an enable toggle and a game-mode setting: Creative Only, Creative + Survival-Op, or All.
- Settings sync to all connected players on login and on every change, including hand edits to the config file.
- Config options: `enabled`, `gameMode`, `bypassCanEquip`, `bypassBindingCurse`, `itemBlacklist`.
- Incremental, time-budgeted item scanning so large modpacks don't hitch when the picker opens.
- Optional, experimental Controllable support: bumpers change page, Y cycles the mod filter, B closes, and a rebindable button opens settings.
