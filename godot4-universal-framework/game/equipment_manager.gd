extends Node
class_name EquipmentManager

signal equipment_changed(slot: EquipmentItem.Slot, item: EquipmentItem)
signal equipment_upgraded(item: EquipmentItem, new_level: int)
signal stats_updated(total_stats: Dictionary)
signal inventory_changed(inventory: Array[EquipmentItem])

var equipped_items: Dictionary = {
	EquipmentItem.Slot.WEAPON: null,
	EquipmentItem.Slot.ARMOR: null,
	EquipmentItem.Slot.TRINKET: null
}

var inventory: Array[EquipmentItem] = []
var item_database: Dictionary = {}
var currency: int = 0

func _ready() -> void:
	_load_item_database()
	_load_default_loadout()

func _load_item_database() -> void:
	var weapon_dir := "res://data/equipment/weapons/"
	var armor_dir := "res://data/equipment/armor/"
	var trinket_dir := "res://data/equipment/trinkets/"
	
	_load_items_from_directory(weapon_dir)
	_load_items_from_directory(armor_dir)
	_load_items_from_directory(trinket_dir)
	
	DebugConsole.info("Equipment database loaded", {"count": item_database.size()})

func _load_items_from_directory(dir_path: String) -> void:
	if not DirAccess.dir_exists_absolute(dir_path):
		return
	
	var dir := DirAccess.open(dir_path)
	if dir:
		dir.list_dir_begin()
		var file_name := dir.get_next()
		while file_name != "":
			if not dir.current_is_dir() and file_name.ends_with(".tres"):
				var full_path := dir_path + file_name
				var item: EquipmentItem = load(full_path)
				if item:
					item_database[item.item_id] = item
			file_name = dir.get_next()
		dir.list_dir_end()

func _load_default_loadout() -> void:
	if item_database.has("starter_sword"):
		equip_item(item_database["starter_sword"].duplicate())
	if item_database.has("starter_armor"):
		equip_item(item_database["starter_armor"].duplicate())

func get_item_by_id(item_id: String) -> EquipmentItem:
	if item_database.has(item_id):
		return item_database[item_id].duplicate()
	return null

func equip_item(item: EquipmentItem) -> bool:
	if not item:
		return false
	
	var old_item: EquipmentItem = equipped_items[item.slot]
	
	if old_item:
		add_to_inventory(old_item)
	
	equipped_items[item.slot] = item
	
	if item in inventory:
		inventory.erase(item)
	
	equipment_changed.emit(item.slot, item)
	_update_total_stats()
	inventory_changed.emit(inventory)
	
	DebugConsole.info("Equipment changed", {
		"slot": item.get_slot_name(),
		"item": item.display_name
	})
	
	return true

func unequip_item(slot: EquipmentItem.Slot) -> bool:
	var item: EquipmentItem = equipped_items[slot]
	if not item:
		return false
	
	equipped_items[slot] = null
	add_to_inventory(item)
	
	equipment_changed.emit(slot, null)
	_update_total_stats()
	inventory_changed.emit(inventory)
	
	return true

func get_equipped_item(slot: EquipmentItem.Slot) -> EquipmentItem:
	return equipped_items.get(slot)

func add_to_inventory(item: EquipmentItem) -> void:
	if item:
		inventory.append(item)
		inventory_changed.emit(inventory)
		DebugConsole.info("Item added to inventory", {"item": item.display_name})

func remove_from_inventory(item: EquipmentItem) -> void:
	if item in inventory:
		inventory.erase(item)
		inventory_changed.emit(inventory)

func get_total_stats() -> Dictionary:
	var stats := {
		"damage": 0.0,
		"defense": 0.0,
		"crit_chance": 0.0,
		"crit_damage": 0.0,
		"attack_speed": 0.0,
		"movement_speed": 0.0,
		"health": 0.0,
		"health_regen": 0.0
	}
	
	for slot in equipped_items:
		var item: EquipmentItem = equipped_items[slot]
		if item:
			var item_stats := item.get_all_stats()
			for stat_name in item_stats:
				stats[stat_name] += item_stats[stat_name]
	
	return stats

func _update_total_stats() -> void:
	var stats := get_total_stats()
	stats_updated.emit(stats)
	GameApp.event_bus.publish("equipment.stats_updated", stats)

func strengthen_item(item: EquipmentItem, material_cost: int = -1) -> bool:
	if not item:
		return false
	
	if not item.can_upgrade():
		DebugConsole.warn("Item is already at max level", {"item": item.display_name})
		return false
	
	var cost: int = material_cost if material_cost >= 0 else item.get_upgrade_cost()
	
	if currency < cost:
		DebugConsole.warn("Insufficient currency", {
			"required": cost,
			"available": currency
		})
		return false
	
	currency -= cost
	item.level_up()
	
	equipment_upgraded.emit(item, item.current_level)
	_update_total_stats()
	
	DebugConsole.info("Item strengthened", {
		"item": item.display_name,
		"level": item.current_level,
		"cost": cost
	})
	
	return true

func add_currency(amount: int) -> void:
	currency += amount
	GameApp.event_bus.publish("equipment.currency_changed", {"currency": currency})

func get_currency() -> int:
	return currency

func get_inventory() -> Array[EquipmentItem]:
	return inventory

func compare_stats(item_a: EquipmentItem, item_b: EquipmentItem) -> Dictionary:
	var comparison := {}
	
	if not item_a or not item_b:
		return comparison
	
	var stats_a := item_a.get_all_stats()
	var stats_b := item_b.get_all_stats()
	
	for stat_name in stats_a:
		comparison[stat_name] = stats_b[stat_name] - stats_a[stat_name]
	
	return comparison

func serialize() -> Dictionary:
	var equipped_data := {}
	for slot in equipped_items:
		var item: EquipmentItem = equipped_items[slot]
		if item:
			equipped_data[slot] = item.serialize()
	
	var inventory_data := []
	for item in inventory:
		inventory_data.append(item.serialize())
	
	return {
		"equipped": equipped_data,
		"inventory": inventory_data,
		"currency": currency
	}

func deserialize(data: Dictionary) -> void:
	if data.has("equipped"):
		for slot in data["equipped"]:
			var item_data: Dictionary = data["equipped"][slot]
			var item_id: String = item_data.get("item_id", "")
			var item: EquipmentItem = get_item_by_id(item_id)
			if item:
				EquipmentItem.deserialize(item_data, item)
				equipped_items[slot] = item
	
	if data.has("inventory"):
		inventory.clear()
		for item_data in data["inventory"]:
			var item_id: String = item_data.get("item_id", "")
			var item: EquipmentItem = get_item_by_id(item_id)
			if item:
				EquipmentItem.deserialize(item_data, item)
				inventory.append(item)
	
	if data.has("currency"):
		currency = data.get("currency", 0)
	
	_update_total_stats()
	inventory_changed.emit(inventory)
