# Changelog

All notable changes to this project will be documented in this file.

The format is inspired by [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2024-11-22
### Added - Equipment System Phase 3
- Complete equipment system with weapon, armor, and trinket slots
- EquipmentItem resource class with base stats, rarity tiers, upgrade curves, and special effects
- EquipmentManager singleton for loadout tracking, stat aggregation, and inventory management
- Loot table system with weighted drop rates and level ranges
- EquipmentDrop pickup nodes with auto-collection and floating animations
- BattleLootHandler for battle reward integration
- Full equipment UI panel with slot display, inventory grid, stat comparison, and strengthening
- 7 default equipment items spanning all rarity tiers (Common to Legendary)
- 2 loot tables for common and elite enemies
- Equipment integration with DataKit for cloud save persistence
- Equipment integration with GameKit for battle currency rewards
- Equipment button added to main demo UI
- EquipmentDemo scene for testing equipment spawning and collection
- Comprehensive documentation: full system guide and quick reference
- Event bus events: stats_updated, currency_changed, item_picked_up

## [0.1.0] - 2024-11-13
### Added
- Initial Godot 4 project scaffold (`/godot4-universal-framework`) with modular architecture.
- Event-driven framework core (`GameApp`) and reusable addons (EventBus, AuthKit, DataKit, PayKit, SocialKit, NetKit, LogKit, GameKit).
- Supabase and Firebase backend adapters plus a mock backend service for offline demos.
- Demo UI (`Main.tscn`) showcasing login → profile loading → cloud sync → store purchase flow.
- Developer tooling: debug console singleton, localization manager, minimal environment configuration.
- Comprehensive documentation set: architecture overview, module deep dives, backend setup guide, demo instructions.

### Planned
- Concrete Supabase REST service implementation and Firebase REST/WebSocket bindings.
- Automated integration tests for key gameplay flows and backend adapters.
- Editor UI widgets for inspecting EventBus traffic and runtime module states.
