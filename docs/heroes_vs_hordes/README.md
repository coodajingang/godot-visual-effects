# Heroes vs Hordes - Leveling System Documentation

## Overview

The Heroes vs Hordes leveling system is a modular, data-driven framework for managing hero progression, XP economy, and skill selection. It integrates seamlessly with the Godot4 Universal Game Framework and allows designers to configure new skills and adjust progression curves without touching code.

## Architecture Overview

The leveling system consists of five core components:

1. **ExperienceManager** – Tracks XP accumulation and level progression
2. **SkillService** – Manages available skills and applies selected upgrades
3. **LevelCurve** – Defines XP thresholds and automatic stat scaling
4. **HeroStats** – Encapsulates hero base attributes
5. **SkillDefinition** – Describes passive/active upgrades with stacking rules

### Data Flow

```
Enemy Death
    ↓
XP Orb Drop (XPOrb.tscn)
    ↓
Hero Collision / Collection
    ↓
ExperienceManager.add_xp()
    ↓
Check Level Threshold (LevelCurve)
    ↓
[Level Up?]
    ├─ Update Hero Stats (LevelCurve scaling)
    ├─ Emit level_up signal
    ├─ Show LevelUpPanel
    └─ Pause Gameplay
        ↓
    [Player Selects Skill]
        ↓
    SkillService.apply_skill()
        ├─ Apply stat modifiers to HeroStats
        ├─ Log selection (analytics)
        └─ Resume Gameplay
```

## Core Components

### 1. ExperienceManager

**Location:** `res://addons/universal_framework/game/experience_manager.gd`

Handles XP tracking and level progression.

#### Key Methods

```gdscript
func add_xp(amount: int) -> void
# Adds XP and checks for level-up

func get_current_level() -> int
# Returns current hero level

func get_current_xp() -> int
# Returns current XP within the level

func get_xp_needed_for_next_level() -> int
# Returns total XP required for next level

func get_xp_progress_percentage() -> float
# Returns 0.0 to 1.0 for progress bar

func set_base_stats(stats: HeroStats) -> void
# Set or update base hero stats
```

#### Signals

```gdscript
signal xp_gained(amount: int, current_xp: int, total_xp: int)
# Emitted whenever XP is collected

signal level_up(new_level: int, remaining_xp: int)
# Emitted when hero reaches next level

signal level_progress_changed(current_xp: int, xp_needed: int)
# Emitted to update UI progress bars
```

#### Example Usage

```gdscript
var base_stats = HeroStats.new(100, 10, 300)
var curve = preload("res://path/to/level_curve.tres")
var exp_manager = ExperienceManager.new(base_stats, curve)

exp_manager.add_xp(50)  # Hero gains 50 XP
# ... monitor signals for UI updates
```

### 2. HeroStats

**Location:** `res://addons/universal_framework/game/resources/hero_stats.gd`

Defines hero attributes as a resource.

#### Attributes

- `max_hp: float` – Maximum health points
- `damage: float` – Base damage output
- `move_speed: float` – Movement speed in pixels/second
- `attack_speed: float` – Attack rate multiplier
- `critical_chance: float` – Chance to deal critical damage (0.0–1.0)
- `critical_multiplier: float` – Damage multiplier for crits
- `armor: float` – Damage reduction
- `dodge_chance: float` – Chance to avoid damage (0.0–1.0)

#### Example

```gdscript
var stats = HeroStats.new(
    p_max_hp = 120.0,
    p_damage = 15.0,
    p_move_speed = 350.0
)
print(stats.max_hp)  # 120.0
```

### 3. LevelCurve

**Location:** `res://addons/universal_framework/game/resources/level_curve.gd`

Configures XP thresholds and stat scaling factors.

#### Key Methods

```gdscript
func get_xp_for_level(level: int) -> int
# Returns XP required to reach this level (relative)

func apply_level_scaling(base_stats: HeroStats, level: int) -> HeroStats
# Returns scaled stats based on level multipliers

func get_total_xp_for_level(level: int) -> int
# Returns cumulative XP needed from level 1
```

#### Configuration

```gdscript
var curve = LevelCurve.new()

# Adjust XP thresholds (per level)
curve.xp_thresholds = [100, 250, 450, 700, 1000, ...]

# Adjust scaling factors
curve.hp_scaling = 1.1           # 10% HP increase per level
curve.damage_scaling = 1.05      # 5% damage increase per level
curve.move_speed_scaling = 1.02  # 2% speed increase per level
```

### 4. SkillDefinition

**Location:** `res://addons/universal_framework/game/resources/skill_definition.gd`

Encapsulates a skill upgrade with modifiers and display data.

#### Attributes

- `skill_id: String` – Unique identifier (e.g., "crit_chance_boost")
- `name: String` – Display name (e.g., "Sharpened Edge")
- `description: String` – Flavor text
- `icon_path: String` – Path to skill icon (for UI)
- `skill_type: String` – "passive" or "active"
- `weight: float` – Relative selection probability
- `stat_modifiers: Dictionary` – Stat changes to apply
- `max_stacks: int` – Max times this skill can be selected (-1 = unlimited)
- `effect_script_path: String` – Optional custom effect script

#### Stat Modifiers Format

```gdscript
stat_modifiers = {
    "damage": {
        "type": "mult",
        "value": 1.1  # 10% damage increase
    },
    "critical_chance": {
        "type": "add",
        "value": 0.05  # +5% crit chance
    },
    "max_hp": {
        "type": "percent",
        "value": 0.15  # +15% HP
    }
}
```

#### Modifier Types

| Type | Description | Example |
|------|-------------|---------|
| `"add"` | Add value directly | `damage: +10` |
| `"mult"` | Multiply current value | `max_hp: * 1.2` (20% increase) |
| `"percent"` | Add percentage of base | `move_speed: +10%` |

### 5. SkillService

**Location:** `res://addons/universal_framework/game/skill_service.gd`

Manages skill selection, application, and logging.

#### Key Methods

```gdscript
func add_available_skill(skill: SkillDefinition) -> void
# Register a skill for selection pools

func select_random_skills(count: int = 3) -> Array[SkillDefinition]
# Returns N random skills (respects weights & stack limits)

func apply_skill(skill: SkillDefinition, level: int = 1) -> void
# Applies a skill to hero stats and logs the selection

func get_applied_skill_count(skill_id: String) -> int
# Returns how many times a skill has been applied (stacking count)
```

#### Signals

```gdscript
signal skill_applied(skill: SkillDefinition, hero_stats: HeroStats)
# Emitted when a skill is successfully applied

signal skill_selection_logged(skill_id: String, level: int)
# Emitted after analytics logging
```

## UI Components

### LevelUpPanel

**Scene:** `res://addons/universal_framework/game/ui/level_up_panel.tscn`

Modal panel shown on level-up. Pauses gameplay and displays three skill options.

#### Features

- **Pause gameplay** – `get_tree().paused = true` during selection
- **Keyboard navigation** – Arrow keys to navigate, Enter to confirm, 1-3 for quick select
- **Mouse support** – Click to select, confirm via button
- **Stat preview** – Shows stat changes for each skill

#### Example Integration

```gdscript
func _on_level_up(level: int) -> void:
    var skills = skill_service.select_random_skills(3)
    var panel = preload("res://addons/universal_framework/game/ui/level_up_panel.tscn").instantiate()
    add_child(panel)
    panel.display_level_up(level, skills)
    panel.skill_selected.connect(func(skill): 
        skill_service.apply_skill(skill, level)
    )
```

### GameHUD

**Scene:** `res://addons/universal_framework/game/ui/game_hud.tscn`

HUD overlay showing:
- Current level
- XP progress bar
- Applied skill icons
- Active buffs

#### Setup

```gdscript
var hud = GameHUD.new()
add_child(hud)
hud.setup(experience_manager, skill_service)
```

## Setting Up the Leveling System

### Step 1: Initialize GameKit

In your game startup script:

```gdscript
var game_app = GameApplication.new()
game_app.add_child(game_app)

var base_stats = HeroStats.new(100, 10, 300)
var level_curve = preload("res://path/to/your_level_curve.tres")
game_app.get_game().initialize_leveling_system(base_stats, level_curve)
```

### Step 2: Configure Skills

Create skill resources programmatically or via scene:

```gdscript
var skill_extra_projectiles = SkillDefinition.new()
skill_extra_projectiles.skill_id = "extra_projectiles"
skill_extra_projectiles.name = "Extra Projectiles"
skill_extra_projectiles.description = "Fire two projectiles per attack"
skill_extra_projectiles.weight = 1.0
skill_extra_projectiles.max_stacks = 3
skill_extra_projectiles.stat_modifiers = {
    "damage": {"type": "mult", "value": 0.9}  # Slight damage nerf
}

game_app.get_game().get_skill_service().add_available_skill(skill_extra_projectiles)
```

### Step 3: Drop XP Orbs on Enemy Death

When enemies die, spawn XP orbs:

```gdscript
func _on_enemy_died(position: Vector2, xp_reward: int) -> void:
    var orb = preload("res://addons/universal_framework/game/xp_orb.tscn").instantiate()
    orb.global_position = position
    orb.set_xp_amount(xp_reward)
    orb.set_target(hero)  # Optional: auto-attract to hero
    add_child(orb)
    
    orb.collected.connect(func(amount):
        experience_manager.add_xp(amount)
    )
```

### Step 4: Listen for Level-Ups

Connect to signals to trigger the level-up panel:

```gdscript
var experience_manager = game_app.get_game().get_experience_manager()
experience_manager.level_up.connect(_on_hero_level_up)

func _on_hero_level_up(new_level: int, _remaining_xp: int) -> void:
    var skills = game_app.get_game().get_skill_service().select_random_skills(3)
    var panel = preload("res://addons/universal_framework/game/ui/level_up_panel.tscn").instantiate()
    add_child(panel)
    panel.display_level_up(new_level, skills)
    panel.skill_selected.connect(func(skill):
        game_app.get_game().get_skill_service().apply_skill(skill, new_level)
    )
```

## Adding New Skills (Data-Only)

To add a new skill without editing code:

1. **Create a SkillDefinition resource:**
   ```gdscript
   # In editor or code:
   var nova_burst = SkillDefinition.new()
   nova_burst.skill_id = "nova_burst"
   nova_burst.name = "Nova Burst"
   nova_burst.description = "Emit a shockwave dealing AOE damage"
   nova_burst.icon_path = "res://assets/skills/nova_burst.png"
   nova_burst.skill_type = "active"
   nova_burst.weight = 0.8  # Slightly rarer
   nova_burst.max_stacks = 1  # Can only select once
   nova_burst.stat_modifiers = {
       "damage": {"type": "mult", "value": 1.15}  # 15% boost
   }
   ```

2. **Register it with SkillService:**
   ```gdscript
   skill_service.add_available_skill(nova_burst)
   ```

3. **Optional: Add custom effect logic** (e.g., trigger on attack):
   - Assign `effect_script_path` to a script inheriting from `Node`
   - SkillService will instantiate it when the skill is applied
   - The script can hook into signals (e.g., `hero.attack_finished`)

## Customization & Extension

### Custom Stat Modifiers

To add a new stat not in HeroStats:

1. Extend HeroStats:
```gdscript
class_name CustomHeroStats
extends HeroStats

@export var lifesteal_percentage: float = 0.0
```

2. Update SkillDefinition's `apply_to_stats()` method to handle your custom stat.

3. Reference it in skill_modifiers:
```gdscript
skill_modifiers = {
    "lifesteal_percentage": {
        "type": "add",
        "value": 0.1  # 10% lifesteal
    }
}
```

### Custom Level Curves

Create a custom LevelCurve subclass for non-linear progression:

```gdscript
class_name FiboLevelCurve
extends LevelCurve

func _init() -> void:
    # Fibonacci XP requirements
    xp_thresholds = [100, 160, 260, 420, 680, ...]
```

### Effect Scripts

For complex skills (e.g., "summon minion"), assign an effect script:

```gdscript
# res://game/skills/summon_minion.gd
extends Node
class_name SummonMinionEffect

func _ready() -> void:
    var hero = get_parent()  # Assuming parent is hero
    hero.summon_minion()
```

## Performance Considerations

- **Skill pools** – Limit available skills; consider batching if >100 total
- **XP orb lifetime** – Set `despawn_time` to avoid memory leaks
- **Stat recalculation** – Cached in ExperienceManager; only recalc on level/skill change
- **Analytics logging** – Log to background thread if using heavy backends

## Debugging

### Enable Debug Output

```gdscript
if experience_manager.level_up.is_connected(_debug_level_up):
    experience_manager.level_up.disconnect(_debug_level_up)
experience_manager.level_up.connect(_debug_level_up)

func _debug_level_up(level: int, xp: int) -> void:
    print("Level up! New level: %d, Remaining XP: %d" % [level, xp])
```

### Check Skill Application

```gdscript
print("Applied skills:", skill_service.get_all_applied_skills())
print("Hero stats:", skill_service.get_hero_stats())
```

## Best Practices

1. **Validate stat modifiers** – Negative values can break gameplay (e.g., negative HP)
2. **Test stacking limits** – Ensure max_stacks prevents infinite scaling
3. **Weight balancing** – Use A/B testing to adjust skill weights
4. **UX flow** – Level-up pause should feel responsive; use quick-select hotkeys
5. **Save progression** – Persist `applied_skills` and `current_level` in cloud save

## FAQ

**Q: Can I adjust XP thresholds mid-game?**
A: Yes. Modify `level_curve.xp_thresholds` before or during gameplay. Re-calculation is automatic.

**Q: Can skills affect weapon behavior?**
A: Yes. Use custom effect scripts to swap weapon modules or modify projectile behavior.

**Q: How do I implement skill cooldowns?**
A: Add `cooldown: float` to SkillDefinition and check it before triggering active skills.

**Q: Can skills be "active" with manual triggers?**
A: Yes. Set `skill_type = "active"` and implement trigger logic in the effect script (e.g., press X to cast).

## Module Relationships

```
GameKit (core logic)
    ├─ ExperienceManager (XP tracking)
    │   └─ LevelCurve (progression curves)
    ├─ SkillService (skill application)
    │   └─ SkillDefinition[] (skill data)
    └─ HeroStats (hero attributes)

GameHUD (UI display)
    ├─ ExperienceManager (listen to signals)
    └─ SkillService (display applied skills)

LevelUpPanel (UI modal)
    └─ SkillService (select & apply)

XPOrb (collectible)
    └─ ExperienceManager (feed XP)
```

## Related Documentation

- **Framework Architecture:** `docs/framework/architecture.md`
- **GameKit API:** `docs/framework/modules/gamekit.md`
- **DataKit (Save/Load):** `docs/framework/modules/datakit.md`
- **LogKit (Analytics):** `docs/framework/modules/logkit.md`

---

**Last Updated:** 2024
**Version:** 1.0
