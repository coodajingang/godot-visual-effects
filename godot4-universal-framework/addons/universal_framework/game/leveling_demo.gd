extends Node
class_name LevelingDemo

var experience_manager: ExperienceManager
var skill_service: SkillService
var game_hud: GameHUD

func _ready() -> void:
	_initialize_leveling_system()
	_setup_ui()
	_setup_demo_inputs()

func _initialize_leveling_system() -> void:
	var base_stats = HeroStats.new(
		p_max_hp = 100.0,
		p_damage = 10.0,
		p_move_speed = 300.0
	)
	
	var level_curve = LevelCurve.new()
	
	experience_manager = ExperienceManager.new(base_stats, level_curve)
	add_child(experience_manager)
	
	skill_service = SkillService.new(base_stats)
	add_child(skill_service)
	
	var all_skills = SkillExamples.create_all_example_skills()
	for skill in all_skills:
		skill_service.add_available_skill(skill)
	
	experience_manager.level_up.connect(_on_hero_level_up)

func _setup_ui() -> void:
	game_hud = preload("res://addons/universal_framework/game/ui/game_hud.tscn").instantiate()
	add_child(game_hud)
	game_hud.setup(experience_manager, skill_service)

func _setup_demo_inputs() -> void:
	if not InputMap.has_action("demo_add_xp"):
		InputMap.add_action("demo_add_xp")
		InputMap.action_add_key_event("demo_add_xp", InputEventKey.new())
		InputMap.action_get_events("demo_add_xp")[0].keycode = KEY_E
	
	if not InputMap.has_action("demo_add_large_xp"):
		InputMap.add_action("demo_add_large_xp")
		InputMap.action_add_key_event("demo_add_large_xp", InputEventKey.new())
		InputMap.action_get_events("demo_add_large_xp")[0].keycode = KEY_R

func _process(_delta: float) -> void:
	if Input.is_action_just_pressed("demo_add_xp"):
		experience_manager.add_xp(50)
		print("Added 50 XP. Level: %d, XP: %d/%d" % [
			experience_manager.get_current_level(),
			experience_manager.get_current_xp(),
			experience_manager.get_xp_needed_for_next_level()
		])
	
	if Input.is_action_just_pressed("demo_add_large_xp"):
		experience_manager.add_xp(500)
		print("Added 500 XP. Level: %d, XP: %d/%d" % [
			experience_manager.get_current_level(),
			experience_manager.get_current_xp(),
			experience_manager.get_xp_needed_for_next_level()
		])

func _on_hero_level_up(level: int, _remaining_xp: int) -> void:
	print("Hero leveled up to level %d!" % level)
	var available_skills = skill_service.select_random_skills(3)
	_show_level_up_panel(level, available_skills)

func _show_level_up_panel(level: int, skills: Array[SkillDefinition]) -> void:
	var panel = preload("res://addons/universal_framework/game/ui/level_up_panel.tscn").instantiate()
	add_child(panel)
	panel.display_level_up(level, skills)
	panel.skill_selected.connect(func(skill: SkillDefinition) -> void:
		skill_service.apply_skill(skill, level)
		var stats = skill_service.get_hero_stats()
		print("Applied skill: %s" % skill.name)
		print("New stats - HP: %.1f, DMG: %.1f, SPD: %.1f" % [stats.max_hp, stats.damage, stats.move_speed])
	)
