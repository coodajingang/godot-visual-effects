extends Control
class_name EquipmentPanel

signal panel_closed

@onready var weapon_slot: Panel = $MainContainer/EquipmentSlots/WeaponSlot
@onready var armor_slot: Panel = $MainContainer/EquipmentSlots/ArmorSlot
@onready var trinket_slot: Panel = $MainContainer/EquipmentSlots/TrinketSlot
@onready var inventory_grid: GridContainer = $MainContainer/InventorySection/ScrollContainer/InventoryGrid
@onready var stats_panel: Panel = $MainContainer/StatsPanel
@onready var stats_label: Label = $MainContainer/StatsPanel/StatsLabel
@onready var currency_label: Label = $MainContainer/TopBar/CurrencyLabel
@onready var close_button: Button = $MainContainer/TopBar/CloseButton
@onready var comparison_panel: Panel = $ComparisonPanel
@onready var comparison_label: Label = $ComparisonPanel/ComparisonLabel
@onready var strengthen_panel: Panel = $StrengthenPanel
@onready var strengthen_button: Button = $StrengthenPanel/StrengthenButton
@onready var strengthen_label: Label = $StrengthenPanel/InfoLabel

var equipment_manager: EquipmentManager
var selected_item: EquipmentItem = null
var selected_slot: EquipmentItem.Slot = EquipmentItem.Slot.WEAPON

const ITEM_BUTTON_SCENE := preload("res://ui/equipment_item_button.tscn")

func _ready() -> void:
	hide()
	close_button.pressed.connect(_on_close_pressed)
	comparison_panel.hide()
	strengthen_panel.hide()
	
	if GameApp.has_method("get_equipment_manager"):
		equipment_manager = GameApp.get_equipment_manager()
	
	if equipment_manager:
		equipment_manager.equipment_changed.connect(_on_equipment_changed)
		equipment_manager.stats_updated.connect(_on_stats_updated)
		equipment_manager.inventory_changed.connect(_on_inventory_changed)
		equipment_manager.equipment_upgraded.connect(_on_equipment_upgraded)
	
	_setup_equipment_slots()

func open_panel() -> void:
	show()
	_refresh_all()

func _on_close_pressed() -> void:
	hide()
	panel_closed.emit()

func _setup_equipment_slots() -> void:
	_setup_slot(weapon_slot, EquipmentItem.Slot.WEAPON, "Weapon")
	_setup_slot(armor_slot, EquipmentItem.Slot.ARMOR, "Armor")
	_setup_slot(trinket_slot, EquipmentItem.Slot.TRINKET, "Trinket")

func _setup_slot(slot_panel: Panel, slot_type: EquipmentItem.Slot, slot_name: String) -> void:
	if not slot_panel:
		return
	
	var label: Label = slot_panel.get_node_or_null("Label")
	if label:
		label.text = slot_name
	
	var button: Button = slot_panel.get_node_or_null("Button")
	if button:
		button.pressed.connect(func(): _on_slot_clicked(slot_type))

func _on_slot_clicked(slot_type: EquipmentItem.Slot) -> void:
	selected_slot = slot_type
	var item: EquipmentItem = equipment_manager.get_equipped_item(slot_type)
	
	if item:
		_show_item_details(item, true)
	else:
		_hide_item_details()

func _refresh_all() -> void:
	_update_equipment_slots()
	_update_stats_display()
	_update_inventory()
	_update_currency()

func _update_equipment_slots() -> void:
	if not equipment_manager:
		return
	
	_update_slot_display(weapon_slot, EquipmentItem.Slot.WEAPON)
	_update_slot_display(armor_slot, EquipmentItem.Slot.ARMOR)
	_update_slot_display(trinket_slot, EquipmentItem.Slot.TRINKET)

func _update_slot_display(slot_panel: Panel, slot_type: EquipmentItem.Slot) -> void:
	if not slot_panel or not equipment_manager:
		return
	
	var item: EquipmentItem = equipment_manager.get_equipped_item(slot_type)
	var icon: TextureRect = slot_panel.get_node_or_null("Icon")
	var level_label: Label = slot_panel.get_node_or_null("LevelLabel")
	
	if item:
		if icon:
			icon.texture = item.icon if item.icon else null
			icon.modulate = item.get_rarity_color()
		if level_label:
			level_label.text = "Lv. %d" % item.current_level
			level_label.show()
		slot_panel.modulate = Color.WHITE
	else:
		if icon:
			icon.texture = null
		if level_label:
			level_label.hide()
		slot_panel.modulate = Color(0.5, 0.5, 0.5, 1.0)

func _update_stats_display() -> void:
	if not equipment_manager or not stats_label:
		return
	
	var stats: Dictionary = equipment_manager.get_total_stats()
	var text := "Total Stats:\n"
	
	for stat_name in stats:
		var value: float = stats[stat_name]
		if value > 0.001 or value < -0.001:
			text += "%s: %.1f\n" % [stat_name.capitalize(), value]
	
	stats_label.text = text

func _update_currency() -> void:
	if not equipment_manager or not currency_label:
		return
	
	currency_label.text = "Currency: %d" % equipment_manager.get_currency()

func _update_inventory() -> void:
	if not equipment_manager or not inventory_grid:
		return
	
	for child in inventory_grid.get_children():
		child.queue_free()
	
	var inventory: Array[EquipmentItem] = equipment_manager.get_inventory()
	
	for item in inventory:
		if ITEM_BUTTON_SCENE:
			var button_node: Node = ITEM_BUTTON_SCENE.instantiate()
			inventory_grid.add_child(button_node)
			
			if button_node.has_method("set_item"):
				button_node.set_item(item)
			
			if button_node.has_method("connect_signals"):
				button_node.connect_signals(
					_on_inventory_item_clicked.bind(item),
					_on_inventory_item_equipped.bind(item)
				)

func _on_inventory_item_clicked(item: EquipmentItem) -> void:
	selected_item = item
	_show_item_details(item, false)

func _on_inventory_item_equipped(item: EquipmentItem) -> void:
	if equipment_manager:
		equipment_manager.equip_item(item)
	_hide_item_details()

func _show_item_details(item: EquipmentItem, is_equipped: bool) -> void:
	if not item:
		return
	
	selected_item = item
	
	if is_equipped:
		_show_strengthen_panel(item)
	else:
		_show_comparison_panel(item)

func _show_comparison_panel(item: EquipmentItem) -> void:
	if not comparison_panel or not comparison_label or not equipment_manager:
		return
	
	var equipped_item: EquipmentItem = equipment_manager.get_equipped_item(item.slot)
	
	var text := "%s\n" % item.display_name
	text += "Level: %d\n" % item.current_level
	text += "Rarity: %s\n\n" % item.get_rarity_name()
	
	if equipped_item:
		var comparison: Dictionary = equipment_manager.compare_stats(equipped_item, item)
		text += "Stat Comparison:\n"
		
		for stat_name in comparison:
			var diff: float = comparison[stat_name]
			if abs(diff) > 0.001:
				var sign := "+" if diff > 0 else ""
				var color := "green" if diff > 0 else "red"
				text += "[color=%s]%s: %s%.1f[/color]\n" % [color, stat_name.capitalize(), sign, diff]
	else:
		text += "No item equipped in this slot\n"
		var stats: Dictionary = item.get_all_stats()
		for stat_name in stats:
			var value: float = stats[stat_name]
			if value > 0.001:
				text += "%s: +%.1f\n" % [stat_name.capitalize(), value]
	
	comparison_label.text = text
	comparison_panel.show()
	strengthen_panel.hide()

func _show_strengthen_panel(item: EquipmentItem) -> void:
	if not strengthen_panel or not strengthen_label or not strengthen_button:
		return
	
	var text := "%s\n" % item.display_name
	text += "Level: %d / %d\n" % [item.current_level, item.max_level]
	text += "Rarity: %s\n\n" % item.get_rarity_name()
	
	if item.can_upgrade():
		var cost: int = item.get_upgrade_cost()
		text += "Upgrade Cost: %d\n" % cost
		text += "Next Level Stats:\n"
		
		var current_stats: Dictionary = item.get_all_stats(item.current_level)
		var next_stats: Dictionary = item.get_all_stats(item.current_level + 1)
		
		for stat_name in current_stats:
			var diff: float = next_stats[stat_name] - current_stats[stat_name]
			if abs(diff) > 0.001:
				text += "%s: +%.1f\n" % [stat_name.capitalize(), diff]
		
		strengthen_button.disabled = equipment_manager.get_currency() < cost
		strengthen_button.text = "Strengthen"
	else:
		text += "Max Level Reached"
		strengthen_button.disabled = true
		strengthen_button.text = "Max Level"
	
	strengthen_label.text = text
	strengthen_panel.show()
	comparison_panel.hide()
	
	if not strengthen_button.pressed.is_connected(_on_strengthen_pressed):
		strengthen_button.pressed.connect(_on_strengthen_pressed)

func _on_strengthen_pressed() -> void:
	if not selected_item or not equipment_manager:
		return
	
	if equipment_manager.strengthen_item(selected_item):
		_show_strengthen_panel(selected_item)
		_update_stats_display()
		_update_currency()
		_update_equipment_slots()

func _hide_item_details() -> void:
	if comparison_panel:
		comparison_panel.hide()
	if strengthen_panel:
		strengthen_panel.hide()
	selected_item = null

func _on_equipment_changed(_slot: EquipmentItem.Slot, _item: EquipmentItem) -> void:
	_update_equipment_slots()
	_update_inventory()

func _on_stats_updated(_stats: Dictionary) -> void:
	_update_stats_display()

func _on_inventory_changed(_inventory: Array[EquipmentItem]) -> void:
	_update_inventory()

func _on_equipment_upgraded(_item: EquipmentItem, _new_level: int) -> void:
	_update_equipment_slots()
