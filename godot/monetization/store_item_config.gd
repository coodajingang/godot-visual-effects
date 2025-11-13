extends Resource
class_name StoreItemConfig

@export var item_id := ""
@export var display_name := ""
@export_multiline var description := ""
@export var price := 0
@export var currency := "Soft Currency"
@export var icon: Texture2D
@export var metadata := {}

func get_display_label() -> String:
	if display_name.is_empty():
		return item_id.capitalize()
	return display_name
