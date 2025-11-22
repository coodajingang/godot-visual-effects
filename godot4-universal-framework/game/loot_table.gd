extends Resource
class_name LootTable

@export var table_id: String = ""
@export var loot_entries: Array[Dictionary] = []

func add_entry(item_id: String, weight: float, min_level: int = 1, max_level: int = 1) -> void:
	loot_entries.append({
		"item_id": item_id,
		"weight": weight,
		"min_level": min_level,
		"max_level": max_level
	})

func roll_loot(luck_modifier: float = 0.0) -> Array[Dictionary]:
	var drops: Array[Dictionary] = []
	
	for entry in loot_entries:
		var drop_chance: float = entry.get("weight", 0.0) + luck_modifier
		if randf() < drop_chance:
			var level: int = randi_range(
				entry.get("min_level", 1),
				entry.get("max_level", 1)
			)
			drops.append({
				"item_id": entry.get("item_id", ""),
				"level": level
			})
	
	return drops

func get_guaranteed_drop() -> Dictionary:
	if loot_entries.is_empty():
		return {}
	
	var total_weight: float = 0.0
	for entry in loot_entries:
		total_weight += entry.get("weight", 0.0)
	
	var roll: float = randf() * total_weight
	var current_weight: float = 0.0
	
	for entry in loot_entries:
		current_weight += entry.get("weight", 0.0)
		if roll <= current_weight:
			var level: int = randi_range(
				entry.get("min_level", 1),
				entry.get("max_level", 1)
			)
			return {
				"item_id": entry.get("item_id", ""),
				"level": level
			}
	
	return {}
