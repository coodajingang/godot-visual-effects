extends Node
class_name BattleLootHandler

static func spawn_loot_drops(loot_table: LootTable, spawn_position: Vector2, spawn_scene: Node2D, luck_modifier: float = 0.0) -> void:
	if not loot_table or not spawn_scene:
		return
	
	var drops: Array[Dictionary] = loot_table.roll_loot(luck_modifier)
	var equipment_manager: EquipmentManager = GameApp.get_equipment_manager()
	
	if not equipment_manager:
		return
	
	var drop_scene: PackedScene = load("res://game/equipment_drop.tscn")
	if not drop_scene:
		return
	
	var angle_offset: float = 0.0
	var angle_increment: float = TAU / max(drops.size(), 1)
	var spread_radius: float = 50.0
	
	for drop_data in drops:
		var item_id: String = drop_data.get("item_id", "")
		var level: int = drop_data.get("level", 1)
		
		var item: EquipmentItem = equipment_manager.get_item_by_id(item_id)
		if item:
			item.current_level = level
			
			var drop_instance: Node2D = drop_scene.instantiate()
			spawn_scene.add_child(drop_instance)
			
			var offset := Vector2(
				cos(angle_offset) * spread_radius,
				sin(angle_offset) * spread_radius
			)
			drop_instance.global_position = spawn_position + offset
			
			if drop_instance.has_method("set_equipment_item"):
				drop_instance.set_equipment_item(item)
			
			angle_offset += angle_increment

static func spawn_currency_drop(amount: int, spawn_position: Vector2, spawn_scene: Node2D) -> void:
	var equipment_manager: EquipmentManager = GameApp.get_equipment_manager()
	if equipment_manager:
		equipment_manager.add_currency(amount)
		DebugConsole.info("Currency dropped", {"amount": amount})

static func process_battle_rewards(battle_result: Dictionary) -> void:
	var equipment_manager: EquipmentManager = GameApp.get_equipment_manager()
	if not equipment_manager:
		return
	
	if battle_result.has("currency"):
		var currency: int = battle_result.get("currency", 0)
		equipment_manager.add_currency(currency)
	
	if battle_result.has("guaranteed_items"):
		var items: Array = battle_result.get("guaranteed_items", [])
		for item_data in items:
			if item_data is Dictionary:
				var item_id: String = item_data.get("item_id", "")
				var level: int = item_data.get("level", 1)
				var item: EquipmentItem = equipment_manager.get_item_by_id(item_id)
				if item:
					item.current_level = level
					equipment_manager.add_to_inventory(item)
