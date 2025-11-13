extends Resource
class_name AdPlacementConfig

@export var placement_id := ""
@export var display_name := ""
@export_enum("interstitial", "rewarded", "banner") var placement_type := "interstitial"
@export var provider := "Simulated Network"
@export_range(0.0, 10.0, 0.1, "or_greater", "suffix:minutes") var cooldown_minutes := 0.0
@export var reward_currency := ""
@export var reward_amount := 0

func get_display_label() -> String:
	if display_name.is_empty():
		return placement_id.capitalize()
	return display_name

func is_rewarded() -> bool:
	return placement_type == "rewarded"
