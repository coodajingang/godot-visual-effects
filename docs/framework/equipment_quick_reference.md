# Equipment System - Quick Reference

## Add New Weapon (5 minutes)

1. Create file: `res://data/equipment/weapons/my_weapon.tres`
2. Copy template:

```gdscript
[gd_resource type="Resource" script_class="EquipmentItem" load_steps=2 format=3]

[ext_resource type="Script" path="res://game/equipment_item.gd" id="1"]

[resource]
script = ExtResource("1")
item_id = "my_weapon"
display_name = "My Weapon"
description = "A powerful weapon."
slot = 0
rarity = 1
max_level = 10
base_damage = 20.0
damage_per_level = 4.0
base_upgrade_cost = 150
cost_per_level_multiplier = 1.5
```

3. Done! Item auto-loads on game start.

## Add New Armor (5 minutes)

1. Create file: `res://data/equipment/armor/my_armor.tres`
2. Copy template:

```gdscript
[gd_resource type="Resource" script_class="EquipmentItem" load_steps=2 format=3]

[ext_resource type="Script" path="res://game/equipment_item.gd" id="1"]

[resource]
script = ExtResource("1")
item_id = "my_armor"
display_name = "My Armor"
description = "Protective armor."
slot = 1
rarity = 2
max_level = 10
base_defense = 15.0
base_health = 100.0
defense_per_level = 2.5
health_per_level = 15.0
base_upgrade_cost = 200
cost_per_level_multiplier = 1.5
```

3. Done!

## Add New Trinket (5 minutes)

1. Create file: `res://data/equipment/trinkets/my_trinket.tres`
2. Copy template:

```gdscript
[gd_resource type="Resource" script_class="EquipmentItem" load_steps=2 format=3]

[ext_resource type="Script" path="res://game/equipment_item.gd" id="1"]

[resource]
script = ExtResource("1")
item_id = "my_trinket"
display_name = "My Trinket"
description = "A magical trinket."
slot = 2
rarity = 3
max_level = 10
base_crit_chance = 0.2
base_movement_speed = 10.0
crit_chance_per_level = 0.03
movement_speed_per_level = 2.0
base_upgrade_cost = 300
cost_per_level_multiplier = 1.6
```

3. Done!

## Add Loot Table (5 minutes)

1. Create file: `res://data/equipment/loot_table_myenemy.tres`
2. Copy template:

```gdscript
[gd_resource type="Resource" script_class="LootTable" load_steps=2 format=3]

[ext_resource type="Script" path="res://game/loot_table.gd" id="1"]

[resource]
script = ExtResource("1")
table_id = "my_enemy_loot"
loot_entries = Array[Dictionary]([
    {
        "item_id": "my_weapon",
        "weight": 0.2,
        "min_level": 1,
        "max_level": 5
    },
    {
        "item_id": "my_armor",
        "weight": 0.15,
        "min_level": 1,
        "max_level": 5
    }
])
```

3. Assign to enemy:

```gdscript
@export var loot_table: LootTable

func die() -> void:
    if loot_table:
        BattleLootHandler.spawn_loot_drops(
            loot_table,
            global_position,
            get_tree().current_scene
        )
```

## Spawn Equipment Drop

```gdscript
var drop_scene := preload("res://game/equipment_drop.tscn")
var drop := drop_scene.instantiate()
var item := GameApp.get_equipment_manager().get_item_by_id("my_weapon")
drop.set_equipment_item(item)
drop.global_position = spawn_position
add_child(drop)
```

## Award Currency

```gdscript
GameApp.get_equipment_manager().add_currency(100)
```

## Check Equipment Stats in Combat

```gdscript
var equipment_manager = GameApp.get_equipment_manager()
var bonus_stats = equipment_manager.get_total_stats()
var damage = base_damage + bonus_stats["damage"]
```

## Open Equipment Panel

```gdscript
# In your HUD or input handler
var equipment_panel: EquipmentPanel = $EquipmentPanel
equipment_panel.open_panel()
```

## Rarity Values

```
0 = COMMON (White)
1 = UNCOMMON (Green)
2 = RARE (Blue)
3 = EPIC (Purple)
4 = LEGENDARY (Orange)
```

## Slot Values

```
0 = WEAPON
1 = ARMOR
2 = TRINKET
```

## All Available Stats

```
base_damage
base_defense
base_crit_chance
base_crit_damage
base_attack_speed
base_movement_speed
base_health
base_health_regen
```

## Upgrade Cost Formula

```
cost = base_upgrade_cost * (cost_per_level_multiplier ^ (target_level - 2))
```

Example: Base cost 100, multiplier 1.5
- Level 1→2: 100
- Level 2→3: 150
- Level 3→4: 225
- Level 4→5: 338

## Event Bus Events

Listen for these events:

```gdscript
GameApp.event_bus.subscribe("equipment.stats_updated", _on_stats_updated)
GameApp.event_bus.subscribe("equipment.currency_changed", _on_currency_changed)
GameApp.event_bus.subscribe("equipment.item_picked_up", _on_item_picked)
```

## Common Tasks

### Give player item directly
```gdscript
var item = GameApp.get_equipment_manager().get_item_by_id("my_weapon")
GameApp.get_equipment_manager().add_to_inventory(item)
```

### Equip item from code
```gdscript
var item = GameApp.get_equipment_manager().get_item_by_id("my_weapon")
GameApp.get_equipment_manager().equip_item(item)
```

### Get equipped weapon
```gdscript
var weapon = GameApp.get_equipment_manager().get_equipped_item(EquipmentItem.Slot.WEAPON)
if weapon:
    print(weapon.display_name)
```

### Strengthen item
```gdscript
var item = GameApp.get_equipment_manager().get_equipped_item(EquipmentItem.Slot.WEAPON)
if GameApp.get_equipment_manager().strengthen_item(item):
    print("Upgrade successful!")
```

### Compare two items
```gdscript
var equipped = GameApp.get_equipment_manager().get_equipped_item(EquipmentItem.Slot.WEAPON)
var new_item = GameApp.get_equipment_manager().get_item_by_id("better_weapon")
var diff = GameApp.get_equipment_manager().compare_stats(equipped, new_item)
print("Damage difference: ", diff["damage"])
```

## Troubleshooting

**Item not showing up?**
- File must be in `data/equipment/[weapons|armor|trinkets]/`
- Extension must be `.tres`
- `item_id` must be unique

**Stats not applying?**
- Connect to `stats_updated` signal
- Call `get_total_stats()` to refresh values

**Loot not dropping?**
- Check `weight` is between 0.0 and 1.0
- Verify `item_id` matches database
- Ensure spawn_scene is in tree

**Can't upgrade?**
- Check currency: `get_equipment_manager().get_currency()`
- Verify item is not max level
- Check upgrade cost: `item.get_upgrade_cost()`
