extends Panel
class_name EquipmentItemButton

@onready var button: Button = $Button
@onready var icon: TextureRect = $Button/Icon
@onready var level_label: Label = $Button/LevelLabel
@onready var name_label: Label = $NameLabel

var equipment_item: EquipmentItem

func _ready() -> void:
	if button:
		button.mouse_entered.connect(_on_mouse_entered)
		button.mouse_exited.connect(_on_mouse_exited)

func set_item(item: EquipmentItem) -> void:
	equipment_item = item
	_update_display()

func _update_display() -> void:
	if not equipment_item:
		return
	
	if icon:
		icon.texture = equipment_item.icon if equipment_item.icon else null
		icon.modulate = equipment_item.get_rarity_color()
	
	if level_label:
		level_label.text = "Lv.%d" % equipment_item.current_level
	
	if name_label:
		name_label.text = equipment_item.display_name
	
	modulate = equipment_item.get_rarity_color().lightened(0.7)

func connect_signals(on_click: Callable, on_equip: Callable) -> void:
	if button:
		if button.pressed.is_connected(on_click):
			button.pressed.disconnect(on_click)
		button.pressed.connect(on_click)
		
		if button.gui_input.is_connected(_on_gui_input):
			button.gui_input.disconnect(_on_gui_input)
		button.gui_input.connect(func(event: InputEvent):
			if event is InputEventMouseButton and event.double_click and event.pressed:
				on_equip.call()
		)

func _on_mouse_entered() -> void:
	modulate = equipment_item.get_rarity_color() if equipment_item else Color.WHITE

func _on_mouse_exited() -> void:
	if equipment_item:
		modulate = equipment_item.get_rarity_color().lightened(0.7)
	else:
		modulate = Color.WHITE

func _on_gui_input(event: InputEvent) -> void:
	pass
