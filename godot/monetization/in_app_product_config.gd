extends Resource
class_name InAppProductConfig

@export var product_id := ""
@export var display_name := ""
@export_multiline var description := ""
@export var price := 0.99
@export var currency := "USD"
@export var consumable := true
@export var reward_currency := ""
@export var reward_amount := 0
@export var metadata := {}

func get_display_label() -> String:
	if display_name.is_empty():
		return product_id.capitalize()
	return display_name
