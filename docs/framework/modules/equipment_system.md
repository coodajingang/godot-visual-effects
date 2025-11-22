# Equipment System Documentation

## Overview

The Equipment System provides a complete gear management solution for RPG and action games, featuring equipment slots, stat bonuses, rarity tiers, upgrade curves, and loot drops. The system integrates seamlessly with the Universal Framework's module architecture and event bus.

## Architecture

### Core Components

1. **EquipmentItem** (Resource) - Base resource class defining equipment properties
2. **EquipmentManager** (Node) - Singleton manager for loadout and inventory
3. **EquipmentDrop** (Area2D) - Pickup node for loot collection
4. **LootTable** (Resource) - Weighted loot generation system
5. **BattleLootHandler** (Static) - Helper for battle rewards
6. **EquipmentPanel** (Control) - UI for managing equipment

### Integration Points

- **GameApp** - Initializes EquipmentManager as a singleton
- **DataKit** - Serializes/deserializes equipment state for cloud saves
- **GameKit** - Awards currency on battle completion
- **EventBus** - Publishes equipment events for other systems

## Equipment Item Resource

### File Location
`res://game/equipment_item.gd`

### Properties

#### Basic Properties
- `item_id: String` - Unique identifier
- `display_name: String` - Display name
- `description: String` - Item description
- `slot: Slot` - Equipment slot (WEAPON, ARMOR, TRINKET)
- `rarity: Rarity` - Rarity tier (COMMON, UNCOMMON, RARE, EPIC, LEGENDARY)
- `icon: Texture2D` - Item icon
- `max_level: int` - Maximum upgrade level

#### Base Stats
- `base_damage: float`
- `base_defense: float`
- `base_crit_chance: float`
- `base_crit_damage: float`
- `base_attack_speed: float`
- `base_movement_speed: float`
- `base_health: float`
- `base_health_regen: float`

#### Upgrade Curve
Each stat has a corresponding `*_per_level` property that defines growth per level.

#### Upgrade Costs
- `base_upgrade_cost: int` - Base cost to upgrade
- `cost_per_level_multiplier: float` - Cost scaling factor

#### Special Effects
- `has_special_effect: bool` - Whether item has triggered effect
- `effect_id: String` - Effect identifier
- `effect_chance: float` - Trigger probability
- `effect_description: String` - Effect description

### Methods

```gdscript
# Get stat value at specified level
func get_stat(stat_name: String, level: int = -1) -> float

# Get all stats as dictionary
func get_all_stats(level: int = -1) -> Dictionary

# Get upgrade cost for target level
func get_upgrade_cost(target_level: int = -1) -> int

# Check if item can be upgraded
func can_upgrade() -> bool

# Increase item level by 1
func level_up() -> bool

# Serialize item state
func serialize() -> Dictionary

# Restore item state from data
static func deserialize(data: Dictionary, item_resource: EquipmentItem) -> EquipmentItem
```

## Equipment Manager

### File Location
`res://game/equipment_manager.gd`

### Signals

```gdscript
signal equipment_changed(slot: EquipmentItem.Slot, item: EquipmentItem)
signal equipment_upgraded(item: EquipmentItem, new_level: int)
signal stats_updated(total_stats: Dictionary)
signal inventory_changed(inventory: Array[EquipmentItem])
```

### Properties

- `equipped_items: Dictionary` - Current loadout indexed by slot
- `inventory: Array[EquipmentItem]` - Available items
- `item_database: Dictionary` - All equipment definitions
- `currency: int` - Upgrade currency

### Key Methods

```gdscript
# Equip an item (auto-swaps if slot occupied)
func equip_item(item: EquipmentItem) -> bool

# Unequip item from slot
func unequip_item(slot: EquipmentItem.Slot) -> bool

# Get currently equipped item
func get_equipped_item(slot: EquipmentItem.Slot) -> EquipmentItem

# Add item to inventory
func add_to_inventory(item: EquipmentItem) -> void

# Get aggregated stats from all equipped items
func get_total_stats() -> Dictionary

# Upgrade item using currency
func strengthen_item(item: EquipmentItem, material_cost: int = -1) -> bool

# Add upgrade currency
func add_currency(amount: int) -> void

# Compare stats between two items
func compare_stats(item_a: EquipmentItem, item_b: EquipmentItem) -> Dictionary

# Serialize entire equipment state
func serialize() -> Dictionary

# Restore equipment state
func deserialize(data: Dictionary) -> void
```

## Creating New Equipment

### Step 1: Create Resource File

1. Navigate to `res://data/equipment/[weapons|armor|trinkets]/`
2. Create new `.tres` file
3. Set script to `res://game/equipment_item.gd`

### Step 2: Configure Properties

Example starter weapon:
```gdscript
[resource]
script = ExtResource("res://game/equipment_item.gd")
item_id = "iron_sword"
display_name = "Iron Sword"
description = "A sturdy iron blade."
slot = 0  # WEAPON
rarity = 0  # COMMON
max_level = 10
base_damage = 15.0
damage_per_level = 3.0
base_upgrade_cost = 100
cost_per_level_multiplier = 1.5
```

### Step 3: Test in Engine

The EquipmentManager automatically loads all `.tres` files from the equipment directories on startup. Access items via:

```gdscript
var equipment_manager = GameApp.get_equipment_manager()
var item = equipment_manager.get_item_by_id("iron_sword")
equipment_manager.add_to_inventory(item)
```

## Loot Tables

### File Location
`res://game/loot_table.gd`

### Creating Loot Tables

```gdscript
[resource]
script = ExtResource("res://game/loot_table.gd")
table_id = "boss_loot"
loot_entries = Array[Dictionary]([
    {
        "item_id": "legendary_sword",
        "weight": 0.05,  # 5% drop chance
        "min_level": 5,
        "max_level": 10
    },
    {
        "item_id": "epic_armor",
        "weight": 0.15,  # 15% drop chance
        "min_level": 3,
        "max_level": 8
    }
])
```

### Spawning Loot

```gdscript
var loot_table: LootTable = load("res://data/equipment/loot_table_boss.tres")
var spawn_pos := enemy.global_position
var luck_bonus := 0.1  # +10% drop rates

BattleLootHandler.spawn_loot_drops(
    loot_table,
    spawn_pos,
    get_tree().current_scene,
    luck_bonus
)
```

## Equipment Drops

### Scene
`res://game/equipment_drop.tscn`

### Properties
- `equipment_item: EquipmentItem` - Item to drop
- `auto_pickup: bool` - Automatically collect on collision
- `pickup_range: float` - Detection radius
- `float_animation: bool` - Enable floating animation

### Manual Spawning

```gdscript
var drop_scene := preload("res://game/equipment_drop.tscn")
var drop := drop_scene.instantiate()
drop.global_position = spawn_position
drop.set_equipment_item(my_item)
add_child(drop)
```

## Equipment UI Panel

### File Location
`res://ui/equipment_panel.tscn`

### Opening Panel

```gdscript
var equipment_panel: EquipmentPanel = preload("res://ui/equipment_panel.tscn").instantiate()
add_child(equipment_panel)
equipment_panel.open_panel()
```

### Features

1. **Equipment Slots** - Visual display of equipped items
2. **Inventory Grid** - Scrollable list of available items
3. **Stat Comparison** - Compare unequipped items to current gear
4. **Strengthening Panel** - Upgrade equipped items with currency
5. **Real-time Updates** - Automatically reflects equipment changes

### Keybinding Integration

Add to your input map and HUD:

```gdscript
func _input(event: InputEvent) -> void:
    if event.is_action_pressed("toggle_equipment"):
        if equipment_panel.visible:
            equipment_panel.hide()
        else:
            equipment_panel.open_panel()
```

## Stat Integration

### Reading Equipment Stats

Equipment stats are aggregated and available via:

```gdscript
var equipment_manager = GameApp.get_equipment_manager()
var stats = equipment_manager.get_total_stats()

# Use in combat calculations
var total_damage = hero_base_damage + stats["damage"]
var total_defense = hero_base_defense + stats["defense"]
var crit_chance = base_crit + stats["crit_chance"]
```

### Applying to Game Entities

```gdscript
# In hero/player script
func update_stats() -> void:
    var equipment_manager = GameApp.get_equipment_manager()
    if equipment_manager:
        var bonus_stats = equipment_manager.get_total_stats()
        damage = base_damage + bonus_stats["damage"]
        defense = base_defense + bonus_stats["defense"]
        max_health = base_health + bonus_stats["health"]
        # ... etc

# Connect to equipment changes
func _ready() -> void:
    var equipment_manager = GameApp.get_equipment_manager()
    if equipment_manager:
        equipment_manager.stats_updated.connect(update_stats)
```

## Battle Integration

### Awarding Currency

```gdscript
# After battle victory
var equipment_manager = GameApp.get_equipment_manager()
equipment_manager.add_currency(victory_reward)
```

### Complete Integration Example

```gdscript
func _on_enemy_defeated(enemy: Node2D) -> void:
    # Award currency
    var currency_reward := 50 + randi() % 50
    BattleLootHandler.spawn_currency_drop(
        currency_reward,
        enemy.global_position,
        get_tree().current_scene
    )
    
    # Roll loot table
    var loot_table: LootTable = enemy.get_loot_table()
    if loot_table:
        BattleLootHandler.spawn_loot_drops(
            loot_table,
            enemy.global_position,
            get_tree().current_scene,
            player_luck_modifier
        )
```

## Event Bus Integration

### Published Events

```gdscript
# Equipment state changes
"equipment.stats_updated" -> {stats: Dictionary}
"equipment.currency_changed" -> {currency: int}
"equipment.item_picked_up" -> {item_id: String, item_name: String, rarity: String}
```

### Subscribing to Events

```gdscript
func _ready() -> void:
    GameApp.event_bus.subscribe("equipment.stats_updated", _on_stats_updated)
    GameApp.event_bus.subscribe("equipment.item_picked_up", _on_item_picked_up)

func _on_stats_updated(payload: Dictionary) -> void:
    var stats = payload.get("stats", {})
    # Update UI or recalculate values

func _on_item_picked_up(payload: Dictionary) -> void:
    show_notification("Found: %s" % payload["item_name"])
```

## Cloud Save Persistence

Equipment state is automatically serialized when calling `DataKit.synchronize_cloud_save()`. The serialized data includes:

- Equipped items (ID and level)
- Inventory items (ID and level)
- Current currency

No additional code required - DataKit handles equipment persistence automatically.

## Extending the System

### Adding New Slots

1. Add slot to `EquipmentItem.Slot` enum
2. Add slot to `EquipmentManager.equipped_items`
3. Update UI panel with new slot widget
4. Create data directory: `data/equipment/[new_slot_name]/`

### Adding New Stats

1. Add base and per-level properties to `EquipmentItem`
2. Add stat to `get_stat()` match statement
3. Add stat to `get_all_stats()` dictionary
4. Update total stats calculation in `EquipmentManager`

### Custom Effects

Equipment effects are stored but not executed by the core system. Implement effect handlers in your game logic:

```gdscript
func apply_equipment_effects() -> void:
    var equipment_manager = GameApp.get_equipment_manager()
    for slot in equipment_manager.equipped_items:
        var item = equipment_manager.get_equipped_item(slot)
        if item and item.has_special_effect:
            match item.effect_id:
                "burn_damage":
                    setup_burn_effect(item.effect_chance)
                "bonus_gold":
                    gold_multiplier += item.effect_chance
                # Add custom effects here
```

## Best Practices

1. **Use Data-Driven Design** - Define equipment in `.tres` files, not code
2. **Leverage Event Bus** - React to equipment changes through events
3. **Aggregate Stats Early** - Cache total stats rather than recalculating per frame
4. **Validate Currency** - `strengthen_item()` enforces currency checks
5. **Duplicate Items** - Always duplicate items from database before adding to inventory
6. **Test Upgrade Curves** - Verify stat scaling matches game balance expectations

## Troubleshooting

### Items not appearing in database
- Verify `.tres` files are in `data/equipment/[weapons|armor|trinkets]/`
- Check file extension is `.tres` not `.res` or `.tres.remap`
- Ensure item has unique `item_id`

### Stats not updating in combat
- Connect to `equipment_manager.stats_updated` signal
- Call `update_stats()` after equipping/upgrading items
- Verify combat formulas read from `get_total_stats()`

### Loot not dropping
- Check loot table `weight` values (0.0 to 1.0 range)
- Verify `item_id` in loot table matches database
- Ensure spawn scene is valid and in tree

### Currency not awarded
- Check `BattleController` or battle system calls `add_currency()`
- Verify `GameKit.complete_battle()` includes `"currency"` in result
- Listen for `"equipment.currency_changed"` event to debug

## Example: Complete Equipment Flow

```gdscript
# 1. Create hero with equipment bonuses
class_name Hero extends CharacterBody2D

var base_damage := 10.0
var base_defense := 5.0
var total_damage := 10.0
var total_defense := 5.0

func _ready() -> void:
    var equipment_manager = GameApp.get_equipment_manager()
    equipment_manager.stats_updated.connect(_update_stats)
    _update_stats(equipment_manager.get_total_stats())

func _update_stats(bonus_stats: Dictionary) -> void:
    total_damage = base_damage + bonus_stats.get("damage", 0.0)
    total_defense = base_defense + bonus_stats.get("defense", 0.0)

# 2. Enemy drops loot on death
class_name Enemy extends CharacterBody2D

@export var loot_table: LootTable
@export var currency_drop: int = 50

func die() -> void:
    if loot_table:
        BattleLootHandler.spawn_loot_drops(
            loot_table,
            global_position,
            get_tree().current_scene
        )
    BattleLootHandler.spawn_currency_drop(
        currency_drop,
        global_position,
        get_tree().current_scene
    )
    queue_free()

# 3. HUD shows equipment panel
class_name GameHUD extends CanvasLayer

@onready var equipment_panel: EquipmentPanel = $EquipmentPanel

func _input(event: InputEvent) -> void:
    if event.is_action_pressed("ui_inventory"):
        equipment_panel.open_panel()
```

## Performance Considerations

- Equipment database loads once at startup
- Stat aggregation is O(3) - only three equipment slots
- UI updates triggered by signals, not polling
- Item duplication is shallow - textures shared
- Loot spawning spreads items radially to avoid overlaps

## Summary

The Equipment System provides a production-ready foundation for gear-based progression. Items are defined in data files, managed by a central singleton, and integrate with cloud saves, battle rewards, and UI automatically. The system is designed for extension - add new slots, stats, or effects without modifying core scripts.
