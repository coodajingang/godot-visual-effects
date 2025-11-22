extends Resource
class_name EquipmentItem

enum Rarity {
	COMMON,
	UNCOMMON,
	RARE,
	EPIC,
	LEGENDARY
}

enum Slot {
	WEAPON,
	ARMOR,
	TRINKET
}

@export var item_id: String = ""
@export var display_name: String = ""
@export_multiline var description: String = ""
@export var slot: Slot = Slot.WEAPON
@export var rarity: Rarity = Rarity.COMMON
@export var icon: Texture2D
@export var max_level: int = 10

@export_group("Base Stats")
@export var base_damage: float = 0.0
@export var base_defense: float = 0.0
@export var base_crit_chance: float = 0.0
@export var base_crit_damage: float = 0.0
@export var base_attack_speed: float = 0.0
@export var base_movement_speed: float = 0.0
@export var base_health: float = 0.0
@export var base_health_regen: float = 0.0

@export_group("Upgrade Curve")
@export var damage_per_level: float = 0.0
@export var defense_per_level: float = 0.0
@export var crit_chance_per_level: float = 0.0
@export var crit_damage_per_level: float = 0.0
@export var attack_speed_per_level: float = 0.0
@export var movement_speed_per_level: float = 0.0
@export var health_per_level: float = 0.0
@export var health_regen_per_level: float = 0.0

@export_group("Upgrade Costs")
@export var base_upgrade_cost: int = 100
@export var cost_per_level_multiplier: float = 1.5

@export_group("Triggered Effects")
@export var has_special_effect: bool = false
@export var effect_id: String = ""
@export var effect_chance: float = 0.0
@export var effect_description: String = ""

var current_level: int = 1

func get_stat(stat_name: String, level: int = -1) -> float:
	var lvl: int = level if level > 0 else current_level
	var level_bonus: int = lvl - 1
	
	match stat_name:
		"damage":
			return base_damage + damage_per_level * level_bonus
		"defense":
			return base_defense + defense_per_level * level_bonus
		"crit_chance":
			return base_crit_chance + crit_chance_per_level * level_bonus
		"crit_damage":
			return base_crit_damage + crit_damage_per_level * level_bonus
		"attack_speed":
			return base_attack_speed + attack_speed_per_level * level_bonus
		"movement_speed":
			return base_movement_speed + movement_speed_per_level * level_bonus
		"health":
			return base_health + health_per_level * level_bonus
		"health_regen":
			return base_health_regen + health_regen_per_level * level_bonus
	
	return 0.0

func get_all_stats(level: int = -1) -> Dictionary:
	var lvl: int = level if level > 0 else current_level
	return {
		"damage": get_stat("damage", lvl),
		"defense": get_stat("defense", lvl),
		"crit_chance": get_stat("crit_chance", lvl),
		"crit_damage": get_stat("crit_damage", lvl),
		"attack_speed": get_stat("attack_speed", lvl),
		"movement_speed": get_stat("movement_speed", lvl),
		"health": get_stat("health", lvl),
		"health_regen": get_stat("health_regen", lvl)
	}

func get_upgrade_cost(target_level: int = -1) -> int:
	var lvl: int = target_level if target_level > 0 else current_level + 1
	if lvl > max_level:
		return -1
	return int(base_upgrade_cost * pow(cost_per_level_multiplier, lvl - 2))

func can_upgrade() -> bool:
	return current_level < max_level

func level_up() -> bool:
	if can_upgrade():
		current_level += 1
		return true
	return false

func get_rarity_name() -> String:
	match rarity:
		Rarity.COMMON:
			return "Common"
		Rarity.UNCOMMON:
			return "Uncommon"
		Rarity.RARE:
			return "Rare"
		Rarity.EPIC:
			return "Epic"
		Rarity.LEGENDARY:
			return "Legendary"
	return "Unknown"

func get_rarity_color() -> Color:
	match rarity:
		Rarity.COMMON:
			return Color.WHITE
		Rarity.UNCOMMON:
			return Color.GREEN
		Rarity.RARE:
			return Color.DODGER_BLUE
		Rarity.EPIC:
			return Color.PURPLE
		Rarity.LEGENDARY:
			return Color.ORANGE
	return Color.WHITE

func get_slot_name() -> String:
	match slot:
		Slot.WEAPON:
			return "Weapon"
		Slot.ARMOR:
			return "Armor"
		Slot.TRINKET:
			return "Trinket"
	return "Unknown"

func serialize() -> Dictionary:
	return {
		"item_id": item_id,
		"current_level": current_level
	}

static func deserialize(data: Dictionary, item_resource: EquipmentItem) -> EquipmentItem:
	if item_resource:
		item_resource.current_level = data.get("current_level", 1)
	return item_resource
