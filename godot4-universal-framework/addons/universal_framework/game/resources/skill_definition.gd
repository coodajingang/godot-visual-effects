extends Resource
class_name SkillDefinition

@export var skill_id: String = ""
@export var name: String = ""
@export var description: String = ""
@export var icon_path: String = ""
@export var skill_type: String = "passive"
@export var weight: float = 1.0

@export var stat_modifiers: Dictionary = {}
@export var max_stacks: int = -1
@export var effect_script_path: String = ""

func get_display_name() -> String:
	return name

func get_display_description() -> String:
	return description

func get_stat_preview() -> String:
	var preview_text = ""
	for stat_name in stat_modifiers:
		var modifier = stat_modifiers[stat_name]
		if modifier is Dictionary:
			match modifier.get("type", "add"):
				"add":
					preview_text += "%s: +%s\n" % [stat_name, str(modifier.get("value", 0))]
				"mult":
					preview_text += "%s: x%.2f\n" % [stat_name, modifier.get("value", 1.0)]
				"percent":
					preview_text += "%s: +%d%%\n" % [stat_name, int(modifier.get("value", 0) * 100)]
	return preview_text.trim_suffix("\n")

func apply_to_stats(stats: HeroStats) -> void:
	for stat_name in stat_modifiers:
		var modifier = stat_modifiers[stat_name]
		if not modifier is Dictionary:
			continue
			
		var stat_field = stat_name.to_lower()
		if not stats.has_meta("_%s" % stat_field):
			match stat_field:
				"max_hp":
					_apply_modifier(stats, "max_hp", modifier)
				"damage":
					_apply_modifier(stats, "damage", modifier)
				"move_speed":
					_apply_modifier(stats, "move_speed", modifier)
				"attack_speed":
					_apply_modifier(stats, "attack_speed", modifier)
				"critical_chance":
					_apply_modifier(stats, "critical_chance", modifier)
				"critical_multiplier":
					_apply_modifier(stats, "critical_multiplier", modifier)
				"armor":
					_apply_modifier(stats, "armor", modifier)
				"dodge_chance":
					_apply_modifier(stats, "dodge_chance", modifier)

func _apply_modifier(stats: HeroStats, field: String, modifier: Dictionary) -> void:
	var current_value = stats.get(field)
	var mod_type = modifier.get("type", "add")
	var mod_value = modifier.get("value", 0)
	
	match mod_type:
		"add":
			stats.set(field, current_value + mod_value)
		"mult":
			stats.set(field, current_value * mod_value)
		"percent":
			stats.set(field, current_value + current_value * mod_value)
