extends Node2D

@onready var spawn_button: Button = $UI/SpawnButton
@onready var add_currency_button: Button = $UI/AddCurrencyButton
@onready var test_battle_button: Button = $UI/TestBattleButton
@onready var status_label: Label = $UI/StatusLabel

var equipment_manager: EquipmentManager

func _ready() -> void:
	equipment_manager = GameApp.get_equipment_manager()
	
	spawn_button.pressed.connect(_on_spawn_equipment_pressed)
	add_currency_button.pressed.connect(_on_add_currency_pressed)
	test_battle_button.pressed.connect(_on_test_battle_pressed)
	
	equipment_manager.inventory_changed.connect(_on_inventory_changed)
	equipment_manager.equipment_changed.connect(_on_equipment_changed)
	
	_update_status()

func _on_spawn_equipment_pressed() -> void:
	var drop_scene := preload("res://game/equipment_drop.tscn")
	var drop := drop_scene.instantiate()
	
	var items := ["starter_sword", "flaming_blade", "dragon_scale_armor", "lucky_coin"]
	var random_item_id := items[randi() % items.size()]
	var item := equipment_manager.get_item_by_id(random_item_id)
	
	if item:
		item.current_level = randi_range(1, 5)
		drop.set_equipment_item(item)
		drop.global_position = Vector2(
			randf_range(200, 600),
			randf_range(200, 400)
		)
		add_child(drop)
		status_label.text = "Spawned: %s (Lv.%d)" % [item.display_name, item.current_level]

func _on_add_currency_pressed() -> void:
	equipment_manager.add_currency(500)
	status_label.text = "Added 500 currency. Total: %d" % equipment_manager.get_currency()

func _on_test_battle_pressed() -> void:
	var loot_table: LootTable = load("res://data/equipment/loot_table_elite.tres")
	if loot_table:
		var spawn_pos := Vector2(400, 300)
		BattleLootHandler.spawn_loot_drops(loot_table, spawn_pos, self, 0.2)
		equipment_manager.add_currency(250)
		status_label.text = "Battle complete! Check for loot drops."

func _on_inventory_changed(inventory: Array[EquipmentItem]) -> void:
	_update_status()

func _on_equipment_changed(slot: EquipmentItem.Slot, item: EquipmentItem) -> void:
	if item:
		status_label.text = "Equipped: %s in %s slot" % [item.display_name, item.get_slot_name()]
	else:
		status_label.text = "Unequipped from slot"
	_update_status()

func _update_status() -> void:
	var stats := equipment_manager.get_total_stats()
	var info := "Currency: %d | Items: %d\n" % [
		equipment_manager.get_currency(),
		equipment_manager.get_inventory().size()
	]
	
	info += "Stats - "
	for stat_name in ["damage", "defense", "health"]:
		var value: float = stats.get(stat_name, 0.0)
		if value > 0.001:
			info += "%s:%.0f " % [stat_name.capitalize(), value]
	
	status_label.text = info

func _input(event: InputEvent) -> void:
	if event.is_action_pressed("ui_accept"):
		_on_spawn_equipment_pressed()
