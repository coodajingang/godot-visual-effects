extends Node
class_name SkillService

signal skill_applied(skill: SkillDefinition, hero_stats: HeroStats)
signal skill_selection_logged(skill_id: String, level: int)

var _applied_skills: Dictionary = {}
var _hero_stats: HeroStats
var _available_skills: Array[SkillDefinition] = []
var _log_kit = null

func _init(hero_stats: HeroStats = null) -> void:
	_hero_stats = hero_stats or HeroStats.new()

func setup(game_app: GameApplication) -> void:
	_log_kit = game_app.get_log()

func add_available_skill(skill: SkillDefinition) -> void:
	if skill not in _available_skills:
		_available_skills.append(skill)

func set_available_skills(skills: Array[SkillDefinition]) -> void:
	_available_skills = skills

func get_available_skills() -> Array[SkillDefinition]:
	return _available_skills

func select_random_skills(count: int = 3) -> Array[SkillDefinition]:
	if _available_skills.is_empty():
		return []
	
	var weighted_skills: Array[SkillDefinition] = []
	
	for skill in _available_skills:
		var stack_count = _applied_skills.get(skill.skill_id, 0)
		if skill.max_stacks == -1 or stack_count < skill.max_stacks:
			for _i in range(int(skill.weight)):
				weighted_skills.append(skill)
	
	if weighted_skills.is_empty():
		return []
	
	var selected: Array[SkillDefinition] = []
	var attempts = 0
	var max_attempts = count * 3
	
	while selected.size() < count and attempts < max_attempts:
		var random_skill = weighted_skills[randi() % weighted_skills.size()]
		if random_skill not in selected:
			selected.append(random_skill)
		attempts += 1
	
	return selected

func apply_skill(skill: SkillDefinition, level: int = 1) -> void:
	var stack_count = _applied_skills.get(skill.skill_id, 0)
	
	if skill.max_stacks != -1 and stack_count >= skill.max_stacks:
		return
	
	skill.apply_to_stats(_hero_stats)
	
	_applied_skills[skill.skill_id] = stack_count + 1
	skill_applied.emit(skill, _hero_stats)
	
	_log_selection(skill.skill_id, level)

func apply_skills(skills: Array[SkillDefinition], level: int = 1) -> void:
	for skill in skills:
		apply_skill(skill, level)

func get_applied_skill_count(skill_id: String) -> int:
	return _applied_skills.get(skill_id, 0)

func get_all_applied_skills() -> Dictionary:
	return _applied_skills.duplicate()

func get_hero_stats() -> HeroStats:
	return _hero_stats

func set_hero_stats(stats: HeroStats) -> void:
	_hero_stats = stats

func _log_selection(skill_id: String, level: int) -> void:
	if _log_kit:
		_log_kit.log_event("skill_selected", {
			"skill_id": skill_id,
			"level": level,
			"timestamp": Time.get_ticks_msec()
		})
	
	skill_selection_logged.emit(skill_id, level)

func reset_skills() -> void:
	_applied_skills.clear()
