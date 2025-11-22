# Equipment System - Phase 3

## Overview

The Equipment System provides complete gear management for RPG and action games, featuring:

- ✅ Equipment slots (Weapon, Armor, Trinket)
- ✅ Stat bonuses with upgrade curves
- ✅ Rarity tiers (Common → Legendary)
- ✅ Loot drops with weighted tables
- ✅ Equipment strengthening with currency
- ✅ Full UI panel with stat comparisons
- ✅ Cloud save integration
- ✅ Event bus integration

## Quick Start

### 1. Test the System

Run `EquipmentDemo.tscn` to see equipment drops, collection, and stat aggregation in action.

```
Open: res://scenes/EquipmentDemo.tscn
- Spawn equipment drops
- Add currency
- Test battle rewards
```

### 2. Access Equipment Manager

```gdscript
var equipment_manager = GameApp.get_equipment_manager()
var stats = equipment_manager.get_total_stats()
var currency = equipment_manager.get_currency()
```

### 3. Open Equipment Panel

From the main demo:
1. Log in (Guest or Email)
2. Click "Equipment" button
3. View equipped items, inventory, and stats
4. Equip/unequip items
5. Strengthen equipped items with currency

## System Components

### Equipment Items
Located in `data/equipment/[weapons|armor|trinkets]/`

Default equipment:
- `starter_sword.tres` - Basic weapon (auto-equipped)
- `starter_armor.tres` - Basic armor (auto-equipped)
- `flaming_blade.tres` - Rare weapon with fire effect
- `dragon_scale_armor.tres` - Epic armor with high defense
- `lucky_coin.tres` - Uncommon trinket with crit bonuses

### Loot Tables
Located in `data/equipment/`

- `loot_table_common.tres` - Common enemy drops
- `loot_table_elite.tres` - Elite/boss drops

### Scripts

Core:
- `game/equipment_item.gd` - Resource definition
- `game/equipment_manager.gd` - Manager singleton
- `game/loot_table.gd` - Loot generation
- `game/battle_loot_handler.gd` - Battle integration
- `game/equipment_drop.gd` - Pickup node

UI:
- `ui/equipment_panel.gd` - Main equipment UI
- `ui/equipment_item_button.gd` - Inventory item button

## Usage Examples

### Spawn Equipment Drop

```gdscript
var drop_scene := preload("res://game/equipment_drop.tscn")
var drop := drop_scene.instantiate()
var item := GameApp.get_equipment_manager().get_item_by_id("flaming_blade")
drop.set_equipment_item(item)
drop.global_position = enemy_position
add_child(drop)
```

### Award Currency After Battle

```gdscript
func _on_enemy_defeated() -> void:
    GameApp.get_equipment_manager().add_currency(100)
```

### Spawn Loot from Table

```gdscript
var loot_table: LootTable = load("res://data/equipment/loot_table_elite.tres")
BattleLootHandler.spawn_loot_drops(
    loot_table,
    enemy.global_position,
    get_tree().current_scene,
    0.1  # +10% luck modifier
)
```

### Apply Equipment Stats to Hero

```gdscript
func update_combat_stats() -> void:
    var equipment_manager = GameApp.get_equipment_manager()
    var bonus_stats = equipment_manager.get_total_stats()
    
    total_damage = base_damage + bonus_stats["damage"]
    total_defense = base_defense + bonus_stats["defense"]
    max_health = base_health + bonus_stats["health"]

func _ready() -> void:
    var equipment_manager = GameApp.get_equipment_manager()
    equipment_manager.stats_updated.connect(update_combat_stats)
    update_combat_stats()
```

### Equip Item from Code

```gdscript
var item = GameApp.get_equipment_manager().get_item_by_id("dragon_scale_armor")
GameApp.get_equipment_manager().equip_item(item)
```

## Creating New Equipment

### Method 1: Duplicate Existing File

1. Duplicate `starter_sword.tres` in equipment directory
2. Rename to `my_weapon.tres`
3. Open in Godot Inspector
4. Change properties:
   - `item_id` - Must be unique
   - `display_name` - Shown in UI
   - `slot` - 0=Weapon, 1=Armor, 2=Trinket
   - `rarity` - 0=Common, 1=Uncommon, 2=Rare, 3=Epic, 4=Legendary
   - Base stats and per-level growth
5. Done! Auto-loaded on game start

### Method 2: Create from Script

See `docs/framework/equipment_quick_reference.md` for copy-paste templates.

## Integration with Game Systems

### Battle Controller

```gdscript
func complete_battle(victory: bool) -> void:
    if victory:
        var currency_reward := 50 + (wave_number * 10)
        GameApp.get_equipment_manager().add_currency(currency_reward)
        
        if enemy_type == "elite":
            var loot_table: LootTable = load("res://data/equipment/loot_table_elite.tres")
            BattleLootHandler.spawn_loot_drops(
                loot_table,
                enemy_position,
                get_tree().current_scene
            )
```

### Cloud Save

Equipment state is automatically saved/loaded with cloud saves. No additional code needed.

```gdscript
# Equipment is serialized automatically
await GameApp.get_data().synchronize_cloud_save()
# Equipment is restored from cloud data
```

### Event Bus

Listen for equipment events:

```gdscript
GameApp.event_bus.subscribe("equipment.stats_updated", _on_stats_changed)
GameApp.event_bus.subscribe("equipment.currency_changed", _on_currency_changed)
GameApp.event_bus.subscribe("equipment.item_picked_up", _on_item_picked)
```

## Stats Reference

Available stats:
- `damage` - Damage bonus
- `defense` - Damage reduction
- `crit_chance` - Critical hit chance (0.0 to 1.0)
- `crit_damage` - Critical damage multiplier
- `attack_speed` - Attack speed multiplier
- `movement_speed` - Movement speed bonus
- `health` - Maximum health bonus
- `health_regen` - Health regeneration per second

## Upgrade Curve Formula

```
stat_at_level = base_stat + (stat_per_level * (level - 1))
cost_at_level = base_upgrade_cost * (cost_multiplier ^ (level - 2))
```

Example for starter_sword:
- Level 1: 10 damage, 0 cost
- Level 2: 12 damage, 100 cost
- Level 3: 14 damage, 150 cost
- Level 4: 16 damage, 225 cost

## UI Keybindings

Add to your project input map for quick access:

```
"toggle_equipment" -> E key
```

Then in your HUD or game controller:

```gdscript
func _input(event: InputEvent) -> void:
    if event.is_action_pressed("toggle_equipment"):
        equipment_panel.open_panel()
```

## Documentation

Comprehensive documentation available:
- `docs/framework/modules/equipment_system.md` - Full system guide
- `docs/framework/equipment_quick_reference.md` - Quick copy-paste reference

## Acceptance Criteria ✅

1. ✅ Hero spawns with default loadout (starter_sword + starter_armor) via data files
2. ✅ EquipmentManager exposes combined stats to other systems
3. ✅ Enemies can drop equipment via loot tables
4. ✅ Picking up items adds to inventory
5. ✅ Equipment panel shows real-time stat comparison
6. ✅ Equipping/unequipping instantly affects stats (observable via get_total_stats())
7. ✅ Strengthening consumes currency, enforces caps, provides feedback
8. ✅ Invalid actions blocked with console messages
9. ✅ Documentation supports adding new gear without modifying core scripts

## Troubleshooting

**Equipment panel not appearing?**
- Check that equipment_button exists in Main.tscn with unique_name_in_owner
- Verify equipment_panel.tscn loads correctly

**Items not loading?**
- Verify .tres files in correct directories
- Check item_id is unique
- Look for errors in debug console

**Stats not applying?**
- Connect to equipment_manager.stats_updated signal
- Call get_total_stats() after equipment changes
- Verify combat formulas read from aggregated stats

**Loot not dropping?**
- Check loot table weights (0.0 to 1.0)
- Verify item_id matches database
- Ensure spawn_scene is valid and in tree

## Performance Notes

- Equipment database loads once at startup (~5-10ms)
- Stat aggregation is O(3) - only 3 equipment slots
- UI updates via signals, not polling
- Loot spawning spreads items to avoid overlap

## Future Enhancements

Potential additions (not in current scope):
- Set bonuses (2-piece, 3-piece effects)
- Socket system for gems/runes
- Item durability and repair
- Trading/marketplace
- Equipment enchanting
- Transmog/visual customization

## Summary

The Equipment System is production-ready and fully integrated with the Universal Framework. All equipment is defined in data files, managed by a singleton, and automatically persists with cloud saves. The system is designed for extension - add new items, slots, or stats without modifying core scripts.

Enjoy building your equipment-based progression system! 🎮⚔️🛡️
